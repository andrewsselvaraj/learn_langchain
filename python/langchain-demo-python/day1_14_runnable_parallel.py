from langchain_core.runnables import (
    RunnableLambda,
    RunnableParallel,
    RunnablePassthrough,
)


def make_uppercase(text):
    return text.upper()


def format_result(data):
    return f"Original: {data['original']} | Uppercase: {data['uppercase']}"


# The same input goes to both branches.
# Their outputs are collected into a dictionary,
# which then becomes the input to the next stage.
chain = (
    RunnableParallel(
        original=RunnablePassthrough(),
        uppercase=RunnableLambda(make_uppercase),
    )
    | RunnableLambda(format_result)
)

result = chain.invoke("hello langchain")

print("Final output:", result)
