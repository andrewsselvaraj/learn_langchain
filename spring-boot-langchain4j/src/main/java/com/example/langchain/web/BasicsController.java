package com.example.langchain.web;

import com.example.langchain.demo.MessagesDemo;
import com.example.langchain.demo.ModelsDemo;
import com.example.langchain.demo.PromptsDemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Modules 2-4: models, messages and prompt templates. */
@RestController
@RequestMapping("/api")
public class BasicsController {

    private final ModelsDemo models;
    private final MessagesDemo messages;
    private final PromptsDemo prompts;

    public BasicsController(ModelsDemo models, MessagesDemo messages, PromptsDemo prompts) {
        this.models = models;
        this.messages = messages;
        this.prompts = prompts;
    }

    // Module 2: GET /api/models?q=Explain RAG in two lines.
    @GetMapping("/models")
    public Map<String, Object> model(@RequestParam String q) {
        return models.anatomy(q);
    }

    // Module 3: GET /api/messages/memory
    @GetMapping("/messages/memory")
    public String memory() {
        return messages.twoTurnsByHand();
    }

    // Module 4: GET /api/prompts?role=Python instructor&topic=decorators (no model is called)
    @GetMapping("/prompts")
    public List<String> prompt(@RequestParam String role, @RequestParam String topic) {
        return prompts.fill(role, topic).stream().map(Object::toString).toList();
    }
}
