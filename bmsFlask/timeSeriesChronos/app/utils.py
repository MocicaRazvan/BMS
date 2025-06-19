import hashlib
import json

from flask import request, jsonify

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
