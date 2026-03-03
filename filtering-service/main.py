from fastapi import FastAPI
from pydantic import BaseModel
from nlp_engine import NLPEngine
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Security Intelligence Filtering Service", version="1.0.0")
nlp_engine = NLPEngine()

class FeedItem(BaseModel):
    title: str
    description: str = ""
    url: str = ""
    source: str = ""

class FilterResult(BaseModel):
    is_relevant: bool
    relevance_score: float
    matched_keywords: list[str]
    detected_products: list[str]
    cve_ids: list[str]

@app.get("/health")
def health():
    return {"status": "UP", "service": "filtering-service"}

@app.post("/filter", response_model=FilterResult)
def filter_item(item: FeedItem):
    result = nlp_engine.analyze(item.title, item.description)
    logger.info(f"Filtered: relevant={result['is_relevant']} score={result['relevance_score']:.3f} title='{item.title[:60]}'")
    return result

@app.post("/filter/batch")
def filter_batch(items: list[FeedItem]):
    return [nlp_engine.analyze(item.title, item.description) for item in items]
