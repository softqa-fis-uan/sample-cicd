from dataclasses import FrozenInstanceError

from assertpy import assert_that

from app.domain.entities import Greeting, LanguageProfile
from app.domain.errors import DomainError, UnsupportedLanguageError


class TestLanguageProfile:
    def test_greet_renders_template_with_name(self, spanish):
        greeting = spanish.greet("Ada")

        assert_that(greeting).is_equal_to(Greeting("¡Hola, Ada!", "es"))

    def test_greet_uses_profile_language_code(self, english):
        greeting = english.greet("Ada")

        assert_that(greeting.language).is_equal_to("en")
        assert_that(greeting.text).is_equal_to("Hello, Ada!")

    def test_greet_preserves_unicode_names(self, english):
        assert_that(english.greet("Zoë").text).is_equal_to("Hello, Zoë!")

    def test_typical_greetings_are_exposed_in_order(self, spanish):
        assert_that(spanish.typical_greetings).is_equal_to(("Hola", "Buenos días"))


class TestImmutability:
    def test_greeting_is_frozen(self):
        greeting = Greeting("Hello, Ada!", "en")

        assert_that(setattr).raises(FrozenInstanceError).when_called_with(
            greeting, "text", "changed"
        )

    def test_language_profile_is_frozen(self, english):
        assert_that(setattr).raises(FrozenInstanceError).when_called_with(
            english, "code", "xx"
        )


class TestUnsupportedLanguageError:
    def test_is_a_domain_error(self):
        assert_that(UnsupportedLanguageError("xx")).is_instance_of(DomainError)

    def test_message_and_language_attribute(self):
        error = UnsupportedLanguageError("xx")

        assert_that(str(error)).is_equal_to("Unsupported language: 'xx'")
        assert_that(error.language).is_equal_to("xx")