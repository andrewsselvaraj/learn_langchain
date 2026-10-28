package com.example.langchain.demo;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** A chain with a tool in front of it: YOUR code decides the tool runs. This is not an agent yet. */
@Service
public class ToolsDemo {

    private static final PromptTemplate PROMPT = PromptTemplate.from(
            "Based on the context: {{context}}\nAnswer the query: {{query}}");

    private final ChatModel model;
    private final FlightTools tools;

    public ToolsDemo(ChatModel model, FlightTools tools) {
        this.model = model;
        this.tools = tools;
    }

    /** tool.name / tool.description / tool.args equivalent. */
    public List<ToolSpecification> describeTools() {
        return ToolSpecifications.toolSpecificationsFrom(FlightTools.class);
    }

    /** One tool call, many questions: reuse the fetched result instead of calling the API again. */
    public Map<String, String> askAboutFlight(String flightNo) {
        String context = tools.getFlightStatus(flightNo);
        return Map.of(
                "status", ask(context, "status"),
                "arrival", ask(context, "arrival time"),
                "gate", ask(context, "gate"));
    }

    private String ask(String context, String query) {
        String prompt = PROMPT.apply(Map.of("context", context, "query", query)).text();
        return model.chat(prompt);
    }
}
