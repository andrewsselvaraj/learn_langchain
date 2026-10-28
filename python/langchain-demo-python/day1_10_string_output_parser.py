from langchain_ollama import ChatOllama
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

prompt = ChatPromptTemplate.from_template(
    "Explain {topic} in one simple sentence."
)

chain_without_parser = prompt | model
response = chain_without_parser.invoke({"topic": "inheritance"})

print("Without parser:")
print(type(response))
print(response.content)

chain_with_parser = prompt | model | StrOutputParser()
result = chain_with_parser.invoke({"topic": "inheritance"})

print("\nWith StrOutputParser:")
print(type(result))
print(result)
