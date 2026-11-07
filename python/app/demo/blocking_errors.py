PASSWORD = "hunter2"  # S105: hardcoded password


def run(expr: str) -> object:
    return eval(expr)  # S307: use of eval


def add(a: int, b: int) -> str:
    return a + b  # mypy: incompatible return value type
