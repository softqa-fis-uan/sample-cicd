package com.example.greetings.application;

import com.example.greetings.domain.Greeting;
import com.example.greetings.domain.InvalidNameException;

public class CreateGreeting {

    private final LanguageResolver resolver;

    public CreateGreeting(LanguageRepository languages) {
        this.resolver = new LanguageResolver(languages);
    }

    public Greeting execute(String name, String language) {
        if (name == null || name.isBlank()) {
            throw new InvalidNameException();
        }
        return resolver.resolve(language).greet(name.strip());
    }
}
