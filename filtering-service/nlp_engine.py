"""
NLP Engine for security feed filtering.
Uses spaCy for NER + keyword ontology matching.
"""
import re
import spacy
from keywords import KEYWORD_ONTOLOGY, PRODUCT_ALIASES

# Load spaCy model (fallback to blank if not available)
try:
    nlp = spacy.load("en_core_web_sm")
except OSError:
    nlp = spacy.blank("en")

# Compiled regex for CVE detection
CVE_PATTERN = re.compile(r"CVE-\d{4}-\d{4,7}", re.IGNORECASE)

# Flatten keywords for fast lookup
ALL_KEYWORDS = {}
for product, keywords in KEYWORD_ONTOLOGY.items():
    for kw in keywords:
        ALL_KEYWORDS[kw.lower()] = product

class NLPEngine:
    def analyze(self, title: str, description: str) -> dict:
        text = f"{title} {description}".lower()
        full_text = f"{title} {description}"

        matched_keywords = []
        detected_products = set()

        # 1. Keyword matching
        for kw, product in ALL_KEYWORDS.items():
            if kw in text:
                matched_keywords.append(kw)
                detected_products.add(product)

        # 2. spaCy NER for organizations/products
        doc = nlp(full_text[:1000])  # limit for performance
        for ent in doc.ents:
            if ent.label_ in ("ORG", "PRODUCT", "GPE"):
                ent_lower = ent.text.lower()
                for kw, product in ALL_KEYWORDS.items():
                    if kw in ent_lower or ent_lower in kw:
                        detected_products.add(product)

        # 3. CVE extraction
        cve_ids = list(set(CVE_PATTERN.findall(full_text)))

        # 4. Relevance score calculation
        keyword_score = min(len(matched_keywords) * 0.2, 0.7)
        cve_bonus = 0.2 if cve_ids else 0.0
        ner_bonus = 0.1 if len(detected_products) > 0 else 0.0
        relevance_score = min(keyword_score + cve_bonus + ner_bonus, 1.0)

        is_relevant = len(matched_keywords) > 0 or len(detected_products) > 0

        return {
            "is_relevant": is_relevant,
            "relevance_score": round(relevance_score, 3),
            "matched_keywords": list(set(matched_keywords)),
            "detected_products": list(detected_products),
            "cve_ids": cve_ids,
        }
