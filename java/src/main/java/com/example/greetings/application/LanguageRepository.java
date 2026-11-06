package com.example.greetings.application;

import com.example.greetings.domain.LanguageProfile;
import java.util.Optional;

public interface LanguageRepository {

    Optional<LanguageProfile> findByCode(String code);
}