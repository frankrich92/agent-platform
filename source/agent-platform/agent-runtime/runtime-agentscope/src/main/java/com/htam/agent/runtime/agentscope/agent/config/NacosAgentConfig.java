package com.htam.agent.runtime.agentscope.agent.config;

import com.htam.agent.common.KvMap;
import com.htam.agent.common.util.FuncUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Properties;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NacosAgentConfig {
    private String agentName;
    private List<KvMap> nacosProperties;

    public Properties getNacosProperties() {
        Properties properties = new Properties();
        if (nacosProperties == null) {
            return properties;
        }

        nacosProperties.forEach(kvMap -> {
            String value = kvMap.isEvn() ? System.getenv(kvMap.getValue()) : kvMap.getValue();
            if (!FuncUtils.isEmpty(value)) {
                properties.put(kvMap.getKey(), value);
            }
        });

        return properties;
    }
}
