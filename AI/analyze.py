from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import requests
import os

app = FastAPI()

# 환경변수 또는 직접 키 설정
OPENROUTER_API_KEY = "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"

# ✅ 요청 데이터 형식
class PostContent(BaseModel):
    content: str

# ✅ 분위기 추출용 프롬프트 템플릿
PROMPT_TEMPLATE = """
##게시글

{post}

##요구조건
게시글의 전반적인 분위기를 분석해줘.

##반환형식
분위기를 가장 잘 표현하는 단어 1개만 출력
"""

# ✅ OpenRouter 요청
def query_openrouter(prompt: str):
    url = "https://openrouter.ai/api/v1/chat/completions"
    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": "deepseek/deepseek-chat-v3-0324:free",
        "messages": [
            {"role": "user", "content": prompt}
        ],
        "temperature": 0.7,
        "max_tokens": 10  # 한 단어만 받도록 제한
    }

    response = requests.post(url, json=payload, headers=headers)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail=f"OpenRouter Error: {response.text}")
    
    return response.json()["choices"][0]["message"]["content"].strip()

# ✅ API 엔드포인트
@app.post("/analyze-tone")
def analyze_tone(post: PostContent):
    prompt = PROMPT_TEMPLATE.format(post=post.content.strip())
    tone_word = query_openrouter(prompt)
    return {"tone": tone_word}
