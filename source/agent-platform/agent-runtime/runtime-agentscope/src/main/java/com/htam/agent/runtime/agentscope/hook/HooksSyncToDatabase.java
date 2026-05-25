package com.htam.agent.runtime.agentscope.hook;

import com.htam.agent.common.entity.HookConfig;
import com.htam.agent.common.enums.HookType;
import com.htam.agent.common.wrapper.HookConfigWrapper;
import com.htam.agent.repo.capability.HookConfigRepository;
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
public class HooksSyncToDatabase implements ApplicationRunner {
    private final HookConfigRepository hookConfigRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("IAgentHooks sync to DB starting");
        ArrayList<HookConfigWrapper> hookConfigWrappers = new ArrayList<>();
        HooksRegister.getHooks().forEach(hook -> {
            hookConfigWrappers.add(HookConfigWrapper.builder()
                    .name(((IAgentHook)hook).getName())
                    .description(((IAgentHook)hook).getDescription())
                    .classPath(hook.getClass().getName())
                    .build());
        });

        syncConfigToDatabase(hookConfigWrappers);
        log.info("IAgentHooks sync to DB completed");
    }

    private void syncConfigToDatabase(ArrayList<HookConfigWrapper> configWrappers) {
        hookConfigRepository.deleteBuiltinNotInClassPaths(configWrappers.stream()
                .map(HookConfigWrapper::getClassPath)
                .toList());
        configWrappers.forEach(configWrapper -> {
            java.util.List<HookConfig> list = hookConfigRepository.listByClassPath(configWrapper.getClassPath());
            if (list.isEmpty()) {
                HookConfig hookConfig = new HookConfig();
                hookConfig.setName(configWrapper.getName());
                hookConfig.setHookType(HookType.BUILTIN);
                hookConfig.setDescription(configWrapper.getDescription());
                hookConfig.setClassPath(configWrapper.getClassPath());
                hookConfig.setEnabled(true);
                hookConfig.setPriority(1);
                hookConfigRepository.save(hookConfig);
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (i == 0) {
                        list.get(i).setHookType(HookType.BUILTIN);
                        list.get(i).setClassPath(configWrapper.getClassPath());
                        hookConfigRepository.updateById(list.get(i));
                    } else {
                        hookConfigRepository.deleteById(list.get(i).getId());
                    }
                }
            }
        });
    }
}
