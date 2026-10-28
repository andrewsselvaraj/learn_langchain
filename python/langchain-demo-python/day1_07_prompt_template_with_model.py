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

prompt_value = prompt.invoke({
    "role": "Python instructor",
    "topic": "generators"
})

response = model.invoke(prompt_value)

print(response.content)
