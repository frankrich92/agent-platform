package com.htam.agent.capability.knowledge.rag.store.impl;

import com.htam.agent.capability.knowledge.rag.EmbeddingRecord;
import com.htam.agent.capability.knowledge.rag.RetrievalResult;
import com.htam.agent.capability.knowledge.rag.store.VectorStore;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Weaviate 向量存储实现。
 */
@Component
@ConditionalOnProperty(name = "rag.store", havingValue = "weaviate")
public class WeaviateVectorStore implements VectorStore {

    private static final Logger log = LoggerFactory.getLogger(WeaviateVectorStore.class);
    private static final int[] SUPPORTED_DIMENSIONS = {64, 128, 256, 512, 768, 1024, 2048, 2560};

    private final WeaviateClient weaviateClient;
    private final String classPrefix;

    public WeaviateVectorStore(@Autowired(required = false) WeaviateClient weaviateClient,
                               @Value("${rag.weaviate.class-prefix:AgentRag}") String classPrefix) {
        this.weaviateClient = weaviateClient;
        this.classPrefix = classPrefix;
        if (weaviateClient != null) {
            initSchema();
        } else {
            log.warn("Weaviate客户端未配置，本地RAG功能不可用");
        }
    }

    String getClassName(int dimension) {
        return classPrefix + "_" + dimension;
    }

