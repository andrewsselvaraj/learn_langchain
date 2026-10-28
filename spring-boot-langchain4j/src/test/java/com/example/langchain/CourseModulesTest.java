package com.example.langchain;

import com.example.langchain.FakeModels.ScriptedChatModel;
import com.example.langchain.FakeModels.WordEmbeddingModel;
import com.example.langchain.demo.AgentDemo;
import com.example.langchain.demo.CompositionDemo;
import com.example.langchain.demo.FlightTools;
import com.example.langchain.demo.MessagesDemo;
import com.example.langchain.demo.PromptsDemo;
import com.example.langchain.demo.RagDemo;
import com.example.langchain.demo.StructuredOutputDemo;
import com.example.langchain.demo.StructuredOutputDemo.SupportTicket;
import com.example.langchain.demo.ToolsDemo;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.content.Content;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Each test mirrors one idea from the course, running offline against fake models. */
class CourseModulesTest {

    private final ScriptedChatModel model = new ScriptedChatModel();

    @Test
    void module3_modelRemembersOnlyWhatYouResend() {
        MessagesDemo demo = new MessagesDemo(model);
        assertThat(demo.twoTurnsByHand()).isEqualTo("Your name is Priya.");
        assertThat(demo.twoTurnsWithMemory()).isEqualTo("Your name is Priya.");

        // Two independent calls: the second knows nothing
        model.chat("My name is Priya.");
        assertThat(model.chat("What is my name?")).isEqualTo("I do not know your name.");
    }

    @Test
    void module4_templateFillsBlanksWithoutCallingAModel() {
        List<ChatMessage> filled = new PromptsDemo().fill("Python instructor", "decorators");
        assertThat(filled.get(0)).isInstanceOf(SystemMessage.class);
        assertThat(((SystemMessage) filled.get(0)).text()).isEqualTo("You are a Python instructor.");
        assertThat(((UserMessage) filled.get(1)).singleText()).isEqualTo("Explain decorators simply.");
        assertThat(model.requests).isEmpty();

        List<ChatMessage> history = List.of(UserMessage.from("Hi"), AiMessage.from("Hello"));
        assertThat(new PromptsDemo().fillWithHistory("teacher", history, "generators")).hasSize(4);
    }

    @Test
    void module4_missingKeyFailsBeforeAnyModelCall() {
        assertThatThrownBy(() -> dev.langchain4j.model.input.PromptTemplate.from("Explain {{topic}}")
                .apply(Map.of("role", "teacher")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void module5_chainTypesAtEachBoundary() {
        CompositionDemo demo = new CompositionDemo(model, new PromptsDemo());
        Map<String, String> inputs = Map.of("role", "teacher", "topic", "lists");

        assertThat(demo.promptAndModelOnly(inputs)).isInstanceOf(AiMessage.class);  // prefix of the chain
        assertThat(demo.chain().apply(inputs)).isEqualTo("Echo: Explain lists simply."); // a String
        assertThat(demo.batch(List.of(inputs, Map.of("role", "teacher", "topic", "sets")))).hasSize(2);
        assertThat(demo.explainWithAiService("teacher", "tuples")).isEqualTo("Echo: Explain tuples simply.");
    }

    @Test
    void module5A_runnableUtilities() {
        assertThat(CompositionDemo.parallel("hello"))
                .containsEntry("original", "hello").containsEntry("uppercase", "HELLO");
        assertThat(CompositionDemo.assignResult(Map.of("name", "Anu", "score", 82)))
                .containsEntry("name", "Anu").containsEntry("result", "Pass");
        assertThat(CompositionDemo.passthroughLogging().apply("LangChain")).isEqualTo("LangChain");

        new CompositionDemo(model, new PromptsDemo()).withTemperature("tagline please", 0.8);
        assertThat(model.requests.get(0).parameters().temperature()).isEqualTo(0.8);
    }

    @Test
    void module6_structuredOutputReturnsATypedObject() {
        SupportTicket ticket = new StructuredOutputDemo(model)
                .extract("My internet has been down for two hours and I have a customer meeting.");
        assertThat(ticket.priority()).isEqualTo("high");
        assertThat(ticket.category()).isEqualTo("technical");
    }

    @Test
    void module7_retrieverFindsAndTheModelAnswersFromContext() {
        RagDemo rag = new RagDemo(new WordEmbeddingModel(), model);
        rag.ingest(List.of(
                Document.from("Employees receive 10 sick-leave days per year.",
                        Metadata.from("source", "handbook.pdf").put("page", 3)),
                Document.from("The office cafeteria opens at eight in the morning.",
                        Metadata.from("source", "handbook.pdf").put("page", 9))));

        List<Content> found = rag.retrieve("How many sick-leave days do employees receive?");
        assertThat(found.get(0).textSegment().text()).contains("10 sick-leave days");
        assertThat(found.get(0).textSegment().metadata().getString("source")).isEqualTo("handbook.pdf");

        rag.ask("How many sick-leave days do employees receive?");
        String whatTheModelSaw = model.requests.get(model.requests.size() - 1).messages().toString();
        assertThat(whatTheModelSaw).contains("10 sick-leave days").contains("only the provided context");
    }

    @Test
    void tools_metadataComesFromAnnotations() {
        ToolsDemo demo = new ToolsDemo(model, new FlightTools());
        List<ToolSpecification> specs = demo.describeTools();
        assertThat(specs).extracting(ToolSpecification::name)
                .containsExactlyInAnyOrder("getFlightStatus", "getBaggageAllowance");
        assertThat(specs).extracting(ToolSpecification::description)
                .contains("Get the current status and arrival time of a flight.");

        // Your code calls the tool once; the prompt narrows it three ways
        assertThat(demo.askAboutFlight("EK524")).containsKeys("status", "arrival", "gate");
        assertThat(model.requests).hasSize(3);
    }

    @Test
    void agents_modelChoosesTheToolAndMemoryFollowsTheThread() {
        AgentDemo agent = new AgentDemo(model, new FlightTools());

        assertThat(agent.chat("session1", "Hi!")).isEqualTo("Echo: Hi!");                 // no tool
        assertThat(agent.chat("session1", "Is flight EK524 on time?")).contains("on time"); // first tool
        assertThat(agent.chat("session1", "What time will it arrive?")).contains("8:10 PM"); // memory
        assertThat(agent.chat("session1", "Baggage allowance for economy?")).contains("25 kg"); // second tool

        // A different thread_id starts a fresh conversation
        assertThat(agent.chat("session2", "What time will it arrive?")).startsWith("Echo:");
    }
}
