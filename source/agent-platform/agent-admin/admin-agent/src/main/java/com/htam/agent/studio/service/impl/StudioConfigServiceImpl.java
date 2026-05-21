package com.htam.agent.studio.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.StudioConfig;
import com.htam.agent.studio.mapper.StudioConfigMapper;
import com.htam.agent.studio.service.AgentStudioService;
import com.htam.agent.studio.service.StudioConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：StudioConfigServiceImpl
 *
 * @author huxuehao
 **/
@Service
@RequiredArgsConstructor
public class StudioConfigServiceImpl extends ServiceImpl<StudioConfigMapper, StudioConfig> implements StudioConfigService {
    private final JdbcTemplate jdbcTemplate;
    private final AgentStudioService agentStudioService;

    @Override
    public List<Object> usedWithAgent(List<Long> ids) {
        List<Object> names = new ArrayList<>();
        getAgentDefinitions(agentStudioService.getAgentIds(ids)).forEach(agentDefinition -> {
            names.add(agentDefinition.getName());
        });

        return names;
    }

    private List<AgentDefinition> getAgentDefinitions(List<Long> agentIds) {
        if (agentIds == null || agentIds.isEmpty()) {
            return new ArrayList<>();
        }

        String subSql = agentIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = String.format("SELECT * FROM %s WHERE id IN (%s)", TableConst.AGENT, subSql);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AgentDefinition agent = new AgentDefinition();
            // 手动映射字段
            agent.setId(rs.getLong("id"));
            agent.setName(rs.getString("name"));
            agent.setDescription(rs.getString("description"));
            return agent;
        });
    }
}
