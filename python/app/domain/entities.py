from dataclasses import dataclass


@dataclass(frozen=True)
class Greeting:
    text: str
    language: str


@dataclass(frozen=True)
class LanguageProfile:
    code: str
    personal_template: str  # must contain "{name}"
    typical_greetings: tuple[str, ...]

    def greet(self, name: str) -> Greeting:
        return Greeting(
            text=self.personal_template.format(name=name),
            language=self.code,
        )
