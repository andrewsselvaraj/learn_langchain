package com.example.langchain.standalone.test;

import java.util.Scanner;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class ComplaintClassifier {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // 1. Get complaint from customer
        System.out.println("Enter Customer Complaint:");

        String complaint = scanner.nextLine();

        // 2. Connect to OpenAI model
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .build();

        // 3. Define classification instruction
        String prompt = """
            You are a company complaint classification system.

            Classify the following customer complaint.

            Return the result in JSON format with these fields:

            category
            subcategory
            issue_type
            priority
            severity
            department
            sentiment
            customer_impact
            duration

            Classification rules:
            - Categories: Technical Issue, Billing,
              Payment, Customer Service, Product Issue, Other.
            - Priority: Critical, High, Medium, Low.
            - Severity: Critical, Major, Minor.
            - Do not invent information.
            - If information is missing, return Unknown.
            - Consider customer impact when deciding priority.
            - Identify multiple issues if present.

            Customer Complaint:
            """ + complaint;

        // 4. Send prompt to LLM
        String response = model.chat(prompt);

        // 5. Display classification
        System.out.println("\n===== COMPLAINT CLASSIFICATION =====");
        System.out.println(response);

        scanner.close();
    }
}
