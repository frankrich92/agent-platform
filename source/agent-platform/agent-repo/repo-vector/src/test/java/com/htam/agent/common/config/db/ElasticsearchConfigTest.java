package com.htam.agent.common.config.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.junit.jupiter.api.Test;

class ElasticsearchConfigTest {

    @Test
    void parseHostsSupportsMultipleUrisAndDefaults() {
        ElasticsearchConfig config = new ElasticsearchConfig();

        HttpHost[] hosts = config.parseHosts("localhost:9200,https://search.internal");

        assertEquals(2, hosts.length);
        assertEquals("http", hosts[0].getSchemeName());
        assertEquals("localhost", hosts[0].getHostName());
        assertEquals(9200, hosts[0].getPort());
        assertEquals("https", hosts[1].getSchemeName());
        assertEquals("search.internal", hosts[1].getHostName());
        assertEquals(443, hosts[1].getPort());
    }

    @Test
    void buildAuthHeaderPrefersApiKeyThenBasicAuth() {
        ElasticsearchConfig config = new ElasticsearchConfig();

        Header apiKey = config.buildAuthHeader("user", "pass", "secret");
        Header basic = config.buildAuthHeader("user", "pass", "");

        assertEquals("Authorization", apiKey.getName());
        assertEquals("ApiKey secret", apiKey.getValue());
        assertEquals("Authorization", basic.getName());
        assertEquals("Basic dXNlcjpwYXNz", basic.getValue());
        assertNull(config.buildAuthHeader("", "", ""));
    }
}
