"""LangChain Days 1-3 practice script.

Runs offline with a fake model so you can study the *types* at each step:
    pip install -U langchain langchain-core
    python practice.py

Set USE_OLLAMA=1 (and `ollama pull llama3.2`, `pip install langchain-ollama`)
to swap in the real local model from the course. Only the model line changes.
"""
import os

from langchain_core.messages import AIMessage, HumanMessage, SystemMessage
from langchain_core.output_parsers import PydanticOutputParser, StrOutputParser
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.runnables import RunnableLambda, RunnableParallel, RunnablePassthrough
from langchain_core.tools import tool
from pydantic import BaseModel, Field



def make_model(fake_reply):
    """The real course model with USE_OLLAMA=1, otherwise a fake that always gives fake_reply."""
    if os.getenv("USE_OLLAMA"):
        from langchain_ollama import ChatOllama
        return ChatOllama(model="llama3.2", temperature=0)
    from langchain_core.language_models import FakeListChatModel
    return FakeListChatModel(responses=[fake_reply])


def section(title):
    print(f"\n=== {title} ===")


# Module 4: a template is a step, not a request (no model is called)
section("Prompt template")
prompt = ChatPromptTemplate.from_messages([
    ("system", "You are a {role}."),
    MessagesPlaceholder("history", optional=True),
    ("human", "Explain {topic} simply."),
])
filled = prompt.invoke({"role": "Python instructor", "topic": "decorators"})
print(type(filled).__name__, "->", [type(m).__name__ for m in filled.to_messages()])
try:
    prompt.invoke({"role": "teacher"})
except KeyError as e:
    print("Missing key fails before any model call:", e)

# Module 5: prompt | model | parser, and debugging a chain by its prefix
section("Chain")
model = make_model("Decorators wrap a function to add behaviour.")
chain = prompt | model | StrOutputParser()
print("prompt|model ->", type((prompt | model).invoke({"role": "teacher", "topic": "x"})).__name__)
result = chain.invoke({"role": "Python instructor", "topic": "decorators"})
print("full chain   -> str?", isinstance(result, str), ":", result)

# Module 3: memory is the list you resend
section("Messages and history")
history = [HumanMessage("My name is Priya."), AIMessage("Nice to meet you, Priya.")]
msgs = prompt.invoke({"role": "assistant", "history": history, "topic": "my name"}).to_messages()
for m in msgs:
    print(f"  {type(m).__name__:14} {m.content}")

# Module 5A: runnable utilities
section("Runnable utilities")
parallel = RunnableParallel(original=RunnablePassthrough(), upper=RunnableLambda(str.upper))
print(parallel.invoke("hello"))
grade = RunnablePassthrough.assign(result=lambda d: "Pass" if d["score"] >= 50 else "Fail")
print(grade.invoke({"name": "Anu", "score": 82}))

# Module 6: structured output via the parser fallback (works with any model)
section("Structured output")


class SupportTicket(BaseModel):
    category: str = Field(description="one of: billing, technical, account")
    priority: str = Field(description="low, medium, or high")
    summary: str = Field(description="one sentence, under twenty words")


parser = PydanticOutputParser(pydantic_object=SupportTicket)
ticket_prompt = ChatPromptTemplate.from_messages([
    ("system", "Answer using this format.\n{format_instructions}"),
    ("human", "{complaint}"),
]).partial(format_instructions=parser.get_format_instructions())
model = make_model('{"category": "technical", "priority": "high", "summary": "Internet down for two hours"}')
ticket = (ticket_prompt | model | parser).invoke(
    {"complaint": "My internet has been down for two hours and I have a customer meeting."})
print(type(ticket).__name__, ticket.priority, "|", ticket.summary)
# With a model that supports it: model.with_structured_output(SupportTicket).invoke("...")

# Module 7: RAG chain shape, with a stand-in retriever (no vector store needed)
section("RAG chain shape")
from langchain_core.documents import Document

docs = [Document("Employees receive 10 sick-leave days per year.", metadata={"source": "handbook.pdf", "page": 3})]
retriever = RunnableLambda(lambda q: docs)  # real code: vectorstore.as_retriever(search_kwargs={"k": 3})


def format_docs(ds):
    return "\n\n".join(d.page_content for d in ds)


rag_prompt = ChatPromptTemplate.from_template(
    "Answer using only the context below. If the answer is not there, say you do not know.\n\n"
    "Context:\n{context}\n\nQuestion: {question}")
model = make_model("Employees get 10 sick-leave days per year.")
rag_chain = (
    {"context": retriever | RunnableLambda(format_docs), "question": RunnablePassthrough()}
    | rag_prompt | model | StrOutputParser()
)
print(rag_chain.invoke("How many sick leave days are allowed?"))

# Tools: the decorator turns name, docstring and annotations into the tool's schema
section("Tools")


@tool
def get_flight_status(flight_no: str) -> str:
    """Get the current status and arrival time of a flight."""
    if flight_no.upper() == "EK524":
        return "EK524 is on time and is expected to arrive at 8:10 PM."
    return "Flight not found."


print(get_flight_status.name, "|", get_flight_status.description, "|", get_flight_status.args)
print(get_flight_status.invoke({"flight_no": "ek524"}))

# Agents need a real tool-calling model, so this part runs only with Ollama
if os.getenv("USE_OLLAMA"):
    section("Agent")
    from langchain.agents import create_agent
    from langgraph.checkpoint.memory import InMemorySaver

    agent = create_agent(model=make_model(""), tools=[get_flight_status],
                         system_prompt="You are a helpful airline assistant. Use the tools when needed.",
                         checkpointer=InMemorySaver())
    config = {"configurable": {"thread_id": "session1"}}
    for q in ["Hi!", "Is flight EK524 on time?", "What time will it arrive?"]:
        out = agent.invoke({"messages": [{"role": "user", "content": q}]}, config=config)
        print(f"> {q}\n{out['messages'][-1].content}")
