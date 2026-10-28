# LangChain concepts in Spring Boot (LangChain4j + Google Gemini)

Spring Boot 3.5 + LangChain4j 1.22 + Google Gemini. One class per lesson in
`src/main/java/com/example/langchain/demo`, one REST endpoint per lesson in `web/CourseController`.

## Run the offline tests (no API key needed)

    mvn test

## Run the app against Gemini

1. Create an API key at https://aistudio.google.com/apikey
2. Put it in an environment variable (never in source code):

       export GEMINI_API_KEY=your-key          # macOS / Linux
       set GEMINI_API_KEY=your-key             # Windows cmd
       $env:GEMINI_API_KEY="your-key"          # Windows PowerShell

3. Start the app:

       mvn spring-boot:run

Models default to `gemini-2.5-flash` (chat) and `gemini-embedding-001` (RAG embeddings).
Override them with `GEMINI_CHAT_MODEL` / `GEMINI_EMBEDDING_MODEL` if you want a different one.

Then try:

    curl "localhost:8080/api/models?q=Explain%20RAG%20in%20two%20lines"
    curl "localhost:8080/api/prompts?role=teacher&topic=decorators"
    curl "localhost:8080/api/chain?role=teacher&topic=lists"
    curl -N "localhost:8080/api/stream?q=Explain%20tuples"
    curl -X POST localhost:8080/api/ticket -d "My internet has been down for two hours"
    curl "localhost:8080/api/rag?q=How%20many%20sick%20leave%20days%20are%20allowed"
    curl "localhost:8080/api/tools/flight?no=EK524"
    curl "localhost:8080/api/agent?thread=session1&q=Is%20flight%20EK524%20on%20time"
    curl "localhost:8080/api/agent?thread=session1&q=What%20time%20will%20it%20arrive"

| Lesson | Class |
| --- | --- |
| Models | `ModelsDemo` (+ `config/LlmConfig`) |
| Messages and memory | `MessagesDemo` |
| Prompt templates | `PromptsDemo` |
| Composition and utilities | `CompositionDemo` |
| Structured output | `StructuredOutputDemo` |
| Retrieval (RAG) | `RagDemo` |
| Tools | `FlightTools`, `ToolsDemo` |
| Agents | `AgentDemo` |
