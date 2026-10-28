package com.example.langchain.web;

import com.example.langchain.demo.CompositionDemo;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/** Module 5: chaining steps together and streaming tokens. */
@RestController
@RequestMapping("/api")
public class CompositionController {

    private final CompositionDemo composition;
    private final StreamingChatModel streamingModel;

    public CompositionController(CompositionDemo composition, StreamingChatModel streamingModel) {
        this.composition = composition;
        this.streamingModel = streamingModel;
    }

    // Module 5: GET /api/chain?role=teacher&topic=lists
    @GetMapping("/chain")
    public String chain(@RequestParam String role, @RequestParam String topic) {
        return composition.chain().apply(Map.of("role", role, "topic", topic));
    }

    // Module 5: GET /api/stream?q=Explain tuples   (tokens arrive as server-sent events)
    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam String q) {
        SseEmitter emitter = new SseEmitter();
        streamingModel.chat(q, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });
        return emitter;
    }
}
