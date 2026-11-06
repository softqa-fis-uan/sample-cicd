package com.example.greetings.application;

import com.example.greetings.domain.LanguageProfile;

public class GetTypicalGreetings {

    private final LanguageResolver resolver;

    public GetTypicalGreetings(LanguageRepository languages) {
        this.resolver = new LanguageResolver(languages);
    }

    public LanguageProfile execute(String language) {
        return resolver.resolve(language);
    }
}