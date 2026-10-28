from langchain_ollama import ChatOllama

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

response1 = model.invoke("My name is Priya.")
print("First response:")
print(response1.content)

response2 = model.invoke("What is my name?")
print("\nSecond response:")
print(response2.content)

# The second call does not automatically include the first conversation.
