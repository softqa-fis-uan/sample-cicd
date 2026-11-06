package com.example.greetings.presentation;

import com.example.greetings.application.CreateGreeting;
import com.example.greetings.application.GetTypicalGreetings;
import com.example.greetings.presentation.api.GreetingsApi;
import com.example.greetings.presentation.api.model.CreateGreetingRequest;
import com.example.greetings.presentation.api.model.Greeting;
import com.example.greetings.presentation.api.model.TypicalGreetings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingsController implements GreetingsApi {

    private final CreateGreeting createGreetingUseCase;
    private final GetTypicalGreetings typicalGreetingsUseCase;

    public GreetingsController(CreateGreeting createGreetingUseCase,
                               GetTypicalGreetings typicalGreetingsUseCase) {
        this.createGreetingUseCase = createGreetingUseCase;
        this.typicalGreetingsUseCase = typicalGreetingsUseCase;
    }

    @Override
    public ResponseEntity<Greeting> createGreeting(CreateGreetingRequest request) {
        // 'var' is the domain Greeting; the imported Greeting is the generated DTO
        var greeting = createGreetingUseCase.execute(request.getName(), request.getLanguage());
        return ResponseEntity.ok(new Greeting()
                .greeting(greeting.text())
                .language(greeting.language()));
    }

    @Override
    public ResponseEntity<TypicalGreetings> getTypicalGreetings(String lang) {
        var profile = typicalGreetingsUseCase.execute(lang);
        return ResponseEntity.ok(new TypicalGreetings()
                .language(profile.code())
                .greetings(profile.typicalGreetings()));
    }
}