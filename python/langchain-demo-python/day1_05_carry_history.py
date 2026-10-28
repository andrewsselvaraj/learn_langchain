from langchain_ollama import ChatOllama
from langchain_core.messages import HumanMessage

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

messages = [
    HumanMessage(content="My name is Priya.")
]

first = model.invoke(messages)
print("First response:")
print(first.content)

messages.append(first)
messages.append(
    HumanMessage(content="What is my name?")
)

second = model.invoke(messages)

print("\nSecond response:")
print(second.content)
