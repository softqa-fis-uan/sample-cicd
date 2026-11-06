from app.domain.entities import LanguageProfile

_PROFILES = {
    p.code: p
    for p in (
        LanguageProfile("en", "Hello, {name}!", ("Hello", "Hi", "Good morning")),
        LanguageProfile("es", "¡Hola, {name}!", ("Hola", "Buenos días", "Buenas tardes")),
        LanguageProfile("fr", "Bonjour, {name} !", ("Bonjour", "Salut", "Bonsoir")),
        LanguageProfile("de", "Hallo, {name}!", ("Hallo", "Guten Morgen", "Guten Tag")),
        LanguageProfile("pt", "Olá, {name}!", ("Olá", "Bom dia", "Boa tarde")),
        LanguageProfile("it", "Ciao, {name}!", ("Ciao", "Buongiorno", "Salve")),
        LanguageProfile("ja", "こんにちは、{name}！", ("こんにちは", "おはようございます", "こんばんは")),
    )
}


class InMemoryLanguageRepository:
    def get(self, language_code: str) -> LanguageProfile | None:
        return _PROFILES.get(language_code)