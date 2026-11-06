package com.example.greetings.domain;

import java.util.List;

public record LanguageProfile(String code, String personalTemplate, List<String> typicalGreetings) {

    public LanguageProfile {
        typicalGreetings = List.copyOf(typicalGreetings);
    }

    public Greeting greet(String name) {
        return new Greeting(personalTemplate.replace("{name}", name), code);
    }
}