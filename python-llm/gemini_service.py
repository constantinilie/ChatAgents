import os
from enum import Enum
from typing import Optional

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from dotenv import load_dotenv
from pathlib import Path

load_dotenv(dotenv_path=Path(__file__).with_name(".env"))

app = FastAPI(title="Gemini LLM Service")

# Pick a default model. Example from docs: gemini-2.5-flash
# You can change this later (e.g., gemini-2.5-pro).
DEFAULT_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

class Operation(str, Enum):
    translate = "translate"
    correct = "correct"
    rephrase = "rephrase"

class LlmRequest(BaseModel):
    operation: Operation
    text: str = Field(min_length=1, max_length=8000)

    # Optional for translate
    source_lang: Optional[str] = None
    target_lang: Optional[str] = None

    # Optional generation params
    temperature: Optional[float] = Field(default=0.2, ge=0.0, le=2.0)
    max_output_tokens: Optional[int] = Field(default=1024, ge=1, le=8192)

class LlmResponse(BaseModel):
    result: str

def build_prompt(req: LlmRequest) -> str:
    if req.operation == Operation.translate:
        src = (req.source_lang or "auto").strip()
        tgt = (req.target_lang or "en").strip()
        return (
            "You are a precise translator.\n"
            f"Translate from {src} to {tgt}.\n"
            "Return only the translated text.\n\n"
            f"{req.text}"
        )
    if req.operation == Operation.correct:
        return (
            "Task: Correct grammar, spelling, punctuation.\n"
            "Rules:\n"
            "- Do NOT shorten the text.\n"
            "- Keep ALL information and sentences.\n"
            "- Keep the same language.\n"
            "- Return ONLY the corrected text, nothing else.\n\n"
            "TEXT:\n"
            f"{req.text}"
        )
    if req.operation == Operation.rephrase:
        return (
            "You are a writing assistant.\n"
            "Rephrase the text to be clearer and more natural. Keep meaning.\n"
            "Keep the same language.\n"
            "Return only the rephrased text.\n\n"
            f"{req.text}"
        )
    return req.text

@app.post("/v1/transform", response_model=LlmResponse)
async def transform(req: LlmRequest):
    api_key = os.getenv("GEMINI_API_KEY") 
    if not api_key:
        raise HTTPException(status_code=500, detail="Missing GEMINI_API_KEY env var")

    model = DEFAULT_MODEL
    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"

    prompt = build_prompt(req)

    payload = {
        "contents": [
            {
                "role": "user",
                "parts": [{"text": prompt}],
            }
        ],
        "generationConfig": {
            "temperature": req.temperature,
            "maxOutputTokens": req.max_output_tokens,
            "responseMimeType": "text/plain",
        },
    }

    headers = {
        "x-goog-api-key": api_key,  # Gemini API key header
        "Content-Type": "application/json",
    }

    try:
        async with httpx.AsyncClient(timeout=60) as client:
            r = await client.post(url, headers=headers, json=payload)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Gemini request failed: {e}")

    if r.status_code < 200 or r.status_code >= 300:
        raise HTTPException(status_code=r.status_code, detail=r.text)

    data = r.json()
    print(data)
    candidates = data.get("candidates") or []
    if not candidates:
        raise HTTPException(status_code=500, detail=f"No candidates: {data}")

    content = candidates[0].get("content") or {}
    parts = content.get("parts") or []
    if not parts:
        raise HTTPException(status_code=500, detail=f"No parts: {data}")

    text_out = "".join([p.get("text", "") for p in parts]).strip()
    if not text_out:
        raise HTTPException(status_code=500, detail=f"Empty text: {data}")

    return LlmResponse(result=text_out)

