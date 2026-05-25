package io.agentscope.core.agui.observer;

import io.agentscope.core.agui.event.AguiEvent;

public interface AguiRunEventObserver {

    void onEvent(AguiRunEventContext context, AguiEvent event);
}
