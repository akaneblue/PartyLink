from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
from sentence_transformers import SentenceTransformer, util
from transformers import pipeline
import requests
import os
import json

app = FastAPI()

# -------------------------------
# 공통 모델 정의
# -------------------------------
class PostContent(BaseModel):
    content: str

class MatchRequest(BaseModel):
    query: str
    candidates: List[str]
    threshold: float = 0.7

class InterestMatchRequest(BaseModel):
    text: str
    candidate_interests: List[str]

class InterestMatchResponse(BaseModel):
    matched: str
    is_new: bool

# -------------------------------
# 모델 초기화
# -------------------------------
semantic_model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

# -------------------------------
# OpenRouter API Key 설정
# -------------------------------
OPENROUTER_API_KEY = "API_Key"

# -------------------------------
# 1. 게시글 분위기 단어 추출
# -------------------------------
PROMPT_TEMPLATE = """
##게시글

{post}

##요구조건
게시글의 전반적인 분위기를 분석해줘.

##반환형식
분위기를 가장 잘 표현하는 단어 1개만 출력
"""

@app.post("/analyze-tone")
def analyze_tone(post: PostContent):
    prompt = PROMPT_TEMPLATE.format(post=post.content.strip())
    url = "https://openrouter.ai/api/v1/chat/completions"
    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "deepseek/deepseek-chat-v3-0324:free",
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.7,
        "max_tokens": 10
    }

    response = requests.post(url, json=payload, headers=headers)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail=f"OpenRouter Error: {response.text}")

    tone_word = response.json()["choices"][0]["message"]["content"].strip()
    return {"tone": tone_word}

# -------------------------------
# 2. 유사도 기반 분위기 태그 매칭
# -------------------------------
@app.post("/match-tone")
def match_tone(req: MatchRequest):
    if not req.candidates:
        return {"matched_tags": []}

    query_vec = semantic_model.encode(req.query, convert_to_tensor=True)
    candidate_vecs = semantic_model.encode(req.candidates, convert_to_tensor=True)

    scores = util.cos_sim(query_vec, candidate_vecs)[0]

    matched = [
        {"tag": tag, "score": float(score)}
        for tag, score in zip(req.candidates, scores)
        if score >= req.threshold
    ]

    return {"matched_tags": matched}

# -------------------------------
# 3. 관심사 매칭 및 추론
# -------------------------------
def ask_llm_for_new_interest(text: str) -> str:
    prompt = f"""
    다음 소개글을 보고 해당 모임의 활동 분야를 단 하나의 단어만으로 알려줘.\n출력 형식:단어\n소개글: {text}
    """
    response = requests.post(
        url="https://openrouter.ai/api/v1/chat/completions",
        headers={
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json"
        },
        data=json.dumps({
            "model": "nvidia/llama-3.1-nemotron-ultra-253b-v1:free",
            "messages": [{"role": "user", "content": prompt}]
        })
    )
    if response.status_code == 200:
        result = response.json()
        return result["choices"][0]["message"]["content"].strip()
    else:
        raise Exception(f"LLM 호출 실패: {response.text}")

@app.post("/keyword", response_model=InterestMatchResponse)
def match_interest(req: InterestMatchRequest):
    if not req.candidate_interests:
        new_keyword = ask_llm_for_new_interest(req.text)
        return InterestMatchResponse(matched=new_keyword, is_new=True)

    result = classifier(req.text, req.candidate_interests)
    top_label = result["labels"][0]
    top_score = result["scores"][0]

    print("[Zero-shot] 전체 결과:")
    for label, score in zip(result["labels"], result["scores"]):
        print(f" - {label}: {score:.4f}")

    threshold = (1 / len(req.candidate_interests))+ (1 / 2 * len(req.candidate_interests))
    if top_score >= threshold:
        return InterestMatchResponse(matched=top_label, is_new=False)
    else:
        new_keyword = ask_llm_for_new_interest(req.text)
        return InterestMatchResponse(matched=new_keyword, is_new=True)
