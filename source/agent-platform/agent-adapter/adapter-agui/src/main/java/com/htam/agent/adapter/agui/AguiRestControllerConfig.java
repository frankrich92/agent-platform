package com.htam.agent.adapter.agui;

import io.agentscope.spring.boot.agui.common.AguiProperties;
import io.agentscope.spring.boot.agui.mvc.AguiMvcEndpoint;
import io.agentscope.spring.boot.agui.mvc.AguiRestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass({AguiMvcEndpoint.class, AguiRestController.class})
public class AguiRestControllerConfig {

    @Bean
    @Primary
    public AguiRestController aguiRestController(AguiMvcEndpoint aguiMvcEndpoint, AguiProperties props) {
        return new AguiRestController(
                aguiMvcEndpoint,
                props.getPathPrefix(),
                props.isEnablePathRouting());
    }
}
