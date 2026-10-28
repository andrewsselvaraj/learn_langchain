"""
01_retriever_demo.py

Goal:
- Create LangChain Document objects
- Store them in Chroma using local Ollama embeddings
- Convert the vector store into a Retriever
- Retrieve relevant Documents
- Build a small LCEL RAG chain

Prerequisites:
    ollama pull llama3.2
    ollama pull nomic-embed-text
"""

from langchain_core.documents import Document
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnableLambda, RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_chroma import Chroma


# 1. Create a few sample Documents
documents = [
    Document(
        page_content="Employees receive 10 paid sick-leave days per year.",
        metadata={"source": "employee_handbook", "section": "sick_leave"},
    ),
    Document(
        page_content="Economy-class passengers can check in up to 25 kg of baggage.",
        metadata={"source": "travel_policy", "section": "baggage"},
    ),
    Document(
        page_content="Employees may work from home up to two days per week with manager approval.",
        metadata={"source": "employee_handbook", "section": "remote_work"},
    ),
]


# 2. Create embeddings and a Chroma vector store.
embeddings = OllamaEmbeddings(model="nomic-embed-text")

vectorstore = Chroma.from_documents(
    documents=documents,
    embedding=embeddings,
    collection_name="langchain_retriever_demo",
    persist_directory="./chroma_db"
)


# 3. Convert the vector store into a Retriever
retriever = vectorstore.as_retriever(search_kwargs={"k": 2})

question = "How many sick leave days do employees get?"

print("\n--- Retriever output ---")
retrieved_docs = retriever.invoke(question)

for i, doc in enumerate(retrieved_docs, start=1):
    print(f"\nDocument {i}")
    print("Content :", doc.page_content)
    print("Metadata:", doc.metadata)


# 4. Format retrieved Documents into prompt context
def format_docs(docs):
    return "\n\n".join(doc.page_content for doc in docs)


prompt = ChatPromptTemplate.from_template(
    """Answer using only the context below.
If the answer is not present, say \"I do not know.\"

Context:
{context}

Question:
{question}
"""
)

model = ChatOllama(model="llama3.2", temperature=0)


# 5. Compose the RAG chain
rag_chain = (
    {
        "context": retriever | RunnableLambda(format_docs),
        "question": RunnablePassthrough(),
    }
    | prompt
    | model
    | StrOutputParser()
)

print("\n--- Final RAG answer ---")
answer = rag_chain.invoke(question)
print(answer)
