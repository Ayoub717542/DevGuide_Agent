package com.example.DevTutor.service.serviceInterface;

import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.dto.document.DocumentUploadResponse;
import com.example.DevTutor.entity.UploadedDocument;

import java.util.List;

public interface DocumentService {
    DocumentUploadResponse  uploadPdf(Long conversationId, MultipartFile file);
    List<DocumentUploadResponse> getUploads(Long conversationId);
    List<DocumentUploadResponse> getAllUploads();
    void deleteUpload(String documentId);
    void deleteOriginal(UploadedDocument upload);
}