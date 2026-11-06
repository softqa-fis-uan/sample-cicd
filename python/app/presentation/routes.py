from typing import Annotated

from fastapi import APIRouter, Depends, Query

from app.application.use_cases import CreateGreeting, GetTypicalGreetings
from app.presentation.dependencies import get_create_greeting, get_get_typical_greetings
from app.presentation.schemas import (
    CreateGreetingRequest,
    GreetingResponse,
    TypicalGreetingsResponse,
)

router = APIRouter(prefix="/greetings", tags=["greetings"])

_400 = {400: {"description": "Invalid request or unsupported language"}}


@router.post(
    "",
    operation_id="createGreeting",
    summary="Generate a personalized greeting",
    response_model=GreetingResponse,
    responses=_400,
)
def create_greeting(
    body: CreateGreetingRequest,
    use_case: CreateGreeting = Depends(get_create_greeting),
) -> GreetingResponse:
    greeting = use_case.execute(body.name, body.language)
    return GreetingResponse(greeting=greeting.text, language=greeting.language)


@router.get(
    "",
    operation_id="getTypicalGreetings",
    summary="Get typical greetings in a language",
    response_model=TypicalGreetingsResponse,
    responses=_400,
)
def get_typical_greetings(
    lang: Annotated[str, Query(description="Language code, such as en or es.", examples=["es"])],
    use_case: GetTypicalGreetings = Depends(get_get_typical_greetings),
) -> TypicalGreetingsResponse:
    profile = use_case.execute(lang)
    return TypicalGreetingsResponse(
        language=profile.code, greetings=list(profile.typical_greetings)
    )
