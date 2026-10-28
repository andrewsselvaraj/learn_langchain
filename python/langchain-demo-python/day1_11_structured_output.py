from pydantic import BaseModel, Field
from langchain_ollama import ChatOllama

class SupportTicket(BaseModel):
    category: str = Field(
        description="One of: network, billing, account, device"
    )
    priority: str = Field(
        description="One of: low, medium, high"
    )
    summary: str = Field(
        description="One short sentence describing the issue"
    )

model = ChatOllama(
    model="llama3.2",
    temperature=0
)

structured_model = model.with_structured_output(SupportTicket)

result = structured_model.invoke(
    "My internet has been disconnected for two hours "
    "and I have an important customer meeting."
)

print("Complete structured result:")
print(result)

print("\nIndividual fields:")
print("Category:", result.category)
print("Priority:", result.priority)
print("Summary:", result.summary)
