from langdetect import detect_langs

from utils import sliding_chunks
from app_config import LANGDECT_ENGLISH_MIN_SCORE, CHUNK_FACTOR


def is_langdetect_english(text: str) -> bool:
    try:
        chunks = sliding_chunks(text, max_length=1000 // CHUNK_FACTOR)

        for chunk in chunks:
            detected_langs = detect_langs(chunk)
            found_english = False
            for lang in detected_langs:
                if lang.prob < LANGDECT_ENGLISH_MIN_SCORE:
                    break
                if lang.lang == 'en' and lang.prob >= LANGDECT_ENGLISH_MIN_SCORE:
                    found_english = True
                    break
            if not found_english:
                return False

        return True
    except Exception as e:
        print(f"Error detecting language: {e}")
    return False
