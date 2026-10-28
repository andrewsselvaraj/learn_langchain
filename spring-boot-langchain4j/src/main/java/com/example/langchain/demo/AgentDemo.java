package com.example.langchain.demo;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

/**
 * Agents: the MODEL decides which tool to call, the AI Service runtime executes it and loops
 * until a final answer. Python create_agent(...) + InMemorySaver + thread_id maps to
 * AiServices + ChatMemoryProvider + @MemoryId.
 */
@Service
public class AgentDemo {

    public interface AirlineAgent {
        @SystemMessage("You are a helpful airline assistant. Use the available tools when needed.")
        String chat(@MemoryId String threadId, @UserMessage String message);
    }

    private final AirlineAgent agent;

    public AgentDemo(ChatModel model, FlightTools tools) {
        // In-memory per thread, like InMemorySaver. Production: a persistent ChatMemoryStore.
        ChatMemoryProvider memoryPerThread = threadId -> MessageWindowChatMemory.builder()
                .id(threadId)
                .maxMessages(20)
                .build();
        this.agent = AiServices.builder(AirlineAgent.class)
                .chatModel(model)
                .tools(tools)                       // the agent can only do what these expose
                .chatMemoryProvider(memoryPerThread)
                .maxSequentialToolsInvocations(5)   // loop limit
                .build();
    }

    /** Same threadId = same conversation; a new threadId starts fresh. */
    public String chat(String threadId, String message) {
        return agent.chat(threadId, message);
    }
}
