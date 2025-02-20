from flask import request, jsonify

from logger import logger


def make_cache_key():
    """Generates a unique cache key based on the request URL and JSON parameters."""
    user_data = request.get_json(silent=True) or {}

    key_parts = [f"{key}={value}" for key, value in user_data.items()]

    return f"{request.path}:" + ",".join(key_parts)


def error_response(message, status=400, error=None):
    if error is None:
        error = message
    logger.error(message)
    return jsonify({"error": error, "message": message, "path": request.path, "status": status}), status
