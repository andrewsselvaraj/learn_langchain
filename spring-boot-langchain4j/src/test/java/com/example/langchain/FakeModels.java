package com.example.langchain;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.ArrayList;
import java.util.List;

/** Offline stand-ins so every module can be tested without a Gemini API key (like FakeListChatModel). */
final class FakeModels {

    private FakeModels() {
    }

    /** A scripted chat model that behaves just enough like a real one to show each concept. */
    static final class ScriptedChatModel implements ChatModel {

        final List<ChatRequest> requests = new ArrayList<>();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            requests.add(request);
            List<ChatMessage> messages = request.messages();
            ChatMessage last = messages.get(messages.size() - 1);
            return ChatResponse.builder().aiMessage(reply(request, messages, last)).build();
        }

        private AiMessage reply(ChatRequest request, List<ChatMessage> messages, ChatMessage last) {
            // Agent step 5-6: a tool result came back, so answer from it
            if (last instanceof ToolExecutionResultMessage result) {
                return AiMessage.from("Final answer: " + result.text());
            }
            String text = ((UserMessage) last).singleText();
            boolean toolsAvailable = request.toolSpecifications() != null && !request.toolSpecifications().isEmpty();

            // Agent step 2-3: the model decides it needs a tool and emits a tool call
            if (toolsAvailable && text.contains("EK524")) {
                return AiMessage.from(toolCall("getFlightStatus", "{\"flightNo\":\"EK524\"}"));
            }
            if (toolsAvailable && text.contains("arrive") && mentionedEarlier(messages, "EK524")) {
                return AiMessage.from(toolCall("getFlightStatus", "{\"flightNo\":\"EK524\"}"));
            }
            if (toolsAvailable && text.toLowerCase().contains("economy")) {
                return AiMessage.from(toolCall("getBaggageAllowance", "{\"travelClass\":\"economy\"}"));
            }
            if (text.contains("support ticket")) {
                return AiMessage.from("{\"category\":\"technical\",\"priority\":\"high\","
                        + "\"summary\":\"Internet down for two hours before a customer meeting\"}");
            }
            if (text.contains("What is my name?")) {
                return AiMessage.from(mentionedEarlier(messages, "My name is Priya")
                        ? "Your name is Priya." : "I do not know your name.");
            }
            return AiMessage.from("Echo: " + text);
        }

        private static ToolExecutionRequest toolCall(String name, String args) {
            return ToolExecutionRequest.builder().id("call-1").name(name).arguments(args).build();
        }

        private static boolean mentionedEarlier(List<ChatMessage> messages, String phrase) {
            return messages.subList(0, messages.size() - 1).stream()
                    .filter(UserMessage.class::isInstance)
                    .anyMatch(m -> ((UserMessage) m).singleText().contains(phrase));
        }
    }

    /** A bag-of-words embedding: texts sharing words land close together. */
    static final class WordEmbeddingModel implements EmbeddingModel {

        private static final int DIM = 64;

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
            return Response.from(segments.stream().map(s -> embedText(s.text())).toList());
        }

        @Override
        public Response<Embedding> embed(String text) {
            return Response.from(embedText(text));
        }

        @Override
        public Response<Embedding> embed(TextSegment segment) {
            return embed(segment.text());
        }

        @Override
        public int dimension() {
            return DIM;
        }

        private static Embedding embedText(String text) {
            float[] v = new float[DIM];
            for (String word : text.toLowerCase().split("\\W+")) {
                if (word.length() > 3) {
                    v[Math.floorMod(word.hashCode(), DIM)] += 1;
                }
            }
            return Embedding.from(v);
        }
    }
}
