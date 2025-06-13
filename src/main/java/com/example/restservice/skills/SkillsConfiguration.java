package com.example.restservice.skills;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class SkillsConfiguration {

    @Bean
    OpenAiApi openAiApi() {
        return OpenAiApi.builder()
                .apiKey(new NoopApiKey())
                .baseUrl("http://localhost:1234")
                .restClientBuilder(RestClient.builder()
                        // Force HTTP/1.1 for both streaming and non-streaming
                        .requestFactory(new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .connectTimeout(Duration.ofSeconds(30))
                                .build())))
                .build();
    }

    @Bean
    OpenAiEmbeddingModel openAiEmbeddingModel() {
        OpenAiEmbeddingOptions options = new OpenAiEmbeddingOptions();
        options.setModel("text-embedding-granite-embedding-278m-multilingual");
        options.setDimensions(1536);

        return new OpenAiEmbeddingModel(openAiApi(), MetadataMode.EMBED, options);
    }
}
