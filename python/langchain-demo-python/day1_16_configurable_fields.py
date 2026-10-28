from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import ConfigurableField
from langchain_ollama import ChatOllama


prompt_template = ChatPromptTemplate.from_messages([
    ("system", "You are a concise creative writer."),
    ("human", "Give me one short tagline for {business}."),
])

model = ChatOllama(
    model="llama3.2",
    temperature=0,
).configurable_fields(
    temperature=ConfigurableField(
        id="temperature",
        name="Model Temperature",
        description="Controls randomness of the model output",
    )
)

chain = (
    prompt_template
    | model
    | StrOutputParser()
)

print("Default temperature = 0")
result1 = chain.invoke({"business": "a coffee shop"})
print(result1)

print("\nRuntime temperature = 0.8")
result2 = chain.with_config(
    configurable={"temperature": 0.8}
).invoke({"business": "a coffee shop"})
print(result2)
