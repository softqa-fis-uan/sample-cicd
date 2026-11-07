package com.example.greetings.demo;

import java.io.File;
import java.util.*;

public final class BlockingErrors {

    public boolean sameName(String a, String b) {
        return a == b;
    }

    public void swallow() {
        try {
            Integer.parseInt("x");
        } catch (NumberFormatException e) {
        }
    }

    public void log(String message) {
        System.out.println(message);
    }
}