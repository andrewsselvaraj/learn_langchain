package com.example.langchain.standalone.test.langgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * LangGraph hello world with LangGraph4j (the Java port of LangGraph). No LLM, so no API key is needed.
 * The three LangGraph ideas: a shared State, Nodes that return updates to it, and Edges that pick the next node.
 *
 *   START -> greet -> (router) -> formal | casual -> END
 *
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.test.langgraph.LangGraphHelloWorld
 */
public class LangGraphHelloWorld {

    /** The State: a typed view over a Map, like a TypedDict in Python LangGraph. */
    static class HelloState extends AgentState {

        /** Reducers: "messages" appends what nodes return; keys without a channel are overwritten. */
        static final Map<String, Channel<?>> SCHEMA = Map.of(
                "messages", Channels.appender(ArrayList::new));

        HelloState(Map<String, Object> data) {
            super(data);
        }

        String name() {
            return this.<String>value("name").orElse("World");
        }

        String style() {
            return this.<String>value("style").orElse("casual");
        }

        List<String> messages() {
            return this.<List<String>>value("messages").orElse(List.of());
        }
    }

    public static void main(String[] args) throws Exception {
        // Nodes: read the state, return only the keys they change
        var graph = new StateGraph<>(HelloState.SCHEMA, HelloState::new)
                .addNode("greet", node_async(state -> Map.of("messages", "Hello, " + state.name() + "!")))
                .addNode("formal", node_async(state -> Map.of("messages", "It is a pleasure to meet you.")))
                .addNode("casual", node_async(state -> Map.of("messages", "Nice to see you :)")))

                // Edges: fixed ones always go to the same node, conditional ones ask a router function
                .addEdge(START, "greet")
                .addConditionalEdges("greet",
                        edge_async(HelloState::style),             // router returns a label...
                        Map.of("formal", "formal", "casual", "casual")) // ...mapped to a node name
                .addEdge("formal", END)
                .addEdge("casual", END);

        CompiledGraph<HelloState> app = graph.compile();
        RunnableConfig config = RunnableConfig.empty(); // would carry a thread id once you add memory

        // invoke(): run to the end and get the final state
        HelloState result = app.invoke(GraphInput.args(Map.of("name", "Andrews", "style", "formal")), config)
                .orElseThrow();
        System.out.println("invoke -> " + result.messages());

        // stream(): see the state after every node, which is how you debug a graph
        System.out.println("\nstream:");
        for (NodeOutput<HelloState> step : app.stream(GraphInput.args(Map.of("name", "LangGraph")), config)) {
            System.out.println("  after " + step.node() + " -> " + step.state().messages());
        }
    }
}
