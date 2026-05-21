package com.htam.agent.common.config.mybatis;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 描述：JSON 节点类型处理器
 *
 * @author huxuehao
 */
@MappedTypes(value = { Object.class, JsonNode.class})
@MappedJdbcTypes(value = { JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.OTHER })
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
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    @Override
    public String toJson(Object  obj) {
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
}
