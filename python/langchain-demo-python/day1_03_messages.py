from langchain_ollama import ChatOllama
from langchain_core.messages import SystemMessage, HumanMessage

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

messages = [
    SystemMessage(content="You are a Python instructor. Explain concepts simply."),
    HumanMessage(content="Explain decorators in two sentences.")
]

response = model.invoke(messages)

print(response.content)
