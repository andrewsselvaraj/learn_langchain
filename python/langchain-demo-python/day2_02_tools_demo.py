"""
02_tools_demo.py

Goal:
- Create a custom LangChain tool
- Inspect the tool metadata
- Invoke the tool manually
- Feed the tool result into a normal LCEL chain

Prerequisite:
    ollama pull llama3.2
"""

from langchain.tools import tool
from langchain_core.prompts import PromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_ollama import ChatOllama


# 1. Define a custom tool
@tool
def get_flight_status(flight_no: str) -> str:
    """Get the current status and schedule of a flight."""
    if flight_no.upper() == "EK524":
        return (
            "Flight EK524 departed at 5:20 PM. "
            "It is on time and is expected to arrive at 8:10 PM at Gate B12."
        )
    return "Flight information not found."


# 2. Inspect what @tool registered
print("\n--- Tool metadata ---")
print("Name       :", get_flight_status.name)
print("Description:", get_flight_status.description)
print("Arguments  :", get_flight_status.args)


# 3. Invoke the tool manually.
# Important: our Python code is choosing the tool here. This is NOT an agent yet.
context = get_flight_status.invoke({"flight_no": "EK524"})

print("\n--- Tool result ---")
print(context)


# 4. Use the tool result as context in a normal chain
prompt = PromptTemplate.from_template(
    """Based only on this context:
{context}

Answer this query briefly:
{query}
"""
)

model = ChatOllama(model="llama3.2", temperature=0)
chain = prompt | model | StrOutputParser()

queries = [
    "What is the flight status?",
    "What is the departure time?",
    "What is the arrival time?",
    "Which gate will it arrive at?",
]

print("\n--- Asking different questions about the same tool result ---")
for query in queries:
    answer = chain.invoke({"context": context, "query": query})
    print(f"\nQuestion: {query}")
    print(f"Answer  : {answer}")

print(
    "\nNOTE: The tool was selected and invoked by our Python code. "
    "The model did not choose the tool. That is what the Agent demo adds."
)
