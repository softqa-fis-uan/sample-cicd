from functools import lru_cache

from fastapi import Depends

from app.application.ports import LanguageRepository
from app.application.use_cases import CreateGreeting, GetTypicalGreetings
from app.infrastructure.in_memory_repository import InMemoryLanguageRepository


@lru_cache
def get_language_repository() -> LanguageRepository:
    return InMemoryLanguageRepository()


_language_repository_dependency = Depends(get_language_repository)


def get_create_greeting(
    repo: LanguageRepository = _language_repository_dependency,
) -> CreateGreeting:
    return CreateGreeting(repo)


def get_get_typical_greetings(
    repo: LanguageRepository = _language_repository_dependency,
) -> GetTypicalGreetings:
    return GetTypicalGreetings(repo)
