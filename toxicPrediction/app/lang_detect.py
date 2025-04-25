from langdetect import detect_langs

def is_langdetect_english(text:str)->bool:
    try:
        detected_langs = detect_langs(text)
        for lang in detected_langs:
            if lang.lang == 'en' and lang.prob > 0.75:
                return True
    except Exception as e:
        print(f"Error detecting language: {e}")
    return False