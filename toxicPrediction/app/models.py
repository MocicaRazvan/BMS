import os

import torch
from transformers import pipeline

from lang_detect import is_langdetect_english
from tiny_transformer import load_model_and_tokenizer, predict_long_text
from app_config import LANGUAGE_MODEL_ID, TOXIC_MODEL_ID, LANGUAGE_MIN_SCORE, TOXIC_MIN_SCORE,NEUTRAL_LABELS_LIST

torch.set_num_threads(int(os.getenv("TORCH_NUM_THREADS", 1)))
torch.set_num_interop_threads(int(os.getenv("TORCH_INTEROP_THREADS", 1)))

pipe_lang_det=pipeline("text-classification", model=LANGUAGE_MODEL_ID,device='cpu', top_k=None)
pipe_toxic_det=pipeline("text-classification", model=TOXIC_MODEL_ID,device='cpu', top_k=None)
guardModel,guardTokenizer,guardDevice=load_model_and_tokenizer()

def is_tiny_toxic(text:str)->bool:
    model, tokenizer, device = load_model_and_tokenizer()
    return predict_long_text(text, model, tokenizer, device)

def predict_english(text:str)->bool:
    if is_langdetect_english(text):
        return True
    prediction=pipe_lang_det(text)[0]
    return  any(p['label'] == 'en' and p['score'] >= LANGUAGE_MIN_SCORE for p in prediction)

def predict_toxicity(text:str)->bool:
    # fast guard model
    if is_tiny_toxic(text):
        return True
    prediction=pipe_toxic_det(text)[0]
    return any(p['label'] not in NEUTRAL_LABELS_LIST and p['score'] >= TOXIC_MIN_SCORE for p in prediction)