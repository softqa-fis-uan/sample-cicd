from app.application.ports import LanguageRepository
from app.domain.entities import Greeting, LanguageProfile
from app.domain.errors import UnsupportedLanguageError


def _normalize(code: str) -> str:
    # "ES", " es ", "es-CO" -> "es"
    return code.strip().lower().replace("_", "-").split("-")[0]


def _require_profile(repo: LanguageRepository, code: str) -> LanguageProfile:
    normalized = _normalize(code)
    profile = repo.get(normalized)
    if profile is None:
        raise UnsupportedLanguageError(code)
    return profile


class CreateGreeting:
    def __init__(self, languages: LanguageRepository) -> None:
        self._languages = languages

    def execute(self, name: str, language: str) -> Greeting:
        return _require_profile(self._languages, language).greet(name.strip())


class GetTypicalGreetings:
    def __init__(self, languages: LanguageRepository) -> None:
        self._languages = languages

    def execute(self, language: str) -> LanguageProfile:
        return _require_profile(self._languages, language)