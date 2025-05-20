from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from transformers import pipeline
import requests
import json
import os

app = FastAPI()

# ✅ 모델 초기화 (zero-shot classification)
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

# ✅ OpenRouter API Key
OPENROUTER_API_KEY = "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"  # 실제 키로 바꿔줘

# ✅ 요청 모델
class InterestMatchRequest(BaseModel):
    text: str
    candidate_interests: List[str]

# ✅ 응답 모델
class InterestMatchResponse(BaseModel):
    matched: str
    is_new: bool

# ✅ LLM 호출 함수 (OpenRouter 사용)
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

# ✅ 핵심 라우팅 함수
@app.post("/keyword", response_model=InterestMatchResponse)
def match_interest(req: InterestMatchRequest):
    # 🔹 후보 관심사가 아예 없는 경우 → 바로 LLM 호출
    if not req.candidate_interests:
        new_keyword = ask_llm_for_new_interest(req.text)
        return InterestMatchResponse(matched=new_keyword, is_new=True)

    # 🔹 후보가 있을 경우 → zero-shot 우선 시도
    result = classifier(req.text, req.candidate_interests)
    top_label = result["labels"][0]
    top_score = result["scores"][0]

    print("[Zero-shot] 전체 결과:")
    for label, score in zip(result["labels"], result["scores"]):
        print(f" - {label}: {score:.4f}")

    # 🔹 신뢰도 기준 이상이면 매칭된 기존 관심사 사용
    if top_score >= 0.55:
        return InterestMatchResponse(matched=top_label, is_new=False)
    else:
        # 🔹 신뢰도가 낮으면 LLM으로 새 키워드 생성
        new_keyword = ask_llm_for_new_interest(req.text)
        return InterestMatchResponse(matched=new_keyword, is_new=True)

