package com.example.langchain.standalone.gemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;

/**
 * LlmConfig without Spring: the Gemini models, built from environment variables.
 * Set GEMINI_API_KEY (and optionally GEMINI_CHAT_MODEL / GEMINI_EMBEDDING_MODEL).
 */
final class GeminiModels {

    private static final String CHAT_MODEL = env("GEMINI_CHAT_MODEL", "gemini-2.5-flash");
    private static final String EMBEDDING_MODEL = env("GEMINI_EMBEDDING_MODEL", "gemini-embedding-001");
    private static final double TEMPERATURE = 0.0;

    private GeminiModels() {
    }

    static ChatModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(requireKey())
                .modelName(CHAT_MODEL)
                .temperature(TEMPERATURE)
                .logRequests(true)   // see exactly what the model received
                .build();
    }

    static StreamingChatModel streamingChatModel() {
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(requireKey())
                .modelName(CHAT_MODEL)
                .temperature(TEMPERATURE)
                .build();
    }

    static EmbeddingModel embeddingModel() {
        // Use the same embedding model for indexing and for queries
        return GoogleAiEmbeddingModel.builder()
                .apiKey(requireKey())
                .modelName(EMBEDDING_MODEL)
                .build();
    }

    /** Joins the command-line arguments, or falls back to a default. */
    static String argOr(String[] args, String fallback) {
        return args.length > 0 ? String.join(" ", args) : fallback;
    }

    private static String requireKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Set the GEMINI_API_KEY environment variable (get a key at https://aistudio.google.com/apikey)");
        }
        return key;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
