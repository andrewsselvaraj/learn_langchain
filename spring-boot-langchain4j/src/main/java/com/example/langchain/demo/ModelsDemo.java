package com.example.langchain.demo;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Module 2: messages in, one AiMessage out, no state between calls. */
@Service
public class ModelsDemo {

    private final ChatModel model;

    public ModelsDemo(ChatModel model) {
        this.model = model;
    }

    /** The string form: a bare string is wrapped as one user turn (Python: model.invoke("...")). */
    public String simple(String question) {
        return model.chat(question);
    }

    /** The full response object: the text is one field among several. */
    public Map<String, Object> anatomy(String question) {
        ChatResponse response = model.chat(UserMessage.from(question));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("content", response.aiMessage().text());            // response.content
        fields.put("modelName", response.metadata().modelName());       // response_metadata
        fields.put("finishReason", String.valueOf(response.finishReason()));
        fields.put("tokenUsage", String.valueOf(response.tokenUsage())); // usage_metadata
        fields.put("toolCalls", response.aiMessage().toolExecutionRequests()); // tool_calls
        return fields;
    }
}
