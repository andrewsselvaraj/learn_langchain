package com.example.langchain.demo;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * Module 7. Same stages as the Python course:
 * loader -> splitter -> embeddings -> vector store -> retriever -> prompt -> model.
 * Document has text() + metadata(), like page_content + metadata.
 */
@Service
public class RagDemo {

    interface HandbookAssistant {
        @SystemMessage("Answer using only the provided context. If the answer is not there, say you do not know.")
        String answer(String question);
    }

    private final EmbeddingModel embeddingModel;
    private final ChatModel model;
    private final EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>(); // swap for Chroma, pgvector...

    public RagDemo(EmbeddingModel embeddingModel, ChatModel model) {
        this.embeddingModel = embeddingModel;
        this.model = model;
    }

    /** Load: PyPDFLoader("data/handbook.pdf").load() equivalent. Only this method knows the file format. */
    public static Document load(Path file) {
        DocumentParser parser = file.toString().endsWith(".pdf")
                ? new ApachePdfBoxDocumentParser()
                : new TextDocumentParser();
        return FileSystemDocumentLoader.loadDocument(file, parser);
    }

    /** Split + embed + store. Re-running it on a persistent store adds the content again. */
    public void ingest(List<Document> documents) {
        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50)) // chunk_size, chunk_overlap
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build()
                .ingest(documents);
    }

    /** vectorstore.as_retriever(search_kwargs={"k": 3}). A retriever finds; it never answers. */
    public ContentRetriever retriever() {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .build();
    }

    public List<Content> retrieve(String question) {
        return retriever().retrieve(Query.from(question));
    }

    /** The whole rag_chain: retrieval, prompting, generation and parsing in one call. */
    public String ask(String question) {
        HandbookAssistant assistant = AiServices.builder(HandbookAssistant.class)
                .chatModel(model)
                .contentRetriever(retriever())
                .build();
        return assistant.answer(question);
    }
}
