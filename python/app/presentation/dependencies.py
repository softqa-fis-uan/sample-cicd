from functools import lru_cache

from fastapi import Depends

from app.application.ports import LanguageRepository
from app.application.use_cases import CreateGreeting, GetTypicalGreetings
from app.infrastructure.in_memory_repository import InMemoryLanguageRepository


@lru_cache
def get_language_repository() -> LanguageRepository:
    return InMemoryLanguageRepository()


def get_create_greeting(
    repo: LanguageRepository = Depends(get_language_repository),
) -> CreateGreeting:
    return CreateGreeting(repo)


def get_get_typical_greetings(
    repo: LanguageRepository = Depends(get_language_repository),
) -> GetTypicalGreetings:
    return GetTypicalGreetings(repo)