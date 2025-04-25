from langdetect import detect_langs

from app_config import LANGDECT_ENGLISH_MIN_SCORE


def is_langdetect_english(text:str)->bool:
    try:
        detected_langs = detect_langs(text)
        for lang in detected_langs:
            if lang.lang == 'en' and lang.prob > LANGDECT_ENGLISH_MIN_SCORE:
                return True
    except Exception as e:
        print(f"Error detecting language: {e}")
    return False