    String toObjectId(Long id) {
        return UUID.nameUUIDFromBytes(String.valueOf(id).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private void initSchema() {
        try {
            for (int dimension : SUPPORTED_DIMENSIONS) {
                ensureClassExists(getClassName(dimension), dimension);
            }
            log.info("Weaviate schema初始化完成，共{}个class", SUPPORTED_DIMENSIONS.length);
        } catch (Exception e) {
            log.error("Weaviate schema初始化失败", e);
        }
    }

    private void ensureClassExists(String className, int dimension) {
        try {
            Result<Boolean> existsResult = weaviateClient.schema().exists()
                    .withClassName(className)
                    .run();
            if (Boolean.TRUE.equals(existsResult.getResult())) {
                return;
            }
        } catch (Exception e) {
            log.warn("检查Weaviate class是否存在时出错: {}", e.getMessage());
        }

        List<Property> properties = List.of(
                Property.builder().name("chunk_id").dataType(List.of(DataType.TEXT)).build(),
                Property.builder().name("document_id").dataType(List.of(DataType.TEXT)).build(),
                Property.builder().name("knowledge_base_config_id").dataType(List.of(DataType.TEXT)).build()
        );

        WeaviateClass clazz = WeaviateClass.builder()
                .className(className)
                .description("RAG embedding vectors for dimension " + dimension)
                .properties(properties)
                .vectorizer("none")
                .build();

        Result<Boolean> createResult = weaviateClient.schema().classCreator()
                .withClass(clazz)
                .run();
        if (createResult.getError() != null) {
            log.warn("Weaviate class创建失败（可能已存在）: {}", createResult.getError().getMessages());
        } else {
            log.info("自动创建Weaviate class: {}, dimension={}", className, dimension);
        }
    }

    @Override
    public boolean isAvailable() {
        return weaviateClient != null;
    }

    @Override
    public void storeEmbedding(Long id, Long chunkId, Long documentId,
                               Long knowledgeBaseConfigId, float[] embedding) {
        if (!isAvailable()) {
            throw new RuntimeException("Weaviate客户端未配置");
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("chunk_id", String.valueOf(chunkId));
        properties.put("document_id", String.valueOf(documentId));
        properties.put("knowledge_base_config_id", String.valueOf(knowledgeBaseConfigId));

        Result<io.weaviate.client.v1.data.model.WeaviateObject> result = weaviateClient.data().creator()
                .withID(toObjectId(id))
                .withClassName(getClassName(embedding.length))
                .withProperties(properties)
                .withVector(boxVector(embedding))
                .run();

        if (result.getError() != null) {
            String errorMsg = result.getError().getMessages().stream()
                    .map(io.weaviate.client.base.WeaviateErrorMessage::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Weaviate向量存储失败: " + errorMsg);
        }
    }

    @Override
    public void storeEmbeddings(List<EmbeddingRecord> records) {
        if (!isAvailable()) {
            throw new RuntimeException("Weaviate客户端未配置");
        }
        if (records == null || records.isEmpty()) {
            return;
        }
        for (EmbeddingRecord record : records) {
            storeEmbedding(record.id(), record.chunkId(), record.documentId(),
                    record.knowledgeBaseConfigId(), record.embedding());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<RetrievalResult> search(float[] queryEmbedding, Long knowledgeBaseConfigId,
                                        int limit, double scoreThreshold) {
        if (!isAvailable()) {
            throw new RuntimeException("Weaviate客户端未配置");
        }

        Field additional = Field.builder()
                .name("_additional")
                .fields(Field.builder().name("distance").build(), Field.builder().name("certainty").build())
                .build();

        Result<GraphQLResponse> result = weaviateClient.graphQL().get()
                .withClassName(getClassName(queryEmbedding.length))
                .withFields(
                        Field.builder().name("chunk_id").build(),
                        Field.builder().name("document_id").build(),
                        Field.builder().name("knowledge_base_config_id").build(),
                        additional)
                .withWhere(WhereArgument.builder().filter(WhereFilter.builder()
                        .path("knowledge_base_config_id")
                        .operator("Equal")
                        .valueText(String.valueOf(knowledgeBaseConfigId))
                        .build()).build())
                .withNearVector(NearVectorArgument.builder().vector(boxVector(queryEmbedding)).build())
                .withLimit(limit)
                .run();

        if (result.getError() != null) {
            String errorMsg = result.getError().getMessages().stream()
                    .map(io.weaviate.client.base.WeaviateErrorMessage::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Weaviate向量检索失败: " + errorMsg);
        }

        if (result.getResult() == null || result.getResult().getData() == null) {
            return List.of();
        }
        Map<String, Object> data = (Map<String, Object>) result.getResult().getData();
        Map<String, Object> get = (Map<String, Object>) data.get("Get");
        if (get == null) {
            return List.of();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) get.get(getClassName(queryEmbedding.length));
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<RetrievalResult> retrievalResults = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Map<String, Object> additionalFields = (Map<String, Object>) item.get("_additional");
            if (additionalFields == null) {
                continue;
            }
            double score = resolveScore(additionalFields);
            if (score < scoreThreshold) {
                continue;
            }

            Object chunkId = item.get("chunk_id");
            Object documentId = item.get("document_id");
            if (chunkId == null || documentId == null) {
                continue;
            }
            retrievalResults.add(new RetrievalResult(
                    Long.parseLong(String.valueOf(chunkId)),
                    Long.parseLong(String.valueOf(documentId)),
                    score));
        }

        return retrievalResults;
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        deleteByField("document_id", documentId);
    }

    @Override
    public void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId) {
        deleteByField("knowledge_base_config_id", knowledgeBaseConfigId);
    }

    @Override
    public void deleteByChunkId(Long chunkId) {
        deleteByField("chunk_id", chunkId);
    }

    private void deleteByField(String fieldName, Long value) {
        if (!isAvailable()) {
            return;
        }
        for (int dimension : SUPPORTED_DIMENSIONS) {
            String className = getClassName(dimension);
            WhereFilter whereFilter = WhereFilter.builder()
                    .path(fieldName)
                    .operator("Equal")
                    .valueText(String.valueOf(value))
                    .build();
            try {
                weaviateClient.batch().objectsBatchDeleter()
                        .withClassName(className)
                        .withWhere(whereFilter)
                        .run();
            } catch (Exception e) {
                log.warn("Weaviate删除失败, class={}, field={}, value={}", className, fieldName, value, e);
            }
        }
    }

    private Float[] boxVector(float[] vector) {
        Float[] boxed = new Float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            boxed[i] = vector[i];
        }
        return boxed;
    }

    private double resolveScore(Map<String, Object> additionalFields) {
        Object distance = additionalFields.get("distance");
        if (distance instanceof Number distanceNumber) {
            return 1.0 - distanceNumber.doubleValue() / 2.0;
        }
        Object certainty = additionalFields.get("certainty");
        if (certainty instanceof Number certaintyNumber) {
            return certaintyNumber.doubleValue();
        }
        return Double.NEGATIVE_INFINITY;
    }
}
