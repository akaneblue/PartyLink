from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from sentence_transformers import SentenceTransformer, util

app = FastAPI()
model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")

class MatchRequest(BaseModel):
    query: str
    candidates: List[str]

@app.post("/match-tone")
def match_tone(req: MatchRequest):
    query_vec = model.encode(req.query, convert_to_tensor=True)
    candidate_vecs = model.encode(req.candidates, convert_to_tensor=True)

    scores = util.cos_sim(query_vec, candidate_vecs)[0]
    best_idx = scores.argmax().item()
    best_match = req.candidates[best_idx]

    return {"matched_tag": best_match, "score": float(scores[best_idx])}
