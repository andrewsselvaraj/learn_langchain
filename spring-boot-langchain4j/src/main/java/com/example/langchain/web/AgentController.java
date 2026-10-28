package com.example.langchain.web;

import com.example.langchain.demo.AgentDemo;
import com.example.langchain.demo.ToolsDemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Tools and agents: the model calls Java methods to answer. */
@RestController
@RequestMapping("/api")
public class AgentController {

    private final ToolsDemo tools;
    private final AgentDemo agent;

    public AgentController(ToolsDemo tools, AgentDemo agent) {
        this.tools = tools;
        this.agent = agent;
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
