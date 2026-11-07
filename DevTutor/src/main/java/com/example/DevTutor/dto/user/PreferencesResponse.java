package com.example.DevTutor.dto.user;

import com.example.DevTutor.entity.LearnerPreferences;
import com.example.DevTutor.enums.CodePreference;
import com.example.DevTutor.enums.ExperienceLevel;
import com.example.DevTutor.enums.ExplanationStyle;
import com.example.DevTutor.enums.ResponseLength;
import com.example.DevTutor.enums.Tone;

public record PreferencesResponse(
        ExperienceLevel experienceLevel,
        ExplanationStyle explanationStyle,
        ResponseLength responseLength,
        CodePreference codePreference,
        Tone tone) {

    public static PreferencesResponse from(LearnerPreferences preferences) {
        return new PreferencesResponse(
                preferences.isExperienceLevelManual() ? preferences.getExperienceLevel() : null,
                preferences.isExplanationStyleManual() ? preferences.getExplanationStyle() : null,
                preferences.isResponseLengthManual() ? preferences.getResponseLength() : null,
                preferences.isCodePreferenceManual() ? preferences.getCodePreference() : null,
                preferences.isToneManual() ? preferences.getTone() : null);
    }
}
