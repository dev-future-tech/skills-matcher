package com.example.restservice.greeting;


import org.junit.jupiter.api.Test;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@SpringBootTest
public class EmbeddingsTest {

    @Autowired
    OpenAiEmbeddingModel model;

    @Test
    public void testEmbeddings() {

        EmbeddingResponse embeddingResponse = model.call(new EmbeddingRequest(List.of("Java","Python","GoLang"), OpenAiEmbeddingOptions.builder()
                .model("text-embedding-granite-embedding-278m-multilingual").build()));

        System.out.println(embeddingResponse);
    }
}
