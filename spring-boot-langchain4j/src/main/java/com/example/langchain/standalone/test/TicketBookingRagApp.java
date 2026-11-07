package com.example.langchain.standalone.test;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.util.List;
import java.util.Scanner;

/**
 * RAG over a cinema's ticket booking policies: a customer-support bot that answers only from the policy text.
 * Same stages as RagDemo: loader -> splitter -> embeddings -> vector store -> retriever -> prompt -> model.
 * Run: mvn -q compile exec:java -Dexec.mainClass=com.example.langchain.standalone.test.TicketBookingRagApp
 */
public class TicketBookingRagApp {

    /** The AI Service: the system message is the prompt, the retriever injects the context. */
    interface BookingSupportAssistant {
        @SystemMessage("""
                You are a customer-support assistant for CineStar Cinemas.
                Answer using only the provided context. If the answer is not there, say you do not know
                and suggest contacting support@cinestar.example.
                Keep answers short and mention prices or time limits exactly as written.
                """)
        String answer(String question);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Please set the OPENAI_API_KEY environment variable.");
            return;
        }

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .build();
        // Use the same embedding model for indexing and for queries
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-3-small")
                .build();
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>(); // swap for Chroma, pgvector...

        // 1. Load: in a real app these would come from FileSystemDocumentLoader (PDF, txt...)
        List<Document> documents = loadPolicies();

        // 2-4. Split + embed + store
        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30)) // chunk_size, chunk_overlap
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build()
                .ingest(documents);
        System.out.println("Indexed " + documents.size() + " policy documents.");

        // 5. Retriever: finds the 3 most similar chunks; it never answers
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)   // drop chunks that are only weakly related
                .build();

        // 6-7. Prompt + model, with memory so follow-up questions like "and for kids?" work
        BookingSupportAssistant assistant = AiServices.builder(BookingSupportAssistant.class)
                .chatModel(chatModel)
                .contentRetriever(retriever)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        System.out.println("=================================");
        System.out.println("   CINESTAR BOOKING SUPPORT (RAG)");
        System.out.println("=================================");
        System.out.println("Try: Can I cancel my ticket?  |  How much is a recliner seat?  |  Is there parking?");
        System.out.println("Type 'exit' to stop.\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Customer: ");
            String question = scanner.nextLine();
            if (question.equalsIgnoreCase("exit")) {
                break;
            }

            // Not needed for the answer: shows what the retriever hands to the model
            printRetrieved(retriever, question);

            System.out.println("\nAssistant: " + assistant.answer(question) + "\n");
        }
        scanner.close();
    }

    /** Document = text() + metadata(), like page_content + metadata. One document per policy section. */
    private static List<Document> loadPolicies() {
        return List.of(
                policy("cancellation", """
                        Cancellation and refunds: Tickets can be cancelled up to 2 hours before the show time
                        from the My Bookings page. Cancellations more than 24 hours before the show get a 100% refund.
                        Cancellations between 24 hours and 2 hours before the show get a 50% refund.
                        No cancellation is possible within 2 hours of the show. Refunds reach the original
                        payment method in 5-7 working days. A convenience fee of Rs 30 per ticket is never refunded.
                        """),
                policy("pricing", """
                        Ticket prices: Regular seats cost Rs 180 on weekdays and Rs 220 on weekends.
                        Premium seats cost Rs 280 on weekdays and Rs 330 on weekends.
                        Recliner seats cost Rs 450 on all days and include a free blanket.
                        IMAX and 3D shows have a surcharge of Rs 100 per ticket. 3D glasses are included.
                        """),
                policy("discounts", """
                        Discounts: Children under 3 years watch free without a separate seat.
                        Children aged 3 to 12 get 20% off regular and premium seats.
                        Senior citizens above 60 get 25% off on weekday shows with a valid ID.
                        Tuesday is Cine Tuesday: all regular seats are Rs 120.
                        Discounts cannot be combined with each other.
                        """),
                policy("booking-limits", """
                        Booking rules: A single booking can have at most 10 tickets.
                        Seats are held for 8 minutes while you complete payment, after which they are released.
                        Accepted payment methods are UPI, credit cards, debit cards and CineStar wallet.
                        The e-ticket QR code is sent by SMS and email and must be shown at the entrance.
                        """),
                policy("theatre-rules", """
                        Theatre rules: Entry opens 20 minutes before the show. Latecomers are allowed in
                        up to 15 minutes after the show starts. Outside food and drinks are not allowed.
                        Films rated A require a government photo ID showing age 18 or above.
                        Free parking is available for 4 hours with a valid ticket for the same day.
                        """));
    }

    private static Document policy(String topic, String text) {
        return Document.from(text, Metadata.from("source", "cinestar-policies").put("topic", topic));
    }

    private static void printRetrieved(ContentRetriever retriever, String question) {
        List<Content> contents = retriever.retrieve(Query.from(question));
        System.out.println("  [retrieved " + contents.size() + " chunk(s)]");
        for (Content content : contents) {
            TextSegment segment = content.textSegment();
            String preview = segment.text().replaceAll("\\s+", " ").strip();
            System.out.println("   - (" + segment.metadata().getString("topic") + ") "
                    + preview.substring(0, Math.min(80, preview.length())) + "...");
        }
    }
}
