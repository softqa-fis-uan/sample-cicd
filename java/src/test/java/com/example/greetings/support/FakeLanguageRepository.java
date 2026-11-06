package com.example.greetings.support;

import com.example.greetings.application.LanguageRepository;
import com.example.greetings.domain.LanguageProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeLanguageRepository implements LanguageRepository {

    private final Map<String, LanguageProfile> profiles = new HashMap<>();
    private final List<String> requestedCodes = new ArrayList<>();

    public FakeLanguageRepository(LanguageProfile... languageProfiles) {
        for (LanguageProfile profile : languageProfiles) {
            profiles.put(profile.code(), profile);
        }
    }

    @Override
    public Optional<LanguageProfile> findByCode(String code) {
        requestedCodes.add(code);
        return Optional.ofNullable(profiles.get(code));
    }

    public List<String> requestedCodes() {
        return List.copyOf(requestedCodes);
    }
}