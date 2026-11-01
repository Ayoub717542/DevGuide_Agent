package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.UploadedDocument;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.UploadedDocumentRepository;
import com.example.DevTutor.service.serviceInterface.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.dto.document.DocumentUploadResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final VectorStore vectorStore;
    private final ConversationRepository conversationRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;

    @Override
    public DocumentUploadResponse uploadPdf(Long conversationId, MultipartFile file){
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a PDF file.");
        }
        Conversation conversation = findOwnConversation(conversationId);
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(file.getResource()); //Gives the reader access to the uploaded file

        List<Document> pages;
        try {
            pages = reader.get(); //Extracts PDF text into Document objects
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not read this PDF. Make sure it is a valid, unencrypted PDF.");
        }

        TokenTextSplitter splitter = //uses token counts to limit passage sizes.
                TokenTextSplitter.builder().build(); //Divides that text into smaller passages, called chunks

        List<Document> chunks = splitter.apply(pages); //divides that text into smaller Spring AI Document objects.

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "No readable text was found in this PDF.");
        }
        String documentId = UUID.randomUUID().toString();

        for (Document chunk : chunks) {
            chunk.getMetadata().put(
                    "conversationId",
                    conversationId.toString()
            );

            chunk.getMetadata().put(
                    "documentId", documentId);

            chunk.getMetadata().put(
                    "filename",
                    file.getOriginalFilename()
            );
        }

        vectorStore.add(chunks); //Generates embeddings and stores the passages

        // Keep a record so the conversation can list its PDFs.
        UploadedDocument upload = new UploadedDocument();
        upload.setDocumentId(documentId);
        upload.setFilename(file.getOriginalFilename());
        upload.setChunksStored(chunks.size());
        upload.setConversation(conversation);
        uploadedDocumentRepository.save(upload);

        return toResponse(upload);
        //Returns how many chunks were stored
    }

    @Override
    public List<DocumentUploadResponse> getUploads(Long conversationId) {
        findOwnConversation(conversationId);

        return uploadedDocumentRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(DocumentServiceImpl::toResponse)
                .toList();
    }

    @Override
    public void deleteUpload(String documentId) {
        UploadedDocument upload = uploadedDocumentRepository
                .findByDocumentIdAndConversationCreatedByEmail(documentId, currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found"));

        // Remove the PDF's chunks first, so a Qdrant failure keeps the record.
        vectorStore.delete("documentId == '" + upload.getDocumentId() + "'");
        uploadedDocumentRepository.delete(upload);
    }

    // Only the creator can use a conversation (admins included).
    private Conversation findOwnConversation(Long conversationId) {
        return conversationRepository
                .findByIdAndCreatedByEmail(conversationId, currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conversation not found"));
    }

    private static DocumentUploadResponse toResponse(UploadedDocument upload) {
        return new DocumentUploadResponse(
                upload.getDocumentId(),
                upload.getFilename(),
                upload.getChunksStored()
        );
    }

    private static String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
