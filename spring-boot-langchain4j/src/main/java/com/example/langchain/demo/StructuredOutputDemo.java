package com.example.langchain.demo;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Service;

/**
 * Module 6: the Pydantic class becomes a Java record, Field(description=...) becomes @Description,
 * and with_structured_output(...) becomes an AI Service method that returns the record.
 */
@Service
public class StructuredOutputDemo {

    public record SupportTicket(
            @Description("one of: billing, technical, account") String category,
            @Description("low, medium, or high") String priority,
            @Description("one sentence, under twenty words") String summary) {
    }

    interface TicketExtractor {
        @UserMessage("Turn this customer complaint into a support ticket: {{it}}")
        SupportTicket extract(String complaint);
    }

    private final TicketExtractor extractor;

    public StructuredOutputDemo(ChatModel model) {
        this.extractor = AiServices.create(TicketExtractor.class, model);
    }

    /** Validation checks shape, not truth: a confidently wrong category still comes back. */
    public SupportTicket extract(String complaint) {
        return extractor.extract(complaint);
    }
}
