package com.example.langchain.standalone.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

public class LangChainExample  {
    String apiKey = System.getenv("OPENAI_API_KEY");
    private static final SystemMessage CONTEXT = SystemMessage.from("""
            You are a customer support assistant.
            Classify the complaint into exactly one category:
            Technical Issue, Billing Issue, Service Complaint, Other.
            Respond with only the category name.""");
    public void chatmodel() {
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-4o-mini")
            .build();
            String response = openAiChatModel.chat("I am open AI chat model, I can help you with your questions. What would you like to know? ");
            System.out.println(response);
            response = openAiChatModel.chat("How many other models in OpenAiChatModel?");
            System.out.println(response);

    }

    public  void retrievePdfTextFromUrl(String urlStr) throws IOException {
        URL url = URI.create(urlStr).toURL();

        // 1. Open an input stream directly to the website's PDF file
        try (InputStream inputStream = url.openStream()) {
            
            // 2. Initialize the LangChain4j PDF parser (powered by Apache PDFBox)
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            
            // 3. Load and parse the input stream into a LangChain4j Document instance
            Document document = parser.parse(inputStream);
            
            // 4. Return the raw text data extracted from the document
            System.out.println(document.text());
        }
    }

    
        public void complaintClassificationSystem() {
            String complaint = "My internet has been down for two hours and nobody answers the support line.";
            OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();
            System.out.println(model.chat(complaint));
        }

        public void complaintClassificationSystemWithContext() {
            String complaint = "My internet has been down for two hours and nobody answers the support line.";
            String context = "You are a customer support assistant. Classify the following complaint into one of the categories: 'Technical Issue', 'Billing Issue', 'Service Complaint', or 'Other'.";
            OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();
            System.out.println(model.chat(context + "\nComplaint: " + complaint));
        }

        public void complaintClassificationSystemWithContextAndSystemMessage() {
            String complaint = "My internet has been down for two hours and nobody answers the support line.";
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
            """ ;
            String fullPrompt = prompt + complaint;
            ChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .build();
            String response = model.chat(fullPrompt);

        // 5. Display classification
        System.out.println("\n===== COMPLAINT CLASSIFICATION =====");
        System.out.println(response);

        }

        public static void main(String[] args) {
        LangChainExample helloWorld = new LangChainExample();
        //helloWorld.chatmodel();
        /*
        try {
            helloWorld.retrievePdfTextFromUrl("https://www.elections.tn.gov.in/Form20_TNLA2026/dt1/AC001.pdf");
        } catch (IOException ex) {
            System.getLogger(LangChainExample.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }*/
        //helloWorld.complaintClassificationSystem();
        //helloWorld.complaintClassificationSystemWithContext();
        helloWorld.complaintClassificationSystemWithContextAndSystemMessage();
    }
}
