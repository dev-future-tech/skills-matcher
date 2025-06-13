package com.example.restservice.skills;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorDatabaseConfig {

    @Bean
    MilvusClientV2 getMilvusClientV2() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .dbName("default")
                .uri("http://localhost:19530")
                .serverName("localhost")
                .build();
        return new MilvusClientV2(connectConfig);
    }

    @Bean
    DatabaseSchemaManager getDatabaseSchemaManager() {
        DatabaseSchemaManager databaseSchemaManager = new DatabaseSchemaManager();
        return databaseSchemaManager;
    }
}
