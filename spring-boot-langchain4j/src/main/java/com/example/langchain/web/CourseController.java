package com.example.langchain.web;

import com.example.langchain.demo.AgentDemo;
import com.example.langchain.demo.CompositionDemo;
import com.example.langchain.demo.MessagesDemo;
import com.example.langchain.demo.ModelsDemo;
import com.example.langchain.demo.PromptsDemo;
import com.example.langchain.demo.RagDemo;
import com.example.langchain.demo.StructuredOutputDemo;
import com.example.langchain.demo.StructuredOutputDemo.SupportTicket;
import com.example.langchain.demo.ToolsDemo;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** One endpoint per course module. Try them with curl once GEMINI_API_KEY is set. */
@RestController
@RequestMapping("/api")
public class CourseController {

    private final ModelsDemo models;
    private final MessagesDemo messages;
    private final PromptsDemo prompts;
    private final CompositionDemo composition;
    private final StructuredOutputDemo structured;
    private final RagDemo rag;
    private final ToolsDemo tools;
    private final AgentDemo agent;
    private final StreamingChatModel streamingModel;

    public CourseController(ModelsDemo models, MessagesDemo messages, PromptsDemo prompts,
                            CompositionDemo composition, StructuredOutputDemo structured, RagDemo rag,
                            ToolsDemo tools, AgentDemo agent, StreamingChatModel streamingModel) {
        this.models = models;
        this.messages = messages;
        this.prompts = prompts;
        this.composition = composition;
        this.structured = structured;
        this.rag = rag;
        this.tools = tools;
        this.agent = agent;
        this.streamingModel = streamingModel;
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

    // Module 5: GET /api/chain?role=teacher&topic=lists
    @GetMapping("/chain")
    public String chain(@RequestParam String role, @RequestParam String topic) {
        return composition.chain().apply(Map.of("role", role, "topic", topic));
    }

    // Module 5: GET /api/stream?q=Explain tuples   (tokens arrive as server-sent events)
    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam String q) {
        SseEmitter emitter = new SseEmitter();
        streamingModel.chat(q, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });
        return emitter;
    }

    // Module 6: POST /api/ticket  body: My internet has been down for two hours...
    @PostMapping("/ticket")
    public SupportTicket ticket(@RequestBody String complaint) {
        return structured.extract(complaint);
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

    // Tools: GET /api/tools/flight?no=EK524
    @GetMapping("/tools/flight")
    public Map<String, String> flight(@RequestParam("no") String flightNo) {
        return tools.askAboutFlight(flightNo);
    }

    // Agents: GET /api/agent?thread=session1&q=Is flight EK524 on time?
    @GetMapping("/agent")
    public String agent(@RequestParam String thread, @RequestParam String q) {
        return agent.chat(thread, q);
    }
}
