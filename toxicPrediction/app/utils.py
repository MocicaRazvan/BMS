import hashlib
import json
from enum import Enum

from flask import request, jsonify

from app_config import STRIDE_FACTOR, CHUNK_FACTOR
from logger import logger


def make_cache_key():
    """Generates a unique cache key based on the request URL and JSON parameters."""
    user_data = request.get_json(silent=True) or {}
    user_data_str = json.dumps(user_data, sort_keys=True)

    body_hash = hashlib.blake2b(user_data_str.encode('utf-8'), digest_size=16).hexdigest()

    return f"{request.path}:{body_hash}"


def error_response(message, status=400, error=None):
    if error is None:
        error = message
    logger.error(message)
    return jsonify({"error": error, "message": message, "path": request.path, "status": status}), status

def sliding_chunks(text, max_length):
    final_max_length = CHUNK_FACTOR * max_length
    stride = final_max_length // STRIDE_FACTOR
    chunks = []

    for i in range(0, len(text), final_max_length - stride):
        chunk = text[i:i + final_max_length]
        chunks.append(chunk)

    return chunks

class ToxicReason(Enum):
    LANGUAGE = "language"
    TOXICITY = "toxicity"
    NONE = "none"