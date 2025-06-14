package com.example.restservice.skills;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.*;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SkillsManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    final String COLLECTION_NAME = "skills";

    @Autowired
    VectorStore vectorStore;

    @Autowired
    MilvusClientV2 milvusClient;

    @Autowired
    OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private Gson gson;

    String insertSkills(String username, String[] skills) {

        String skillsStr = String.join(", ", skills);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("username", username);
        requestJson.addProperty("skills", skillsStr);

        Embedding embedding = getEmbedding(skills);
        requestJson.add("skills_embedding", gson.toJsonTree(embedding.getOutput()));

        List<JsonObject> toInsert = new ArrayList<>();
        toInsert.add(requestJson);
        InsertReq insertReq = InsertReq.builder()
                .data(toInsert)
                .collectionName(COLLECTION_NAME)
                .build();
        InsertResp insertResp = milvusClient.insert(insertReq);
        long totalInserted = insertResp.getInsertCnt();
        List<Object> pks = insertResp.getPrimaryKeys();
        logger.debug("Added {} records", totalInserted);

        return pks.get(0).toString();
    }

    Embedding getEmbedding(String[] skills) {
        List<String> skillsList = List.of(skills);
        EmbeddingOptions options = EmbeddingOptionsBuilder.builder()
                .build();
        EmbeddingRequest request = new EmbeddingRequest(skillsList, options);
        EmbeddingResponse response = openAiEmbeddingModel.call(request);
        return response.getResult();
    }

    List<SkilledPerson> searchSkills(String skill) {

        List<String> outputFields = List.of("user_id", "username", "skills");

        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("drop_ratio_search", 0.2);

        String query = String.format("Who has %s as a skill?", skill);
        Embedding embeddedQuery = getEmbedding(new String[]{query});
        logger.debug("Got embedding for query {}", embeddedQuery.getOutput());

        SearchReq searchRequest = SearchReq.builder()
                .collectionName(COLLECTION_NAME)
                .data(Collections.singletonList(new FloatVec(embeddedQuery.getOutput())))
                .annsField("skills_embedding")
                .topK(3)
                .searchParams(searchParams)
                .outputFields(outputFields).build();

        List<SkilledPerson> skilledPersons = new ArrayList<>();

        SearchResp searchResp = milvusClient.search(searchRequest);
        List<List<SearchResp.SearchResult>> results = searchResp.getSearchResults();
        results.forEach(searchResults -> {
            searchResults.forEach(searchResult -> {
                logger.info(searchResult.toString());
                Map<String, Object> data = searchResult.getEntity();
                SkilledPerson skilledPerson = new SkilledPerson();
                skilledPerson.setUserId(data.get("user_id").toString());
                skilledPerson.setSkills(data.get("skills").toString());
                skilledPerson.setUsername(data.get("username").toString());
                skilledPersons.add(skilledPerson);
            });
        });

        return skilledPersons;
    }
}
