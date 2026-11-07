package com.example.langchain.standalone.openai;

import com.example.langchain.demo.StructuredOutputDemo;

/**
 * Module 6 on OpenAI: turning free text into a typed object.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.openai.StructuredOutputApp -Dexec.args="My internet has been down for two hours"
 */
public class StructuredOutputApp {

    public static void main(String[] args) {
        String complaint = OpenAiModels.argOr(args,
                "My internet has been down for two hours and nobody answers the support line.");
        System.out.println(new StructuredOutputDemo(OpenAiModels.chatModel()).extract(complaint));
    }
}
