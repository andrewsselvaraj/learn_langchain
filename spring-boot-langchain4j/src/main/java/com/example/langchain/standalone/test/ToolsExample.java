package com.example.langchain.standalone.test;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

public class ToolsExample  {

     private final String apiKey = System.getenv("OPENAI_API_KEY");

    @dev.langchain4j.agent.tool.Tool("Finds the department responsible for a complaint")
    public String getDepartment(
        @dev.langchain4j.agent.tool.P("Complaint category") String category) {

        if (category.equalsIgnoreCase("Technical Issue")) {
            return "Technical Support";
        }

        if (category.equalsIgnoreCase("Billing")) {
            return "Billing Department";
        }

        if (category.equalsIgnoreCase("Payment")) {
            return "Payment Support";
        }

        return "Customer Care";
    }

    // The model classifies the complaint, then calls getDepartment (category) itself
    interface ComplaintAssistant {
        @SystemMessage("""
                You are a customer support assistant.
                Classify the complaint into one category: Technical Issue, Billing, Payment, Other.
                Then call getDepartment with that category.
                Reply with only the department name the tool returns.""")
        String findDepartment(String complaint);

        @SystemMessage("""
                You are a customer support assistant.
                Decide the priority of the complaint: Critical, High, Medium, Low.
                Reply with only the priority.""")
        String findPriority(String complaint);

        @SystemMessage("""
                You are a customer support assistant.
                Detect the customer's sentiment: Angry, Frustrated, Neutral, Happy.
                Reply with only the sentiment.""")
        String findSentiment(String complaint);
    }

    public void findComplaintDepartment(String complaint) {
        ChatModel model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName("gpt-4o-mini")
            .temperature(0.0)
            .build();

        ComplaintAssistant assistant = AiServices.builder(ComplaintAssistant.class)
            .chatModel(model)
            .tools(this)          // exposes getDepartment to the model
            .build();

        System.out.println("Complaint : " + complaint.strip());
        String department = assistant.findDepartment(complaint);
        System.out.println("Department: " + department);
        // Each call below is a separate request to OpenAI
        System.out.println("Priority  : " + assistant.findPriority(complaint));
        System.out.println("Sentiment : " + assistant.findSentiment(complaint));
    }

     public static void main(String[] args) {
         ToolsExample helloWorld = new ToolsExample();
         //try {
         //    helloWorld.helloWorld();
         //} catch (Exception ex) {
         //    System.getLogger(LangChainExample.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
         //}
         //helloWorld.complaintClassificationSystem();

         String complaint = """
            My internet is not working.
            """;
         helloWorld.findComplaintDepartment(complaint);
     }
}
