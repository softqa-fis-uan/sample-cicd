from typing import Annotated

from pydantic import BaseModel, Field, StringConstraints

NonBlank = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1)]


class CreateGreetingRequest(BaseModel):
    name: NonBlank = Field(examples=["Ada"])
    language: NonBlank = Field(description="Language code, such as en or es.", examples=["en"])


class GreetingResponse(BaseModel):
    greeting: str = Field(examples=["Hello, Ada!"])
    language: str = Field(examples=["en"])


class TypicalGreetingsResponse(BaseModel):
    language: str = Field(examples=["es"])
    greetings: list[str] = Field(examples=[["Hola", "Buenos días"]])
