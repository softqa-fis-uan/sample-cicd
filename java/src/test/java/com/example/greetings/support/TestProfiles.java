package com.example.greetings.support;

import com.example.greetings.domain.LanguageProfile;
import java.util.List;

public final class TestProfiles {

    public static final LanguageProfile ENGLISH =
            new LanguageProfile("en", "Hello, {name}!", List.of("Hello", "Hi"));

    public static final LanguageProfile SPANISH =
            new LanguageProfile("es", "¡Hola, {name}!", List.of("Hola", "Buenos días"));

    private TestProfiles() {}
}