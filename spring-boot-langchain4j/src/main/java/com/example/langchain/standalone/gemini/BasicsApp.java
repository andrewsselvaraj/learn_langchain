package com.example.langchain.standalone.gemini;

import com.example.langchain.demo.MessagesDemo;
import com.example.langchain.demo.ModelsDemo;
import com.example.langchain.demo.PromptsDemo;
import dev.langchain4j.model.chat.ChatModel;

/**
 * Modules 2-4: models, messages and prompt templates (was BasicsController).
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.gemini.BasicsApp -Dexec.args="Explain RAG in two lines."
 */
public class BasicsApp {

    public static void main(String[] args) {
        String question = GeminiModels.argOr(args, "Explain RAG in two lines.");

        // Module 4 first: filling a template calls no model
        System.out.println("== Module 4: prompts");
        new PromptsDemo().fill("Python instructor", "decorators").forEach(System.out::println);

        ChatModel model = GeminiModels.chatModel();

        System.out.println("\n== Module 2: models");
        new ModelsDemo(model).anatomy(question).forEach((k, v) -> System.out.println(k + ": " + v));

        System.out.println("\n== Module 3: messages / memory");
        System.out.println(new MessagesDemo(model).twoTurnsByHand());
    }
}
