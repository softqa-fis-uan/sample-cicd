package com.example.greetings.domain;

public class InvalidNameException extends DomainException {

    public InvalidNameException() {
        super("Name must not be blank");
    }
}
