from langchain_core.runnables import RunnableLambda, RunnablePassthrough


def log_input(text):
    # Observe the value without changing what continues through the pipeline.
    print("LOG - received:", text)


def make_uppercase(text):
    return text.upper()


chain = (
    RunnablePassthrough(func=log_input)
    | RunnableLambda(make_uppercase)
)

result = chain.invoke("hello langchain")

print("Final output:", result)
