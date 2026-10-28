from langchain_ollama import ChatOllama
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

prompt = ChatPromptTemplate.from_messages([
    ("system", "You are a Python instructor."),
    ("human", "Explain {topic} in two simple sentences.")
])

chain = prompt | model | StrOutputParser()

print("INVOKE")
print(chain.invoke({"topic": "lists"}))

print("\nSTREAM")
for chunk in chain.stream({"topic": "tuples"}):
    print(chunk, end="", flush=True)

print("\n\nBATCH")
results = chain.batch([
    {"topic": "sets"},
    {"topic": "dictionaries"},
    {"topic": "functions"}
])

for i, result in enumerate(results, start=1):
    print(f"{i}. {result}")
