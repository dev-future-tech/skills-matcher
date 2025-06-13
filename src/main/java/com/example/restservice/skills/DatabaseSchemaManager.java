package com.example.restservice.skills;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSchemaManager {
    private final Logger logger = LoggerFactory.getLogger(DatabaseSchemaManager.class);

    @Autowired
    MilvusClientV2 milvusClient;

    @PostConstruct
    void init() {
        logger.info("Initializing Milvus Database Schema Manager");
        createSchema();
    }

    void createSchema() {

        String COLLECTION_NAME = "skills";

        logger.debug("Checking to see if the collection {} exists", COLLECTION_NAME);
        boolean collectionExists = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(COLLECTION_NAME)
                .build()
        );

        if (!collectionExists) {
            logger.debug("Collection does not exist, creating the collection {}", COLLECTION_NAME);

            CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();

            schema.addField(AddFieldReq.builder()
                    .fieldName("user_id")
                    .dataType(DataType.Int64)
                    .isPrimaryKey(true)
                    .autoID(true)
                    .build()
            );

            schema.addField(AddFieldReq.builder()
                    .fieldName("username")
                    .dataType(DataType.VarChar)
                    .build()
            );

            schema.addField(AddFieldReq.builder()
                    .fieldName("skills")
                    .dataType(DataType.VarChar)
                    .build()
            );

            schema.addField(AddFieldReq.builder()
                    .fieldName("skills_embedding")
                    .dataType(DataType.FloatVector)
                    .dimension(768)
                    .description("Vector embedding")
                    .build()
            );

            milvusClient.createCollection(CreateCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .collectionSchema(schema)
                    .build()
            );

            IndexParam indexParam = IndexParam.builder()
                    .fieldName("skills_embedding")
                    .indexName("vector_index")
                    .indexType(IndexParam.IndexType.IVF_FLAT)
                    .metricType(IndexParam.MetricType.COSINE)
                    .extraParams(Map.of("nlist", 1024, "nprobe", 1))
                    .build();
            List<IndexParam> indexParams = new ArrayList<>();
            indexParams.add(indexParam);

            CreateIndexReq createIndexReq = CreateIndexReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .indexParams(indexParams)
                    .build();

            milvusClient.createIndex(createIndexReq);
            milvusClient.loadCollection(LoadCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .build()
            );
            logger.debug("Collection {} created", COLLECTION_NAME);
        } else {
            logger.debug("Collection {} already exists", COLLECTION_NAME);
        }
    }
}
