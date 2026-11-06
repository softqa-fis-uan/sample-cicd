class DomainError(Exception):
    """Base class for domain errors."""


class UnsupportedLanguageError(DomainError):
    def __init__(self, language: str) -> None:
        super().__init__(f"Unsupported language: '{language}'")
        self.language = language
