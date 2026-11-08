package com.example.DevTutor.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class E5EmbeddingModel implements EmbeddingModel {

    private final TransformersEmbeddingModel model;

    public E5EmbeddingModel(TransformersEmbeddingModel model) {
        this.model = model;
    }

    @Override
    public float[] embed(String text) {
        return model.embed("query: " + text);
    }

    @Override
    public String getEmbeddingContent(Document document) {
        return "passage: " + model.getEmbeddingContent(document);
    }

    @Override
    public float[] embed(Document document) {
        return model.embed(getEmbeddingContent(document));
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return model.call(request);
    }

    @Override
    public int dimensions() {
        return model.dimensions();
    }
}
