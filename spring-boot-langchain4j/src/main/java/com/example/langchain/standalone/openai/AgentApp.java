package com.example.langchain.standalone.openai;

import com.example.langchain.demo.AgentDemo;
import com.example.langchain.demo.FlightTools;
import com.example.langchain.demo.ToolsDemo;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Tools and agents on OpenAI: the model calls Java methods to answer.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.openai.AgentApp -Dexec.args="Is flight EK524 on time?"
 */
public class AgentApp {

    public static void main(String[] args) {
        String question = OpenAiModels.argOr(args, "Is flight EK524 on time?");

        ChatModel model = OpenAiModels.chatModel();
        FlightTools tools = new FlightTools();

        System.out.println("== Tools: your code calls the tool");
        new ToolsDemo(model, tools).askAboutFlight("EK524").forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.println("\n== Agent: the model decides which tool to call");
        AgentDemo agent = new AgentDemo(model, tools);
        System.out.println(agent.chat("session1", question));
        // Same thread id, so the agent remembers the flight from the previous turn
        System.out.println(agent.chat("session1", "And what is the business class baggage allowance?"));
    }
}
