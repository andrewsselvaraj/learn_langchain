package com.example.langchain.demo;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 3: the model remembers nothing; memory is the list you resend.
 * Python HumanMessage = Java UserMessage, Python AIMessage = Java AiMessage.
 */
@Service
public class MessagesDemo {

    private final ChatModel model;

    public MessagesDemo(ChatModel model) {
        this.model = model;
    }

    /** A persona under your control. */
    public String withPersona(String persona, String question) {
        List<ChatMessage> messages = List.of(
                SystemMessage.from(persona),
                UserMessage.from(question));
        return model.chat(messages).aiMessage().text();
    }

    /** Carrying history forward by hand: append the reply and the next question, send it all again. */
    public String twoTurnsByHand() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from("My name is Priya."));
        AiMessage first = model.chat(messages).aiMessage();

        messages.add(first);
        messages.add(UserMessage.from("What is my name?"));
        return model.chat(messages).aiMessage().text();
    }

    /** "Keep the last N turns" strategy, done for you by a ChatMemory. */
    public String twoTurnsWithMemory() {
        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);
        memory.add(UserMessage.from("My name is Priya."));
        memory.add(model.chat(memory.messages()).aiMessage());
        memory.add(UserMessage.from("What is my name?"));
        return model.chat(memory.messages()).aiMessage().text();
    }
}
