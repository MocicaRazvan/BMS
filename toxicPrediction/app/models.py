import os
from itertools import chain

import torch
from transformers import pipeline

from app_config import LANGUAGE_MODEL_ID, TOXIC_MODEL_ID, LANGUAGE_MIN_SCORE, TOXIC_MIN_SCORE, NEUTRAL_LABELS_LIST
from lang_detect import is_langdetect_english
from tiny_transformer import load_model_and_tokenizer, predict_long_text
from utils import sliding_chunks

torch.set_num_threads(int(os.getenv("TORCH_NUM_THREADS", 1)))
torch.set_num_interop_threads(int(os.getenv("TORCH_INTEROP_THREADS", 1)))

pipe_lang_det=pipeline("text-classification", model=LANGUAGE_MODEL_ID,device='cpu', top_k=None)
pipe_toxic_det=pipeline("text-classification", model=TOXIC_MODEL_ID,device='cpu', top_k=None)
guardModel,guardTokenizer,guardDevice=load_model_and_tokenizer()

def is_tiny_toxic(text:str)->bool:
    return predict_long_text(text, guardModel, guardTokenizer, guardDevice)

def predict_english(text:str)->bool:
    if is_langdetect_english(text):
        return True
    chunks = sliding_chunks(text, pipe_lang_det.tokenizer.model_max_length)

    predictions = pipe_lang_det(chunks)
    all_preds = chain.from_iterable(predictions)

    return any(p['label'] == 'en' and p['score'] >= LANGUAGE_MIN_SCORE for p in all_preds)


def predict_toxicity(text:str)->bool:
    # fast guard model
    if is_tiny_toxic(text):
        return True
    chunks = sliding_chunks(text,  pipe_toxic_det.tokenizer.model_max_length)

    predictions = pipe_toxic_det(chunks)
    all_preds = chain.from_iterable(predictions)

    return any(p['label'] not in NEUTRAL_LABELS_LIST and p['score'] >= TOXIC_MIN_SCORE for p in all_preds)