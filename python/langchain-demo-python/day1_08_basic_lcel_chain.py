from langchain_ollama import ChatOllama
from langchain_core.prompts import ChatPromptTemplate

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

prompt = ChatPromptTemplate.from_messages([
    ("system", "You are a {role}."),
    ("human", "Explain {topic} simply in two sentences.")
])

chain = prompt | model

response = chain.invoke({
    "role": "Python instructor",
    "topic": "dictionaries"
})

print(response.content)
