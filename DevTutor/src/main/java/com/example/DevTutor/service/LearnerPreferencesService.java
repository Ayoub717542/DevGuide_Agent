package com.example.DevTutor.service;

import com.example.DevTutor.dto.chat.CheckpointResult;
import com.example.DevTutor.entity.LearnerPreferences;
import com.example.DevTutor.enums.CodePreference;
import com.example.DevTutor.enums.ExperienceLevel;
import com.example.DevTutor.enums.ExplanationStyle;
import com.example.DevTutor.enums.ResponseLength;
import com.example.DevTutor.enums.Tone;
import com.example.DevTutor.repository.LearnerPreferencesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class LearnerPreferencesService {

    private final LearnerPreferencesRepository learnerPreferencesRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void merge(String userEmail, CheckpointResult detected) {
        LearnerPreferences preferences = learnerPreferencesRepository
                .findLockedByUserEmail(userEmail)
                .orElseGet(LearnerPreferences::new);
        preferences.setUserEmail(userEmail);

        ExperienceLevel level = parse(ExperienceLevel.class, detected.experienceLevel());
        if (level != null) {
            preferences.setExperienceLevel(oneStep(preferences.getExperienceLevel(), level));
        }
        set(parse(ExplanationStyle.class, detected.explanationStyle()), preferences::setExplanationStyle);
        set(parse(ResponseLength.class, detected.responseLength()), preferences::setResponseLength);
        set(parse(CodePreference.class, detected.codePreference()), preferences::setCodePreference);
        set(parse(Tone.class, detected.tone()), preferences::setTone);

        learnerPreferencesRepository.saveAndFlush(preferences);
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(value.trim().toUpperCase())) {
                return constant;
            }
        }
        return null;
    }

    static ExperienceLevel oneStep(ExperienceLevel current, ExperienceLevel detected) {
        if (current == null) {
            return detected;
        }
        int step = Integer.signum(detected.ordinal() - current.ordinal());
        return ExperienceLevel.values()[current.ordinal() + step];
    }

    private static <T> void set(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
