package com.example.DevTutor.service;

import com.example.DevTutor.dto.chat.CheckpointResult;
import com.example.DevTutor.entity.LearnerPreferences;
import com.example.DevTutor.enums.CodePreference;
import com.example.DevTutor.enums.ExperienceLevel;
import com.example.DevTutor.enums.ExplanationStyle;
import com.example.DevTutor.enums.ResponseLength;
import com.example.DevTutor.enums.Tone;
import com.example.DevTutor.repository.LearnerPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearnerPreferencesServiceTest {

    private static final String EMAIL = "ali@mail.com";

    @Mock
    private LearnerPreferencesRepository repository;

    @InjectMocks
    private LearnerPreferencesService service;

    private LearnerPreferences existing;

    @BeforeEach
    void setUp() {
        existing = new LearnerPreferences();
        existing.setUserEmail(EMAIL);
        existing.setExperienceLevel(ExperienceLevel.BEGINNER);
        existing.setExplanationStyle(ExplanationStyle.DIRECT);
        existing.setResponseLength(ResponseLength.SHORT);
        existing.setCodePreference(CodePreference.SMALL_EXAMPLES);
        existing.setTone(Tone.FRIENDLY);
    }

    private static CheckpointResult detected(String level, String style, String length,
                                             String code, String tone) {
        return new CheckpointResult("summary", level, style, length, code, tone);
    }

    @Test
    void parseAcceptsValidValues() {
        assertEquals(ResponseLength.SHORT, LearnerPreferencesService.parse(ResponseLength.class, "SHORT"));
        assertEquals(ResponseLength.SHORT, LearnerPreferencesService.parse(ResponseLength.class, " short "));
    }

    @Test
    void parseRejectsInvalidValues() {
        assertNull(LearnerPreferencesService.parse(ResponseLength.class, "VERY_SHORT"));
        assertNull(LearnerPreferencesService.parse(ResponseLength.class, "null"));
        assertNull(LearnerPreferencesService.parse(ResponseLength.class, ""));
        assertNull(LearnerPreferencesService.parse(ResponseLength.class, null));
    }

    @Test
    void invalidValuesKeepExistingPreferences() {
        when(repository.findLockedByUserEmail(EMAIL)).thenReturn(Optional.of(existing));

        service.merge(EMAIL, detected("EXPERT", "FUNNY", "VERY_SHORT", "NO_CODE", "SARCASTIC"));

        assertEquals(ExperienceLevel.BEGINNER, existing.getExperienceLevel());
        assertEquals(ExplanationStyle.DIRECT, existing.getExplanationStyle());
        assertEquals(ResponseLength.SHORT, existing.getResponseLength());
        assertEquals(CodePreference.SMALL_EXAMPLES, existing.getCodePreference());
        assertEquals(Tone.FRIENDLY, existing.getTone());
    }

    @Test
    void nullValuesKeepExistingPreferences() {
        when(repository.findLockedByUserEmail(EMAIL)).thenReturn(Optional.of(existing));

        service.merge(EMAIL, detected(null, null, null, null, null));

        assertEquals(ExperienceLevel.BEGINNER, existing.getExperienceLevel());
        assertEquals(ResponseLength.SHORT, existing.getResponseLength());
        assertEquals(Tone.FRIENDLY, existing.getTone());
    }

    @Test
    void onlyDetectedFieldsChange() {
        when(repository.findLockedByUserEmail(EMAIL)).thenReturn(Optional.of(existing));

        service.merge(EMAIL, detected(null, null, null, null, "HUMOROUS"));

        assertEquals(Tone.HUMOROUS, existing.getTone());
        assertEquals(ExperienceLevel.BEGINNER, existing.getExperienceLevel());
        assertEquals(ExplanationStyle.DIRECT, existing.getExplanationStyle());
        assertEquals(ResponseLength.SHORT, existing.getResponseLength());
        assertEquals(CodePreference.SMALL_EXAMPLES, existing.getCodePreference());
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void levelMovesOnlyOneStep() {
        when(repository.findLockedByUserEmail(EMAIL)).thenReturn(Optional.of(existing));

        service.merge(EMAIL, detected("ADVANCED", null, null, null, null));

        assertEquals(ExperienceLevel.INTERMEDIATE, existing.getExperienceLevel());
    }

    @Test
    void oneStepRules() {
        assertEquals(ExperienceLevel.ADVANCED,
                LearnerPreferencesService.oneStep(null, ExperienceLevel.ADVANCED));
        assertEquals(ExperienceLevel.INTERMEDIATE,
                LearnerPreferencesService.oneStep(ExperienceLevel.ADVANCED, ExperienceLevel.BEGINNER));
        assertEquals(ExperienceLevel.BEGINNER,
                LearnerPreferencesService.oneStep(ExperienceLevel.BEGINNER, ExperienceLevel.BEGINNER));
    }

    @Test
    void newUserGetsRowWithEmailFromCaller() {
        when(repository.findLockedByUserEmail(EMAIL)).thenReturn(Optional.empty());

        service.merge(EMAIL, detected("ADVANCED", "ANALOGIES", null, null, null));

        ArgumentCaptor<LearnerPreferences> saved = ArgumentCaptor.forClass(LearnerPreferences.class);
        verify(repository).saveAndFlush(saved.capture());
        assertEquals(EMAIL, saved.getValue().getUserEmail());
        assertEquals(ExperienceLevel.ADVANCED, saved.getValue().getExperienceLevel());
        assertEquals(ExplanationStyle.ANALOGIES, saved.getValue().getExplanationStyle());
        assertNull(saved.getValue().getResponseLength());
    }
}
