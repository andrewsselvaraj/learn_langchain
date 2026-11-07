
import java.util.Scanner;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

public class TicketBookingAgent {

    // Java Tools
    public static class BookingTools {

        @Tool("Check whether movie tickets are available")
        public String checkAvailability(
                @P("Movie name") String movie,
                @P("Show time") String time,
                @P("Number of tickets") int count) {

            System.out.println("\nChecking ticket availability...");

            // Demo availability check
            if (count <= 5) {
                return count + " tickets are available for "
                        + movie + " at " + time;
            } else {
                return "Sorry, only 5 tickets are available.";
            }
        }

        @Tool("Book movie tickets after customer confirmation")
        public String bookTickets(
                @P("Movie name") String movie,
                @P("Show time") String time,
                @P("Number of tickets") int count) {

            System.out.println("\nBooking tickets...");

            // Demo booking
            return "Booking successful! Booking ID: BK1001"
                    + "\nMovie: " + movie
                    + "\nShow Time: " + time
                    + "\nTickets: " + count;
        }
    }

    // AI Agent
    public interface BookingAgent {

        @SystemMessage("""
            You are a Movie Ticket Booking Assistant.

            Follow these rules:

            1. Ask the customer for the movie name if missing.
            2. Ask for the show time if missing.
            3. Ask for the number of tickets if missing.
            4. Once all details are collected, call checkAvailability().
            5. Tell the customer whether tickets are available.
            6. Ask the customer to confirm the booking.
            7. Call bookTickets() ONLY after the customer clearly confirms.
            8. Never book tickets without confirmation.
            9. Do not ask again for information already provided.
            10. Give a friendly response.
            """)
        String chat(String message);
    }

    public static void main(String[] args) {

        // OpenAI API Key
        String apiKey = System.getenv("OPENAI_API_KEY");

     

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Please set the OPENAI_API_KEY environment variable.");
            return;
        }

        // Create AI Model
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .build();

        // Create Booking Agent with memory
        BookingAgent agent = AiServices.builder(BookingAgent.class)
                .chatModel(model)
                .tools(new BookingTools())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("   MOVIE TICKET BOOKING AGENT");
        System.out.println("=================================");
        System.out.println("Type 'exit' to stop.\n");

        while (true) {

            System.out.print("Customer: ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Thank you for using our booking service!");
                break;
            }

            // Send customer message to AI Agent
            String response = agent.chat(message);

            System.out.println("\nAI Agent: " + response);
            System.out.println();
        }

        scanner.close();
    }
}
