"""
03_agent_demo.py

Goal:
- Create two local custom tools
- Create a LangChain agent using Ollama
- Let the model choose whether a tool is needed
- Let the model choose between two tools
- Preserve short-term conversation state with a checkpointer

Prerequisite:
    ollama pull llama3.2
"""

from langchain.agents import create_agent
from langchain.tools import tool
from langchain_ollama import ChatOllama
from langgraph.checkpoint.memory import InMemorySaver


# 1. Define two capabilities
@tool
def get_flight_status(flight_no: str) -> str:
    """Get the current status and arrival time of a flight."""
    if flight_no.upper() == "EK524":
        return "EK524 is on time and is expected to arrive at 8:10 PM."
    return "Flight not found."


@tool
def get_baggage_allowance(travel_class: str) -> str:
    """Get the baggage allowance for a travel class."""
    if travel_class.lower() == "economy":
        return "Economy passengers can check in 25 kg."
    if travel_class.lower() == "business":
        return "Business passengers can check in 35 kg."
    return "Travel class not found."


# 2. Create the local model and checkpointer
model = ChatOllama(model="llama3.2", temperature=0)
checkpointer = InMemorySaver()


# 3. Create the agent
agent = create_agent(
    model=model,
    tools=[get_flight_status, get_baggage_allowance],
    system_prompt=(
        "You are a helpful airline assistant. "
        "For greetings and general conversation, respond normally without using tools"
        "Use the available tools when they are needed. "
        "Do not claim to perform actions for which no tool is available."
    ),
    checkpointer=checkpointer,
)


# 4. Same thread_id = same conversation
config = {"configurable": {"thread_id": "session1"}}


def ask(question: str):
    """Invoke the agent and print the final response."""
    result = agent.invoke(
        {"messages": [{"role": "user", "content": question}]},
        config=config,
    )
    print(f"\nUSER : {question}")
    print("AGENT:", result["messages"][-1].content)


# 5. Four turns showing four behaviours
ask("Hi!")
# No tool should be needed.

ask("Is flight EK524 on time?")
# The model should choose get_flight_status.

ask("What tme will it arrive?")
# Same thread_id: the agent can understand that "it" means EK524.

ask("What is the baggage allowance for economy?")
# The model should choose get_baggage_allowance.


# 6. Missing capability example
ask("Book me a seat on EK524.")
# There is no booking tool, so the agent should not claim to complete a booking.
