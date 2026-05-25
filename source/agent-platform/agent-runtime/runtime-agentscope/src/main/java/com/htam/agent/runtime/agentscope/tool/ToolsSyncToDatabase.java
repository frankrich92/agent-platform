package com.htam.agent.runtime.agentscope.tool;

import com.htam.agent.common.entity.ToolConfig;
import com.htam.agent.common.enums.ToolType;
import com.htam.agent.common.util.JsonUtils;
import com.htam.agent.common.wrapper.ToolInfoWrapper;
import com.htam.agent.repo.capability.ToolConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 描述：钩子加载到数据库
 *
 * @author huxuehao
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolsSyncToDatabase implements ApplicationRunner {
    private final ToolConfigRepository toolConfigRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("IAgentTool sync to DB starting");
        ArrayList<ToolInfoWrapper> toolInfoWrappers = new ArrayList<>();
        ToolsRegister.getTools().forEach(toolInfo -> {
            toolInfoWrappers.add(toolInfo.parseToolInfo());
        });

        syncConfigToDatabase(toolInfoWrappers);
        log.info("IAgentTool sync to DB completed");
    }

    private void syncConfigToDatabase(ArrayList<ToolInfoWrapper> toolInfos) {
        toolConfigRepository.deleteBuiltinNotInClassPaths(toolInfos.stream()
                .map(ToolInfoWrapper::getClassPath)
                .toList());
        toolInfos.forEach(toolInfo -> {
            java.util.List<ToolConfig> list = toolConfigRepository.listBuiltinByClassPath(toolInfo.getClassPath());
            if (list.isEmpty()) {
                ToolConfig toolConfig = new ToolConfig();
                toolConfig.setName(toolInfo.getName());
                toolConfig.setToolId(toolInfo.getName());
                toolConfig.setToolType(ToolType.BUILTIN);
                toolConfig.setCategory("内置");
                toolConfig.setDescription(toolInfo.getDescription());
                toolConfig.setClassPath(toolInfo.getClassPath());
                toolConfig.setInputSchema(JsonUtils.toJsonNode(toolInfo.getParams()));
                toolConfig.setNeedConfirm(false);
                toolConfig.setEnabled(true);
                toolConfigRepository.save(toolConfig);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        list.get(i).setToolId(toolInfo.getName());
                        list.get(i).setToolType(ToolType.BUILTIN);
                        list.get(i).setClassPath(toolInfo.getClassPath());
                        list.get(i).setInputSchema(JsonUtils.toJsonNode(toolInfo.getParams()));
                        if (list.get(i).getNeedConfirm() == null) {
                            list.get(i).setNeedConfirm(false);
                        }
                        toolConfigRepository.updateById(list.get(i));
                    } else {
                        toolConfigRepository.deleteById(list.get(i).getId());
                    }
                }
            }
        });
    }
}
