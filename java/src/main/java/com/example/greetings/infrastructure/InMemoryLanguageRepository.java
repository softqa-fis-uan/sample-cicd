package com.example.greetings.infrastructure;

import com.example.greetings.application.LanguageRepository;
import com.example.greetings.domain.LanguageProfile;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryLanguageRepository implements LanguageRepository {

    private static final Map<String, LanguageProfile> PROFILES = Stream.of(
                    new LanguageProfile("en", "Hello, {name}!", List.of("Hello", "Hi", "Good morning")),
                    new LanguageProfile("es", "¡Hola, {name}!", List.of("Hola", "Buenos días", "Buenas tardes")),
                    new LanguageProfile("fr", "Bonjour, {name} !", List.of("Bonjour", "Salut", "Bonsoir")),
                    new LanguageProfile("de", "Hallo, {name}!", List.of("Hallo", "Guten Morgen", "Guten Tag")),
                    new LanguageProfile("pt", "Olá, {name}!", List.of("Olá", "Bom dia", "Boa tarde")),
                    new LanguageProfile("it", "Ciao, {name}!", List.of("Ciao", "Buongiorno", "Buonasera")),
                    new LanguageProfile("ja", "こんにちは、{name}さん！", List.of("こんにちは", "おはようございます", "こんばんは")))
            .collect(Collectors.toUnmodifiableMap(LanguageProfile::code, Function.identity()));

    @Override
    public Optional<LanguageProfile> findByCode(String code) {
        return Optional.ofNullable(PROFILES.get(code));
    }
}