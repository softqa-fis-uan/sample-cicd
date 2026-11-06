package com.example.greetings.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.greetings.domain.LanguageProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InMemoryLanguageRepositoryTest {

    private final InMemoryLanguageRepository repository = new InMemoryLanguageRepository();

    @ParameterizedTest
    @ValueSource(strings = {"en", "es", "fr", "de", "pt"})
    void providesWellFormedProfileForEachSupportedLanguage(String code) {
        LanguageProfile profile = repository.findByCode(code).orElseThrow();

        assertThat(profile.code()).isEqualTo(code);
        assertThat(profile.personalTemplate()).contains("{name}");
        assertThat(profile.typicalGreetings()).isNotEmpty();
    }

    @Test
    void returnsEmptyForUnknownLanguage() {
        assertThat(repository.findByCode("xx")).isEmpty();
    }

    @Test
    void lookupIsCaseSensitiveBecauseNormalizationIsTheUseCasesJob() {
        assertThat(repository.findByCode("EN")).isEmpty();
    }
}