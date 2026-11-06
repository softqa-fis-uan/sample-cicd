package com.example.greetings.application;

import com.example.greetings.domain.LanguageProfile;
import com.example.greetings.domain.UnsupportedLanguageException;
import java.util.Locale;

final class LanguageResolver {

    private final LanguageRepository languages;

    LanguageResolver(LanguageRepository languages) {
        this.languages = languages;
    }

    LanguageProfile resolve(String language) {
        return languages.findByCode(normalize(language))
                .orElseThrow(() -> new UnsupportedLanguageException(language));
    }

    // "ES", " es ", "es-CO", "es_CO" -> "es"
    static String normalize(String code) {
        return code.strip().toLowerCase(Locale.ROOT).replace('_', '-').split("-", -1)[0];
    }
}
