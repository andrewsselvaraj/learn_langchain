package com.example.langchain.standalone.openai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * The OpenAI version of standalone.Models: the same three models, built from environment variables.
 * Set OPENAI_API_KEY (and optionally OPENAI_CHAT_MODEL / OPENAI_EMBEDDING_MODEL).
 */
final class OpenAiModels {

    private static final String CHAT_MODEL = env("OPENAI_CHAT_MODEL", "gpt-4o-mini");
    private static final String EMBEDDING_MODEL = env("OPENAI_EMBEDDING_MODEL", "text-embedding-3-small");
    private static final double TEMPERATURE = 0.0;

    private OpenAiModels() {
    }

    static ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(requireKey())
                .modelName(CHAT_MODEL)
                .temperature(TEMPERATURE)
                .logRequests(true)   // see exactly what the model received
                .build();
    }

    static StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(requireKey())
                .modelName(CHAT_MODEL)
                .temperature(TEMPERATURE)
                .build();
    }

    static EmbeddingModel embeddingModel() {
        // Use the same embedding model for indexing and for queries
        return OpenAiEmbeddingModel.builder()
                .apiKey(requireKey())
                .modelName(EMBEDDING_MODEL)
                .build();
    }

    /** Joins the command-line arguments, or falls back to a default. */
    static String argOr(String[] args, String fallback) {
        return args.length > 0 ? String.join(" ", args) : fallback;
    }

    private static String requireKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Set the OPENAI_API_KEY environment variable (get a key at https://platform.openai.com/api-keys)");
        }
        return key;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
