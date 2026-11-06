from typing import Protocol

from app.domain.entities import LanguageProfile


class LanguageRepository(Protocol):
    def get(self, language_code: str) -> LanguageProfile | None: ...