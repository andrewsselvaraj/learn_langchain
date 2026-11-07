package com.example.langchain.standalone.openai;

import com.example.langchain.demo.RagDemo;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Module 7 on OpenAI: retrieval-augmented generation over the sample handbook.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.openai.RagApp -Dexec.args="How many sick leave days are allowed?"
 */
public class RagApp {

    public static void main(String[] args) throws IOException {
        String question = OpenAiModels.argOr(args, "How many sick leave days are allowed?");

        RagDemo rag = new RagDemo(OpenAiModels.embeddingModel(), OpenAiModels.chatModel());
        // From a file on disk you would use RagDemo.load(Path.of("data/handbook.pdf"))
        try (InputStream in = RagApp.class.getClassLoader().getResourceAsStream("data/handbook.txt")) {
            String text = new String(in.readAllBytes());
            rag.ingest(List.of(Document.from(text, Metadata.from("source", "handbook.txt"))));
        }
        System.out.println(rag.ask(question));
    }
}
