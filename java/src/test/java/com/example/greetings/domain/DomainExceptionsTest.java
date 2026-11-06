package com.example.greetings.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DomainExceptionsTest {

    @Test
    void unsupportedLanguageIsADomainException() {
        assertThat(new UnsupportedLanguageException("xx")).isInstanceOf(DomainException.class);
    }

    @Test
    void unsupportedLanguageExposesMessageAndLanguage() {
        var exception = new UnsupportedLanguageException("xx");

        assertThat(exception).hasMessage("Unsupported language: 'xx'");
        assertThat(exception.language()).isEqualTo("xx");
    }

    @Test
    void invalidNameIsADomainExceptionWithMessage() {
        var exception = new InvalidNameException();

        assertThat(exception).isInstanceOf(DomainException.class).hasMessage("Name must not be blank");
    }
}
