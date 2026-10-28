package com.example.langchain.web;

import com.example.langchain.demo.StructuredOutputDemo;
import com.example.langchain.demo.StructuredOutputDemo.SupportTicket;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Module 6: turning free text into a typed object. */
@RestController
@RequestMapping("/api")
public class StructuredOutputController {

    private final StructuredOutputDemo structured;

    public StructuredOutputController(StructuredOutputDemo structured) {
        this.structured = structured;
    }

    // Module 6: POST /api/ticket  body: My internet has been down for two hours...
    @PostMapping("/ticket")
    public SupportTicket ticket(@RequestBody String complaint) {
        return structured.extract(complaint);
    }
}
