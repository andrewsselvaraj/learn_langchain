package com.example.langchain.standalone.openai;

import com.example.langchain.demo.CompositionDemo;
import com.example.langchain.demo.PromptsDemo;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Module 5 on OpenAI: chaining steps together and streaming tokens.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.openai.CompositionApp -Dexec.args="Explain tuples"
 */
public class CompositionApp {

    public static void main(String[] args) {
        String question = OpenAiModels.argOr(args, "Explain tuples");

        System.out.println("== Module 5: chain");
        CompositionDemo composition = new CompositionDemo(OpenAiModels.chatModel(), new PromptsDemo());
        System.out.println(composition.chain().apply(Map.of("role", "teacher", "topic", "lists")));

        // Tokens are printed as they arrive instead of sent as server-sent events
        System.out.println("\n== Module 5: stream");
        CompletableFuture<Void> done = new CompletableFuture<>();
        OpenAiModels.streamingChatModel().chat(question, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                System.out.print(token);
                System.out.flush();
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                System.out.println();
                done.complete(null);
            }

            @Override
            public void onError(Throwable error) {
                done.completeExceptionally(error);
            }
        });
        done.join();   // keep main alive until the stream finishes
    }
}
