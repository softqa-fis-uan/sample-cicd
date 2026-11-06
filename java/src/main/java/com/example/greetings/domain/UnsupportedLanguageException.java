package com.example.greetings.domain;

public class UnsupportedLanguageException extends DomainException {

    private final String language;

    public UnsupportedLanguageException(String language) {
        super("Unsupported language: '" + language + "'");
        this.language = language;
    }

    public String language() {
        return language;
    }
}