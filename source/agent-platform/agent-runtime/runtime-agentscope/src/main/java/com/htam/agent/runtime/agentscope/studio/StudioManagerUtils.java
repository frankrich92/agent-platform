package com.htam.agent.runtime.agentscope.studio;

import com.htam.agent.common.vo.AccountVO;
import com.htam.agent.runtime.agentscope.agui.AgentContext;
import io.agentscope.core.studio.StudioManager;

/**
 * 描述：StudioManagerUtils
 *
 * @author huxuehao
 **/
public class StudioManagerUtils {
    public static void initOnce(String url, String project, String agentCode) {
        String runName = calculateRunName(agentCode);
        synchronized (StudioManagerUtils.class) {
            StudioManager.init()
                    .studioUrl(url)
                    .project(project)
                    .runName(runName)
                    .initialize()
                    .block();
        }
    }

    private static String calculateRunName(String agentCode) {
        AgentContext agentContext = AgentContext.get();
        AccountVO userInfo = agentContext.getUserInfo();
        if (userInfo == null) {
            return agentCode;
        }
        return userInfo.getUsername() + "_" + agentCode;
    }
}
