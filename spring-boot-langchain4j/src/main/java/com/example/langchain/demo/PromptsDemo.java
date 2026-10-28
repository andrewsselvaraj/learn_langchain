package com.example.langchain.demo;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Module 4: templates with named placeholders, filled at run time.
 * LangChain4j uses {{name}} where Python LangChain uses {name}.
 * Filling a template calls no model.
 */
@Service
public class PromptsDemo {

    static final PromptTemplate SYSTEM = PromptTemplate.from("You are a {{role}}.");
    static final PromptTemplate HUMAN = PromptTemplate.from("Explain {{topic}} simply.");

    /** Equivalent of ChatPromptTemplate.from_messages([...]).invoke({...}).to_messages(). */
    public List<ChatMessage> fill(String role, String topic) {
        Prompt system = SYSTEM.apply(Map.of("role", role));
        Prompt human = HUMAN.apply(Map.of("topic", topic));
        return List.of(system.toSystemMessage(), human.toUserMessage());
    }

    /** Equivalent of MessagesPlaceholder("history"): history goes between the system and the new question. */
    public List<ChatMessage> fillWithHistory(String role, List<ChatMessage> history, String topic) {
        List<ChatMessage> filled = fill(role, topic);
        return java.util.stream.Stream.of(List.of(filled.get(0)), history, List.of(filled.get(1)))
                .flatMap(List::stream)
                .toList();
    }
}
