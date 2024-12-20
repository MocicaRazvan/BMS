import logging
import logging_loki
from multiprocessing import Queue

from config import APP_NAME,LOKI_URL

handler = None

if LOKI_URL:
    handler = logging_loki.LokiQueueHandler(
        Queue(-1),
        url=LOKI_URL,
        tags={"application": APP_NAME},
    )

logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)

logger = logging.getLogger(APP_NAME)

if LOKI_URL and handler:
    logger.addHandler(handler)

