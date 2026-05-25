package com.htam.agent.repo.mybatis.type;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * JSON node type handler used by the MyBatis persistence adapter.
 */
@MappedTypes(value = {Object.class, JsonNode.class})
@MappedJdbcTypes(value = {JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.OTHER})
public class JsonNodeTypeHandler extends AbstractJsonTypeHandler<Object> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonNodeTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public Object parse(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return parseJsonNode(json);
        } catch (Exception e) {
            return parseEscapedJsonNode(json, e);
        }
    }

    @Override
    public String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String text) {
            return text;
        }
        if (obj instanceof JsonNode node && node.isNull()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    private JsonNode parseJsonNode(String json) throws JsonProcessingException {
        JsonNode node = OBJECT_MAPPER.readTree(json);
        if (node != null && node.isTextual() && looksLikeJson(node.asText())) {
            return parseJsonNode(node.asText());
        }
        return node;
    }

    private JsonNode parseEscapedJsonNode(String json, Exception cause) {
        String text = json.trim();
        if (!looksLikeEscapedJson(text)) {
            throw new RuntimeException("JSON反序列化失败", cause);
        }
        try {
            String decoded = OBJECT_MAPPER.readValue("\"" + text + "\"", String.class);
            return parseJsonNode(decoded);
        } catch (Exception decodeException) {
            throw new RuntimeException("JSON反序列化失败", cause);
        }
    }

    private boolean looksLikeJson(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private boolean looksLikeEscapedJson(String text) {
        return text.startsWith("{\\\"") || text.startsWith("[\\\"");
    }
}
