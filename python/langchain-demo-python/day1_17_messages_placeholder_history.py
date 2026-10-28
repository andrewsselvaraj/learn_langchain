from langchain_ollama import ChatOllama
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.messages import HumanMessage


model = ChatOllama(
    model="llama3.2",
    temperature=0
)

# The history placeholder reserves a position
# where earlier message objects will be inserted.
prompt_template = ChatPromptTemplate.from_messages([
    ("system", "You are a concise and helpful assistant."),
    MessagesPlaceholder("history"),
    ("human", "{question}")
])

chain = prompt_template | model


# Start with no conversation history.
history = []

# ----- First turn -----
question1 = "My name is Priya."

response1 = chain.invoke({
    "history": history,
    "question": question1
})

print("User:", question1)
print("Assistant:", response1.content)


# Store the first turn in history.
history.append(HumanMessage(content=question1))
history.append(response1)   # response1 is already an AIMessage


# ----- Second turn -----
question2 = "What is my name?"

response2 = chain.invoke({
    "history": history,
    "question": question2
})

print("\nUser:", question2)
print("Assistant:", response2.content)
