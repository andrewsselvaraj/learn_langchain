package com.example.langchain.demo;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Modules 5 and 5A. Java has no `|` operator, so there are two ways to get the same effect:
 *  1. Plain java.util.function composition, which keeps the types at every boundary visible.
 *  2. AI Services, LangChain4j's idiomatic replacement for `prompt | model | parser`.
 */
@Service
public class CompositionDemo {

    /** The AI Service: the annotations are the prompt template, the return type is the parser. */
    public interface Teacher {
        @SystemMessage("You are a {{role}}.")
        @UserMessage("Explain {{topic}} simply.")
        String explain(@V("role") String role, @V("topic") String topic);
    }

    private final ChatModel model;
    private final PromptsDemo prompts;
    private final Teacher teacher;

    public CompositionDemo(ChatModel model, PromptsDemo prompts) {
        this.model = model;
        this.prompts = prompts;
        this.teacher = AiServices.create(Teacher.class, model);
    }

    // ---- 1. prompt | model | parser, spelled with Function.andThen ----------------------------

    /** dict -> filled messages */
    Function<Map<String, String>, List<ChatMessage>> promptStep() {
        return in -> prompts.fill(in.get("role"), in.get("topic"));
    }

    /** messages -> AiMessage */
    Function<List<ChatMessage>, AiMessage> modelStep() {
        return messages -> model.chat(messages).aiMessage();
    }

    /** AiMessage -> String (StrOutputParser) */
    Function<AiMessage, String> parserStep() {
        return AiMessage::text;
    }

    public Function<Map<String, String>, String> chain() {
        return promptStep().andThen(modelStep()).andThen(parserStep());
    }

    /** Debug a chain by running a prefix of it, like (prompt | model).invoke(inputs). */
    public AiMessage promptAndModelOnly(Map<String, String> inputs) {
        return promptStep().andThen(modelStep()).apply(inputs);
    }

    /** batch(): independent inputs through the same chain; statelessness still holds. */
    public List<String> batch(List<Map<String, String>> inputs) {
        return inputs.parallelStream().map(chain()).toList();
    }

    // ---- 2. The idiomatic way ----------------------------------------------------------------

    public String explainWithAiService(String role, String topic) {
        return teacher.explain(role, topic);
    }

    // ---- 5A. Runnable utilities, in plain Java ------------------------------------------------

    /** RunnableLambda: any Function is already a pipeline step. */
    public static final Function<String, String> UPPERCASE = String::toUpperCase;

    /** RunnablePassthrough: Function.identity(), optionally with a side effect for logging. */
    public static Function<String, String> passthroughLogging() {
        return text -> {
            System.out.println("Length of input: " + text.length());
            return text;
        };
    }

    /** RunnableParallel: every branch gets the same input; results collected under names. */
    public static Map<String, String> parallel(String input) {
        return Map.of(
                "original", Function.<String>identity().apply(input),
                "uppercase", UPPERCASE.apply(input));
    }

    /** RunnablePassthrough.assign: keep the dict, add one computed field. */
    public static Map<String, Object> assignResult(Map<String, Object> data) {
        Map<String, Object> out = new HashMap<>(data);
        out.put("result", ((Integer) data.get("score")) >= 50 ? "Pass" : "Fail");
        return out;
    }

    /** Configurable fields: same model bean, a different temperature for this one call. */
    public String withTemperature(String question, double temperature) {
        ChatRequest request = ChatRequest.builder()
                .messages(dev.langchain4j.data.message.UserMessage.from(question))
                .temperature(temperature)
                .build();
        return model.chat(request).aiMessage().text();
    }
}
