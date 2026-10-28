from langchain_core.prompts import ChatPromptTemplate

prompt = ChatPromptTemplate.from_messages([
    ("system", "You are a {role}."),
    ("human", "Explain {topic} simply in two sentences.")
])

prompt_value = prompt.invoke({
    "role": "Python instructor",
    "topic": "generators"
})

print("Prompt value:")
print(prompt_value)

print("\nMessages created by the template:")
print(prompt_value.to_messages())
