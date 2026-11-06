package com.example.greetings.application;

import static com.example.greetings.support.TestProfiles.ENGLISH;
import static com.example.greetings.support.TestProfiles.SPANISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.greetings.domain.UnsupportedLanguageException;
import com.example.greetings.support.FakeLanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GetTypicalGreetingsTest {

    private final FakeLanguageRepository repository = new FakeLanguageRepository(ENGLISH, SPANISH);
    private final GetTypicalGreetings useCase = new GetTypicalGreetings(repository);

    @Test
    void returnsLanguageProfile() {
        assertThat(useCase.execute("es")).isEqualTo(SPANISH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "ES", " es ", "es-CO", "es_CO", "Es-mx"})
    void normalizesLanguageCode(String language) {
        assertThat(useCase.execute(language).code()).isEqualTo("es");
        assertThat(repository.requestedCodes()).containsExactly("es");
    }

    @ParameterizedTest
    @ValueSource(strings = {"xx", "", "   "})
    void rejectsUnsupportedLanguage(String language) {
        assertThatThrownBy(() -> useCase.execute(language))
                .isInstanceOf(UnsupportedLanguageException.class);
    }
}
