package com.example.greetings.domain;

import static com.example.greetings.support.TestProfiles.SPANISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LanguageProfileTest {

    @Test
    void greetRendersTemplateWithName() {
        assertThat(SPANISH.greet("Ada")).isEqualTo(new Greeting("¡Hola, Ada!", "es"));
    }

    @Test
    void greetPreservesUnicodeNames() {
        assertThat(SPANISH.greet("Zoë").text()).isEqualTo("¡Hola, Zoë!");
    }

    @Test
    void greetTreatsSpecialCharactersInNameLiterally() {
        assertThat(SPANISH.greet("$1 {name} \\").text()).isEqualTo("¡Hola, $1 {name} \\!");
    }

    @Test
    void typicalGreetingsKeepTheirOrder() {
        assertThat(SPANISH.typicalGreetings()).containsExactly("Hola", "Buenos días");
    }

    @Test
    void typicalGreetingsAreCopiedDefensively() {
        List<String> source = new ArrayList<>(List.of("Hola"));
        LanguageProfile profile = new LanguageProfile("es", "¡Hola, {name}!", source);

        source.add("Adiós");

        assertThat(profile.typicalGreetings()).containsExactly("Hola");
    }

    @Test
    void typicalGreetingsCannotBeModified() {
        assertThatThrownBy(() -> SPANISH.typicalGreetings().add("Adiós"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
