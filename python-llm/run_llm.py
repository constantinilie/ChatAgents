import os
from dotenv import load_dotenv
load_dotenv()

host = os.getenv("GEMINI_HOST", "127.0.0.1")
port = int(os.getenv("GEMINI_PORT", "8000"))

import uvicorn
uvicorn.run("gemini_service:app", host=host, port=port, reload=False)
