package com.htam.agent.repo.mybatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

class JsonNodeTypeHandlerTest {

    private final JsonNodeTypeHandler handler = new JsonNodeTypeHandler(JsonNode.class);

    @Test
    void parseReadsNormalJsonObject() {
        JsonNode node = (JsonNode) handler.parse("{\"url\":\"https://example.com/mcp\"}");

        assertEquals("https://example.com/mcp", node.get("url").asText());
    }

    @Test
    void parseReadsJsonStringObject() {
        JsonNode node = (JsonNode) handler.parse("\"{\\\"url\\\":\\\"https://example.com/mcp\\\"}\"");

        assertEquals("https://example.com/mcp", node.get("url").asText());
    }

    @Test
    void parseReadsLegacyEscapedJsonObject() {
        JsonNode node = (JsonNode) handler.parse(
                "{\\\"url\\\":\\\"https://mcp.amap.com/mcp\\\","
                        + "\\\"queryParams\\\":[{\\\"key\\\":\\\"key\\\",\\\"value\\\":\\\"yourself key\\\"}],"
                        + "\\\"headers\\\":[]}");

        assertEquals("https://mcp.amap.com/mcp", node.get("url").asText());
        assertEquals("key", node.get("queryParams").get(0).get("key").asText());
    }

    @Test
    void parseStillRejectsInvalidJson() {
        assertThrows(RuntimeException.class, () -> handler.parse("{bad json}"));
    }
}
