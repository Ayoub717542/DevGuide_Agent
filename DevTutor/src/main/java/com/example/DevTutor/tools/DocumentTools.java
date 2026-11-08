package com.example.DevTutor.tools;

import com.example.DevTutor.entity.GeneratedDocument;
import com.example.DevTutor.service.serviceInterface.GeneratedDocumentService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTools {

    private final GeneratedDocumentService documentService;

    @Tool(description = """
            Create a styled PDF or Word document when requested.

            Generate the complete document as one HTML page with CSS.
            Follow the user's requested colors, layout and style.
            If no style is specified, choose a modern, readable, professional design.

            Include all content inside the HTML.
            Use headings, paragraphs, lists, tables, code blocks and highlighted
            tip boxes where appropriate.
            Always write < as &lt; and > as &gt; in text and code, for example
            if (a &lt; b) and List&lt;String&gt;.
            Do not include Markdown fences or JavaScript.

            For PDF: a full <style> block, flexbox, grid and Google Fonts are allowed.
            Add images with <img src="https://..."> only when you are sure the URL
            exists, for example from upload.wikimedia.org; otherwise add no image.

            For DOCX: use strict XHTML with inline CSS only. Close every tag,
            including <br />, and do not use flexbox, grid or external resources.

            Use PDF or DOCX as the output format.

            Return the download path after successful creation.
            """)
    public Map<String, Object> createDocument(
            @ToolParam(description = "Document title, at most 255 characters")
            String title,

            @ToolParam(description = "Complete valid XHTML with inline CSS")
            String html,

            @ToolParam(description = "PDF or DOCX")
            String format,

            // Set by ChatServiceImpl; the AI cannot see or change it.
            ToolContext toolContext
    ) {
        try {
            Long conversationId =
                    (Long) toolContext.getContext().get("conversationId");

            GeneratedDocument document =
                    documentService.createDocument(title, html, format, conversationId);

            return Map.of(
                    "success", true,
                    "documentId", document.getId(),
                    "format", document.getFormat(),
                    "downloadPath",
                    "/api/documents/" + document.getId() + "/download"
            );

        } catch (RuntimeException e) {

            log.error("Document generation failed", e);


            return Map.of(
                    "success", false,
                    "error", "Document generation failed. No download is available."
            );
        }
    }
}