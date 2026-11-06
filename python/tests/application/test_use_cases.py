import pytest
from assertpy import assert_that

from app.application.use_cases import CreateGreeting, GetTypicalGreetings
from app.domain.entities import Greeting
from app.domain.errors import UnsupportedLanguageError

LANGUAGE_VARIANTS = ["es", "ES", " es ", "es-CO", "es_CO", "Es-mx"]


class TestCreateGreeting:
    def test_returns_personalized_greeting(self, repository):
        use_case = CreateGreeting(repository)

        result = use_case.execute(name="Ada", language="en")

        assert_that(result).is_equal_to(Greeting("Hello, Ada!", "en"))

    @pytest.mark.parametrize("language", LANGUAGE_VARIANTS)
    def test_normalizes_language_code(self, repository, language):
        use_case = CreateGreeting(repository)

        result = use_case.execute(name="Ada", language=language)

        assert_that(result.language).is_equal_to("es")
        assert_that(result.text).is_equal_to("¡Hola, Ada!")

    @pytest.mark.parametrize("language", LANGUAGE_VARIANTS)
    def test_queries_repository_with_normalized_code(self, repository, language):
        CreateGreeting(repository).execute(name="Ada", language=language)

        assert_that(repository.requested_codes).is_equal_to(["es"])

    def test_strips_whitespace_from_name(self, repository):
        result = CreateGreeting(repository).execute(name="  Ada  ", language="en")

        assert_that(result.text).is_equal_to("Hello, Ada!")

    @pytest.mark.parametrize("language", ["xx", "klingon", "", "   ", "-en"])
    def test_unsupported_language_raises(self, repository, language):
        use_case = CreateGreeting(repository)

        assert_that(use_case.execute).raises(
            UnsupportedLanguageError
        ).when_called_with(name="Ada", language=language)

    def test_unsupported_language_error_keeps_original_input(self, repository):
        use_case = CreateGreeting(repository)

        assert_that(use_case.execute).raises(
            UnsupportedLanguageError
        ).when_called_with(name="Ada", language="XX").is_equal_to(
            "Unsupported language: 'XX'"
        )


class TestGetTypicalGreetings:
    def test_returns_language_profile(self, repository, spanish):
        result = GetTypicalGreetings(repository).execute("es")

        assert_that(result).is_equal_to(spanish)
        assert_that(result.typical_greetings).contains_sequence("Hola", "Buenos días")

    @pytest.mark.parametrize("language", LANGUAGE_VARIANTS)
    def test_normalizes_language_code(self, repository, language):
        result = GetTypicalGreetings(repository).execute(language)

        assert_that(result.code).is_equal_to("es")
        assert_that(repository.requested_codes).is_equal_to(["es"])

    @pytest.mark.parametrize("language", ["xx", "", "   "])
    def test_unsupported_language_raises(self, repository, language):
        use_case = GetTypicalGreetings(repository)

        assert_that(use_case.execute).raises(
            UnsupportedLanguageError
        ).when_called_with(language)