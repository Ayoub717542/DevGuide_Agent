package com.example.DevTutor.exception;

import com.example.DevTutor.dto.common.ApiError;
import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.QdrantException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.example.DevTutor.util.Exceptions.findCause;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DOCUMENT_SEARCH_DOWN =
            "Document search is unavailable. Please try again later.";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleBadInput(
            Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid or missing request data", request);
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "File is too large. Maximum size is 10 MB.", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        return build(HttpStatus.valueOf(ex.getStatusCode().value()),
                ex.getReason(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Access denied", request);
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ApiError> handleGeminiClient(
            ClientException ex, HttpServletRequest request) {
        if (ex.code() == 429) {
            log.warn("Gemini rate limit: {}", ex.message());
            return build(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI service is busy. Please try again shortly.", request);
        }
        log.error("Gemini rejected request (code {}): {}", ex.code(), ex.message(), ex);
        return build(HttpStatus.BAD_GATEWAY,
                "The AI service rejected the request.", request);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ApiError> handleGeminiServer(
            ServerException ex, HttpServletRequest request) {
        log.error("Gemini server error: {}", ex.message(), ex);
        return build(HttpStatus.BAD_GATEWAY,
                "The AI service is currently unavailable. Please try again.", request);
    }

    @ExceptionHandler(GenAiIOException.class)
    public ResponseEntity<ApiError> handleGeminiUnreachable(
            GenAiIOException ex, HttpServletRequest request) {
        log.error("Cannot reach Gemini", ex);
        return build(HttpStatus.BAD_GATEWAY,
                "Cannot reach the AI service. Please try again later.", request);
    }

    @ExceptionHandler(TransientAiException.class)
    public ResponseEntity<ApiError> handleTransientAi(
            TransientAiException ex, HttpServletRequest request) {
        log.error("Transient AI failure", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "AI service temporarily unavailable. Please try again.", request);
    }

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<ApiError> handleNonTransientAi(
            NonTransientAiException ex, HttpServletRequest request) {
        log.error("Non-transient AI failure", ex);
        return build(HttpStatus.BAD_GATEWAY,
                "The AI service rejected the request.", request);
    }

    @ExceptionHandler(QdrantException.class)
    public ResponseEntity<ApiError> handleQdrant(
            QdrantException ex, HttpServletRequest request) {
        log.error("Qdrant failure", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE, DOCUMENT_SEARCH_DOWN, request);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> handleDatabaseDown(
            DataAccessResourceFailureException ex, HttpServletRequest request) {
        log.error("Database unavailable", ex);
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "Database is unavailable. Please try again later.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        if (findCause(ex, StatusRuntimeException.class).isPresent()) {
            log.error("Qdrant unreachable", ex);
            return build(HttpStatus.SERVICE_UNAVAILABLE, DOCUMENT_SEARCH_DOWN, request);
        }
        Optional<ClientException> geminiClient = findCause(ex, ClientException.class);
        if (geminiClient.isPresent()) {
            return handleGeminiClient(geminiClient.get(), request);
        }
        Optional<ServerException> geminiServer = findCause(ex, ServerException.class);
        if (geminiServer.isPresent()) {
            return handleGeminiServer(geminiServer.get(), request);
        }
        Optional<GenAiIOException> geminiIo = findCause(ex, GenAiIOException.class);
        if (geminiIo.isPresent()) {
            return handleGeminiUnreachable(geminiIo.get(), request);
        }
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.", request);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                ApiError.of(status.value(), status.getReasonPhrase(),
                        message, request.getRequestURI()));
    }
}