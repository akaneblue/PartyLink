from fastapi import FastAPI
from pydantic import BaseModel
import requests
import json
import os

# ✅ FastAPI 인스턴스 생성
app = FastAPI()

# ✅ Pydantic 모델 정의 (요청 데이터)
class TextInput(BaseModel):
    text: str

# ✅ API 키 (환경변수나 하드코딩 가능 – 보안상 환경변수 추천)
OPENROUTER_API_KEY = "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"  # <- 실제 키로 바꿔주세요

# ✅ POST 요청 처리
@app.post("/keyword")
def extract_keywords(input: TextInput):
    # OpenRouter API 요청
    response = requests.post(
        url="https://openrouter.ai/api/v1/chat/completions",
        headers={
            "Authorization": f"Bearer {OPENROUTER_API_KEY}",
            "Content-Type": "application/json"
        },
        data=json.dumps({
            "model": "nvidia/llama-3.1-nemotron-ultra-253b-v1:free",  # 정확한 모델명 사용
            "messages": [
                {
                    "role": "user",
                    "content": f"다음 모임 소개글을 보고 해당 모임이 하고자 하는 활동의 분야를 단어 하나로 알려줘.\n 출력 형식:단어 \n 소개글:{input.text}"
                }
            ]
        })
    )

    # 응답 JSON 파싱
    if response.status_code == 200:
        result = response.json()
        message = result["choices"][0]["message"]["content"]
        return {"keyword": message.strip()}
    else:
        return {"error": response.text}
