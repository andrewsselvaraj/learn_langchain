from langchain_core.runnables import RunnableLambda


def clean_text(text):
    return text.strip()


def make_uppercase(text):
    return text.upper()


# Both normal Python functions are adapted into Runnable stages.
chain = (
    RunnableLambda(clean_text)
    | RunnableLambda(make_uppercase)
)

result = chain.invoke("   hello langchain   ")

print("Final output:", result)
