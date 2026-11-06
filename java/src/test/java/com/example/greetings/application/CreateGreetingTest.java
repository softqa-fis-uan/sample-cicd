package com.example.greetings.application;

import static com.example.greetings.support.TestProfiles.ENGLISH;
import static com.example.greetings.support.TestProfiles.SPANISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.greetings.domain.Greeting;
import com.example.greetings.domain.InvalidNameException;
import com.example.greetings.domain.UnsupportedLanguageException;
import com.example.greetings.support.FakeLanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CreateGreetingTest {

    private final FakeLanguageRepository repository = new FakeLanguageRepository(ENGLISH, SPANISH);
    private final CreateGreeting useCase = new CreateGreeting(repository);

    @Test
    void returnsPersonalizedGreeting() {
        assertThat(useCase.execute("Ada", "en")).isEqualTo(new Greeting("Hello, Ada!", "en"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "ES", " es ", "es-CO", "es_CO", "Es-mx"})
    void normalizesLanguageCode(String language) {
        Greeting greeting = useCase.execute("Ada", language);

        assertThat(greeting.language()).isEqualTo("es");
        assertThat(greeting.text()).isEqualTo("¡Hola, Ada!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"es", "ES", " es ", "es-CO", "es_CO", "Es-mx"})
    void queriesRepositoryWithNormalizedCode(String language) {
        useCase.execute("Ada", language);

        assertThat(repository.requestedCodes()).containsExactly("es");
    }

    @Test
    void stripsWhitespaceFromName() {
        assertThat(useCase.execute("  Ada  ", "en").text()).isEqualTo("Hello, Ada!");
    }

    @ParameterizedTest
    @ValueSource(strings = {"xx", "klingon", "", "   ", "-en"})
    void rejectsUnsupportedLanguage(String language) {
        assertThatThrownBy(() -> useCase.execute("Ada", language))
                .isInstanceOf(UnsupportedLanguageException.class);
    }

    @Test
    void unsupportedLanguageErrorKeepsOriginalInput() {
        assertThatThrownBy(() -> useCase.execute("Ada", "XX"))
                .isInstanceOf(UnsupportedLanguageException.class)
                .hasMessage("Unsupported language: 'XX'");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> useCase.execute(name, "en")).isInstanceOf(InvalidNameException.class);
        assertThat(repository.requestedCodes()).isEmpty();
    }
}