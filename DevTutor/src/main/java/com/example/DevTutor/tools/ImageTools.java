package com.example.DevTutor.tools;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageTools {

    private final ImageModel imageModel;
    private final Cloudinary cloudinary;

    @Tool(description = """
            Create a new picture from a description and return its URL.
            Use it only when the student asks you to draw, generate or create a picture or illustration.
            Show the picture in your answer as Markdown: ![short description](url).
            """)
    public Map<String, Object> generateImage(
            @ToolParam(description = "What the picture should show, in English, with enough detail")
            String description
    ) {
        try {
            ImageResponse response = imageModel.call(new ImagePrompt(description));
            if (response.getResult() == null || response.getResult().getOutput().getB64Json() == null) {
                return Map.of("success", false, "error", "The image model returned no picture.");
            }
            byte[] bytes = Base64.getDecoder().decode(response.getResult().getOutput().getB64Json());
            Map<?, ?> uploaded = cloudinary.uploader().upload(bytes, Map.of("resource_type", "image"));
            return Map.of("success", true, "url", uploaded.get("secure_url").toString());
        } catch (IOException | RuntimeException e) {
            log.warn("Image generation failed for '{}'", description, e);
            return Map.of("success", false, "error", "Could not create the picture.");
        }
    }
}
