package com.htam.agent.repo.mybatis.knowledge;

import com.htam.agent.repo.knowledge.PgVectorRepository;
import com.htam.agent.repo.knowledge.VectorSearchRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@ConditionalOnProperty(name = "rag.store", havingValue = "pgvector")
public class PgVectorMybatisRepository implements PgVectorRepository {
    private static final Logger log = LoggerFactory.getLogger(PgVectorMybatisRepository.class);

    private static final int[] SUPPORTED_DIMENSIONS = {64, 128, 256, 512, 768, 1024, 2048, 2560};

    private final JdbcTemplate pgJdbcTemplate;

    public PgVectorMybatisRepository(
            @Autowired(required = false) @Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        if (pgVectorDataSource == null) {
            this.pgJdbcTemplate = null;
            log.warn("PgVector数据源未配置，本地RAG功能不可用");
            return;
        }

        this.pgJdbcTemplate = new JdbcTemplate(pgVectorDataSource);
        initSchema();
    }

    @Override
    public boolean isAvailable() {
        return pgJdbcTemplate != null;
    }

    @Override
    public void storeEmbedding(Long id, Long chunkId, Long documentId, Long knowledgeBaseConfigId, float[] embedding) {
        String tableName = getTableName(embedding.length);
        String vectorStr = arrayToVectorString(embedding);
        String sql = "INSERT INTO " + tableName + " (id, chunk_id, document_id, knowledge_base_config_id, embedding) "
                + "VALUES (?, ?, ?, ?, ?::vector)";
        pgJdbcTemplate.update(sql, id, chunkId, documentId, knowledgeBaseConfigId, vectorStr);
    }

    @Override
    public List<VectorSearchRecord> search(float[] queryEmbedding, Long knowledgeBaseConfigId, int limit) {
        String tableName = getTableName(queryEmbedding.length);
        String vectorStr = arrayToVectorString(queryEmbedding);
        String sql = "SELECT chunk_id, document_id, 1 - (embedding <=> ?::vector) AS score "
                + "FROM " + tableName + " "
                + "WHERE knowledge_base_config_id = ? "
                + "ORDER BY embedding <=> ?::vector "
                + "LIMIT ?";

        List<Map<String, Object>> rows = pgJdbcTemplate.queryForList(sql, vectorStr, knowledgeBaseConfigId, vectorStr, limit);
        List<VectorSearchRecord> results = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            results.add(new VectorSearchRecord(
                    ((Number) row.get("chunk_id")).longValue(),
                    ((Number) row.get("document_id")).longValue(),
                    ((Number) row.get("score")).doubleValue()));
        }
        return results;
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        for (int dim : SUPPORTED_DIMENSIONS) {
            pgJdbcTemplate.update("DELETE FROM " + getTableName(dim) + " WHERE document_id = ?", documentId);
        }
    }

    @Override
    public void deleteByKnowledgeBaseConfigId(Long knowledgeBaseConfigId) {
        for (int dim : SUPPORTED_DIMENSIONS) {
            pgJdbcTemplate.update(
                    "DELETE FROM " + getTableName(dim) + " WHERE knowledge_base_config_id = ?",
                    knowledgeBaseConfigId);
        }
    }

    @Override
    public void deleteByChunkId(Long chunkId) {
        for (int dim : SUPPORTED_DIMENSIONS) {
            pgJdbcTemplate.update("DELETE FROM " + getTableName(dim) + " WHERE chunk_id = ?", chunkId);
        }
    }

    private void initSchema() {
        try {
            pgJdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            for (int dim : SUPPORTED_DIMENSIONS) {
                String embeddingType = dim > 2000 ? "halfvec" : "vector";
                String tableName = getTableName(dim);
                pgJdbcTemplate.execute(String.format("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id BIGINT PRIMARY KEY,
                        chunk_id BIGINT NOT NULL,
                        document_id BIGINT NOT NULL,
                        knowledge_base_config_id BIGINT NOT NULL,
                        embedding %s(%d) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                """, tableName, embeddingType, dim));
                pgJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_embedding_kbc_" + dim
                        + " ON " + tableName + "(knowledge_base_config_id)");
                pgJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_embedding_doc_" + dim
                        + " ON " + tableName + "(document_id)");
                pgJdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_rag_vectors_embedding_" + dim
                        + " ON " + tableName + " USING hnsw (embedding " + embeddingType + "_cosine_ops)");
            }
            log.info("PgVector表结构初始化完成，共{}张表", SUPPORTED_DIMENSIONS.length);
        } catch (Exception e) {
            log.error("PgVector表结构初始化失败", e);
        }
    }

    private String getTableName(int dimension) {
        return "rag_embedding_" + dimension;
    }

    private String arrayToVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
