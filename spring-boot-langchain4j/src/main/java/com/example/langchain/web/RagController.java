package com.example.langchain.web;

import com.example.langchain.demo.RagDemo;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/** Module 7: retrieval-augmented generation over the sample handbook. */
@RestController
@RequestMapping("/api")
public class RagController {

    private final RagDemo rag;

    public RagController(RagDemo rag) {
        this.rag = rag;
    }

    // Module 7: GET /api/rag?q=How many sick leave days are allowed?
    @GetMapping("/rag")
    public String rag(@RequestParam String q) throws IOException {
        if (rag.retrieve(q).isEmpty()) {
            // From a file on disk you would use RagDemo.load(Path.of("data/handbook.pdf"))
            String text = new String(new ClassPathResource("data/handbook.txt").getInputStream().readAllBytes());
            rag.ingest(List.of(Document.from(text, Metadata.from("source", "handbook.txt"))));
        }
        return rag.ask(q);
    }
}
