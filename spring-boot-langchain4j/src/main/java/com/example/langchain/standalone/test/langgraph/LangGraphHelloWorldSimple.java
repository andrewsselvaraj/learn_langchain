package com.example.langchain.standalone.test.langgraph;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.NodeAction;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import org.bsc.langgraph4j.state.AgentState;

/**
 * The smallest LangGraph4j program: a state, two nodes, and the edges that connect them. No LLM needed.
 *
 *   START -> greet -> shout -> END
 *
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.test.langgraph.LangGraphHelloWorldSimple
 */
public class LangGraphHelloWorldSimple {

    public static void main(String[] args) throws Exception {
        // Each node reads the state and returns the keys it wants to update
        NodeAction<AgentState> greetAction = state -> Map.of("text", "Hello, " + state.value("name").orElse("World"));
        AsyncNodeAction<AgentState> greet = node_async(greetAction);
        NodeAction<AgentState> shoutAction = state -> Map.of("text", state.value("text").orElse("") + "!!!");
        AsyncNodeAction<AgentState> shout = node_async(shoutAction);

        var app = new StateGraph<>(AgentState::new)
                .addNode("greet", greet)
                .addNode("shout", shout)
                .addEdge(START, "greet")
                .addEdge("greet", "shout")
                .addEdge("shout", END)
                .compile();

        GraphInput input = GraphInput.args(Map.of("name", "Andrews"));
        RunnableConfig config = RunnableConfig.empty();

        var result = app.invoke(input, config).orElseThrow();
        System.out.println(result.value("text").orElse(""));   // Hello, Andrews!!!
    }
}
