package com.example.langchain.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lesson 2: the model is constructed once and reused everywhere.
 * These are the only provider-specific lines in the project: switching from Gemini
 * to another provider means changing this class and one Maven dependency.
 */
@Configuration
public class LlmConfig {

    @Value("${course.gemini.api-key}")
    private String apiKey;

    @Value("${course.gemini.chat-model}")
    private String chatModelName;

    @Value("${course.gemini.embedding-model}")
    private String embeddingModelName;

    @Value("${course.gemini.temperature}")
    private double temperature;

    @Bean
    public ChatModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(requireKey())
                .modelName(chatModelName)
                .temperature(temperature)
                .logRequests(true)   // see exactly what the model received
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(requireKey())
                .modelName(chatModelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        // Use the same embedding model for indexing and for queries
        return GoogleAiEmbeddingModel.builder()
                .apiKey(requireKey())
                .modelName(embeddingModelName)
                .build();
    }

    private String requireKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Set the GEMINI_API_KEY environment variable (get a key at https://aistudio.google.com/apikey)");
        }
        return apiKey;
    }
}
