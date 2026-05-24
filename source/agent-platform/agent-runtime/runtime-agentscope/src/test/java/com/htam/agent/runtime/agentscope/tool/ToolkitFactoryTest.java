package com.htam.agent.runtime.agentscope.tool;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.entity.ToolConfig;
import org.junit.jupiter.api.Test;

class ToolkitFactoryTest {

    @Test
    void needConfirmTreatsNullAsFalse() {
        ToolConfig toolConfig = new ToolConfig();

        assertFalse(ToolkitFactory.needConfirm(toolConfig));
    }

    @Test
    void needConfirmOnlyTrueForExplicitTrue() {
        ToolConfig toolConfig = new ToolConfig();
        toolConfig.setNeedConfirm(false);
        assertFalse(ToolkitFactory.needConfirm(toolConfig));

        toolConfig.setNeedConfirm(true);
        assertTrue(ToolkitFactory.needConfirm(toolConfig));
    }
}
