package com.example.DevTutor.dto.document;

import com.example.DevTutor.entity.GeneratedDocument;

public record GeneratedDocumentResponse(
        Long id,
        String title,
        String format,
        String downloadPath) {

    public static GeneratedDocumentResponse from(GeneratedDocument document) {
        return new GeneratedDocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getFormat(),
                "/api/documents/" + document.getId() + "/download");
    }
}
