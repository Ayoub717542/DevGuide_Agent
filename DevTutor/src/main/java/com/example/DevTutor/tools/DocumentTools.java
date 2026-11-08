package com.example.DevTutor.tools;

import com.example.DevTutor.entity.GeneratedDocument;
import com.example.DevTutor.service.serviceInterface.GeneratedDocumentService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentTools {

    private final GeneratedDocumentService documentService;

    private final RestClient wikimedia = RestClient.builder()
            .baseUrl("https://commons.wikimedia.org")
            .defaultHeader("User-Agent", "DevTutor/1.0")
            .build();

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
            For pictures, call findImage first and use only the URLs it returns
            in <img src="...">. Never invent image URLs.

            For DOCX: use strict XHTML with inline CSS only. Close every tag,
            including <br />, and do not use flexbox, grid or external resources.

            Use PDF or DOCX as the output format.

            Return the download path after successful creation.
            """)
    public Map<String, Object> createDocument(
            @ToolParam(description = "Document title, at most 255 characters")
            String title,

            @ToolParam(description = "Complete HTML page with CSS")
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

    @Tool(description = """
            Find real pictures for a PDF. Returns up to 3 image URLs from Wikimedia Commons.
            Call this before adding any <img> to a PDF and use only the URLs it returns.
            """)
    public List<String> findImage(
            @ToolParam(description = "What the picture should show, in English, for example HDMI connector")
            String query
    ) {
        try {
            CommonsResponse response = wikimedia.get()
                    .uri(uri -> uri.path("/w/api.php")
                            .queryParam("action", "query")
                            .queryParam("generator", "search")
                            .queryParam("gsrsearch", "filetype:bitmap " + query)
                            .queryParam("gsrnamespace", 6)
                            .queryParam("gsrlimit", 3)
                            .queryParam("prop", "imageinfo")
                            .queryParam("iiprop", "url")
                            .queryParam("iiurlwidth", 600)
                            .queryParam("format", "json")
                            .queryParam("formatversion", 2)
                            .build())
                    .retrieve()
                    .body(CommonsResponse.class);

            if (response == null || response.query() == null) {
                return List.of();
            }
            return response.query().pages().stream()
                    .filter(page -> page.imageinfo() != null)
                    .flatMap(page -> page.imageinfo().stream())
                    .map(ImageInfo::thumburl)
                    .toList();

        } catch (RuntimeException e) {
            log.warn("Image search failed for '{}'", query, e);
            return List.of();
        }
    }

    record CommonsResponse(Query query) {}
    record Query(List<Page> pages) {}
    record Page(List<ImageInfo> imageinfo) {}
    record ImageInfo(String thumburl) {}
}