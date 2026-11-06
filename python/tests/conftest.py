import pytest
from fastapi.testclient import TestClient

from app.domain.entities import LanguageProfile
from app.main import create_app
from app.presentation.dependencies import get_language_repository


class FakeLanguageRepository:
    """In-memory test double that also records the codes it was asked for."""

    def __init__(self, *profiles: LanguageProfile) -> None:
        self._profiles = {p.code: p for p in profiles}
        self.requested_codes: list[str] = []

    def get(self, language_code: str) -> LanguageProfile | None:
        self.requested_codes.append(language_code)
        return self._profiles.get(language_code)


@pytest.fixture
def english() -> LanguageProfile:
    return LanguageProfile("en", "Hello, {name}!", ("Hello", "Hi"))


@pytest.fixture
def spanish() -> LanguageProfile:
    return LanguageProfile("es", "¡Hola, {name}!", ("Hola", "Buenos días"))


@pytest.fixture
def repository(english, spanish) -> FakeLanguageRepository:
    return FakeLanguageRepository(english, spanish)


@pytest.fixture
def client(repository) -> TestClient:
    app = create_app()
    app.dependency_overrides[get_language_repository] = lambda: repository
    return TestClient(app)
