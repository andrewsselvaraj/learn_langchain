package com.example.langchain.demo;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * Tools. Python's @tool reads the docstring and type hints;
 * here @Tool("...") is the description and @P describes each argument.
 */
@Component
public class FlightTools {

    @Tool("Get the current status and arrival time of a flight.")
    public String getFlightStatus(@P("The flight number, for example EK524") String flightNo) {
        // In production, replace this stub with a real API call
        if (flightNo.equalsIgnoreCase("EK524")) {
            return "EK524 departed at 5:20 PM. It is on time and expected to arrive at 8:10 PM at Gate B12.";
        }
        return "Flight not found.";
    }

    @Tool("Get the baggage allowance for a travel class.")
    public String getBaggageAllowance(@P("economy or business") String travelClass) {
        return switch (travelClass.toLowerCase()) {
            case "economy" -> "Economy passengers can check in 25 kg.";
            case "business" -> "Business passengers can check in 35 kg.";
            default -> "Travel class not found.";
        };
    }
}
