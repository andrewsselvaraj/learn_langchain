from langchain_ollama import ChatOllama

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

response = model.invoke(
    "Explain Python tuples in two sentences."
)

print("Type of response:")
print(type(response))

print("\nActual answer:")
print(response.content)

print("\nComplete response object:")
print(response)

print("\nResponse metadata:")
print(response.response_metadata)
