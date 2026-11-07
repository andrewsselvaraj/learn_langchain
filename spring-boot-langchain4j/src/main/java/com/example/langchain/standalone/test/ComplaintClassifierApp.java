package com.example.langchain.standalone.test;

import java.util.List;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Complaint classification with an explicit context: the instructions go in a SystemMessage,
 * the complaint goes in a UserMessage.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.test.ComplaintClassifierApp
 */
public class ComplaintClassifierApp {

    // The context: role, task, allowed categories and output format
    private static final SystemMessage CONTEXT = SystemMessage.from("""
            You are a customer support assistant.
            Classify the complaint into exactly one category:
            Technical Issue, Billing Issue, Service Complaint, Other.
            Respond with only the category name.""");

    private final ChatModel model;

    public ComplaintClassifierApp(ChatModel model) {
        this.model = model;
    }

    public String classify(String complaint) {
        ChatResponse response = model.chat(CONTEXT, UserMessage.from(complaint));
        return response.aiMessage().text();
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set the OPENAI_API_KEY environment variable");
        }

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)   // same input, same answer
                .build();

        ComplaintClassifierApp classifier = new ComplaintClassifierApp(model);

        List<String> complaints = List.of(
                "My internet has been down for two hours and nobody answers the support line.",
                "I was charged twice for my subscription this month.",
                "The agent at the store was rude and made me wait 40 minutes.",
                "Can you tell me your office address?");

        for (String complaint : complaints) {
            System.out.println("Complaint: " + complaint);
            System.out.println("Category : " + classifier.classify(complaint));
            System.out.println();
        }
    }
}
