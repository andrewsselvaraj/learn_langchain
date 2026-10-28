# learn_langchain

Hands-on examples for learning LangChain concepts: models, messages, prompt templates,
composition, structured output, retrieval (RAG), tools and agents.

| Folder | What it is |
| --- | --- |
| [`spring-boot-langchain4j/`](spring-boot-langchain4j) | Spring Boot 3.5 + LangChain4j 1.22 + Google Gemini. One class and one REST endpoint per lesson, plus offline tests. |
| [`python/`](python) | `practice.py`: the same concepts in Python LangChain, runnable offline with a fake model. |

## Quick start (Spring Boot)

    cd spring-boot-langchain4j
    mvn test                          # offline, no API key needed
    export GEMINI_API_KEY=your-key    # from https://aistudio.google.com/apikey
    mvn spring-boot:run

See [`spring-boot-langchain4j/README.md`](spring-boot-langchain4j/README.md) for the endpoints to try.
Never commit your API key; it is read from the `GEMINI_API_KEY` environment variable.
