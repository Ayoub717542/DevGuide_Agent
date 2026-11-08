package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.entity.GeneratedDocument;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.GeneratedDocumentRepository;
import com.example.DevTutor.service.serviceInterface.GeneratedDocumentService;

import lombok.RequiredArgsConstructor;

import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class GeneratedDocumentServiceImpl implements GeneratedDocumentService {

    private static final String MERMAID_SCRIPT = """
            <script src="https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js"></script>
            <script>
            window.mermaid
                ? mermaid.run().finally(() => window.mermaidDone = true)
                : window.mermaidDone = true;
            </script>
            """;

    private static final Pattern IMAGE_TAG = Pattern.compile("<img[^>]*src=\"([^\"]+)\"[^>]*>");

    private final RestClient imageChecker = RestClient.builder()
            .requestFactory(imageCheckerFactory())
            .build();

    private final GeneratedDocumentRepository repository;
    private final ConversationRepository conversationRepository;

    @Value("${app.gotenberg.url}")
    private String gotenbergUrl;

    @Override
    public GeneratedDocument createDocument(
            String title,
            String html,
            String format,
            Long conversationId
    ) {

        if (title == null || title.isBlank()
                || title.length() > 255
                || html == null || html.isBlank()
                || format == null || format.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A title of 1–255 characters, HTML and format are required."
            );
        }


        String normalizedFormat = format.trim()
                .toUpperCase(Locale.ROOT);
        html = html.replace("&nbsp;", "&#160;");
        // Escape any '&' that does not start an entity like &amp; or &#160;
        html = html.replaceAll("&(?!#?\\w+;)", "&amp;");
        byte[] data = switch (normalizedFormat) {
            case "PDF" -> generatePdf(html);
            case "DOCX" -> generateWord(html);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF and DOCX are supported."
            );
        };

        GeneratedDocument document = new GeneratedDocument();
        document.setTitle(title);
        document.setFormat(normalizedFormat);
        document.setData(data);
        document.setConversation(conversationRepository.getReferenceById(conversationId));

        return repository.save(document);
    }

    private byte[] generatePdf(String html) {
        html = removeBrokenImages(html);
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        if (html.contains("class=\"mermaid\"")) {
            html = html.contains("</body>")
                    ? html.replace("</body>", MERMAID_SCRIPT + "</body>")
                    : html + MERMAID_SCRIPT;
            form.add("waitForExpression", "window.mermaidDone === true");
        }
        form.add("files", new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "index.html";
            }
        });
        form.add("printBackground", "true");

        try {
            return RestClient.create(gotenbergUrl)
                    .post()
                    .uri("/forms/chromium/convert/html")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Could not convert HTML to PDF.", e
            );
        }
    }

    private String removeBrokenImages(String html) {
        return IMAGE_TAG.matcher(html).replaceAll(image ->
                imageWorks(image.group(1)) ? Matcher.quoteReplacement(image.group()) : "");
    }

    private boolean imageWorks(String url) {
        if (!url.startsWith("http")) {
            return true;
        }
        try {
            imageChecker.get()
                    .uri(URI.create(url.replace("&amp;", "&")))
                    .header("User-Agent", "DevTutor/1.0")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static SimpleClientHttpRequestFactory imageCheckerFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    private byte[] generateWord(String html) {
        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            // Create an empty Word document.
            WordprocessingMLPackage word =
                    WordprocessingMLPackage.createPackage();

            // Convert XHTML into Word elements.
            XHTMLImporterImpl importer =
                    new XHTMLImporterImpl(word);

            word.getMainDocumentPart()
                    .getContent()
                    .addAll(importer.convert(html, null));

            // Write the Word file into memory.
            word.save(output);

            return output.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not convert HTML to Word.", e
            );
        }
    }
}