package com.example.langchain.standalone.gemini;

import com.example.langchain.demo.StructuredOutputDemo;

/**
 * Module 6: turning free text into a typed object (was StructuredOutputController).
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.gemini.StructuredOutputApp -Dexec.args="My internet has been down for two hours"
 */
public class StructuredOutputApp {

    public static void main(String[] args) {
        String complaint = GeminiModels.argOr(args,
                "My internet has been down for two hours and nobody answers the support line.");
        System.out.println(new StructuredOutputDemo(GeminiModels.chatModel()).extract(complaint));
    }
}
