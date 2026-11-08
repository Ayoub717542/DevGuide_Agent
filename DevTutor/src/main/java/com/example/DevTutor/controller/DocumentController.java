package com.example.DevTutor.controller;

import com.example.DevTutor.dto.document.DocumentUploadResponse;
import com.example.DevTutor.service.serviceInterface.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public DocumentUploadResponse uploadPdf(
            @RequestParam Long conversationId,
            @RequestParam("file") MultipartFile file) {

        return documentService.uploadPdf(conversationId, file);
    }

    @GetMapping("/uploads")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<DocumentUploadResponse> getUploads(@RequestParam Long conversationId) {
        return documentService.getUploads(conversationId);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<DocumentUploadResponse> getAllUploads() {
        return documentService.getAllUploads();
    }

    @DeleteMapping("/uploads/{documentId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUpload(@PathVariable String documentId) {
        documentService.deleteUpload(documentId);
    }
}