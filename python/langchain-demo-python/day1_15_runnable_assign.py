from langchain_core.runnables import RunnableLambda, RunnablePassthrough


def format_student(data):
    return (
        f"{data['name']} scored {data['score']} "
        f"and the result is {data['result']}."
    )


# The input to this pipeline is already a dictionary.
# assign() keeps the existing fields and adds a new calculated field.
chain = (
    RunnablePassthrough.assign(
        result=lambda data: "Pass" if data["score"] >= 50 else "Fail"
    )
    | RunnableLambda(format_student)
)

student = {
    "name": "Anu",
    "score": 82,
}

result = chain.invoke(student)

print("Final output:", result)
