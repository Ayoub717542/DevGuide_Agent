package com.example.DevTutor.dto.user;

import com.example.DevTutor.entity.LearnerNote;

public record LearnerNoteResponse(
        Long id,
        String note) {

    public static LearnerNoteResponse from(LearnerNote learnerNote) {
        return new LearnerNoteResponse(learnerNote.getId(), learnerNote.getNote());
    }
}
