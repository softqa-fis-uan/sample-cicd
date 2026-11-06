package com.example.greetings.config;

import com.example.greetings.application.CreateGreeting;
import com.example.greetings.application.GetTypicalGreetings;
import com.example.greetings.application.LanguageRepository;
import com.example.greetings.infrastructure.InMemoryLanguageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    LanguageRepository languageRepository() {
        return new InMemoryLanguageRepository();
    }

    @Bean
    CreateGreeting createGreeting(LanguageRepository languages) {
        return new CreateGreeting(languages);
    }

    @Bean
    GetTypicalGreetings getTypicalGreetings(LanguageRepository languages) {
        return new GetTypicalGreetings(languages);
    }
}
