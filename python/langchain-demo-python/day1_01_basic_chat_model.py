from langchain_ollama import ChatOllama

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

response = model.invoke(
    "Explain Python lists in two sentences."
)

print(response.content)
