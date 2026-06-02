package com.htam.agent.adapter.rest.capability.provider.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.dto.ModelConfigDTO;
import com.htam.agent.common.entity.ModelConfig;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.mp.support.PageParams;
import com.htam.agent.common.r.R;
import com.htam.agent.common.util.BeanUtils;
import com.htam.agent.common.vo.CheckModelResult;
import com.htam.agent.common.vo.ModelConfigVO;
import com.htam.agent.capability.provider.service.ModelConfigService;
import com.htam.agent.common.wrapper.ModelConfigWrapper;
import com.htam.agent.common.wrapper.ModelWrapper;
import com.htam.agent.runtime.agentscope.model.ChatModelFactory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模型配置Controller
 *
 * @author huxuehao
 */
@RestController
@RequestMapping("/model/config")
@RequiredArgsConstructor
public class ModelConfigController {

    private final ModelConfigService modelConfigService;
    private final ChatModelFactory chatModelFactory;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<IPage<ModelConfigVO>> page(PageParams pageParams, ModelConfigDTO query) {
        IPage<ModelConfig> page = modelConfigService.page(pageParams, query);
        return R.data(BeanUtils.copyPage(page, ModelConfigVO.class));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<ModelConfigVO> detail(@PathVariable("id") Long id) {
        ModelConfig entity = modelConfigService.getById(id);

        ModelConfigVO vo = BeanUtils.copy(entity, ModelConfigVO.class);
        vo.setUsed(modelConfigService.usedWithAgent(List.of(id)));

        return R.data(vo);
    }

    /**
     * 新增
     */
    @PostMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Long> save(@RequestBody ModelConfig entity) {
        modelConfigService.save(entity);
        return R.data(entity.getId());
    }

    /**
     * 修改
     */
    @PutMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Long> update(@RequestBody ModelConfig entity) {
        modelConfigService.doUpdate(entity);
        return R.data(entity.getId());
    }

    /**
     * 删除
     */
    @DeleteMapping
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.data(modelConfigService.deleteByIds(ids));
    }

    /**
     * 被哪些Agent使用
     */
    @PostMapping("used-with-agent")
    public R<List<Object>> usedWithAgent(@RequestBody List<Long> ids) {
        return R.data(modelConfigService.usedWithAgent(ids));
    }

    @RequestMapping("/check/{modelId}")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<CheckModelResult> checkModel(@PathVariable("modelId") Long modelId) {
        updateConnectivityResult(modelId, "CHECKING", null);
        try {
            ModelWrapper config = modelConfigService.getModelWrapperById(modelId);
            ModelConfigWrapper configWrapper = new ModelConfigWrapper();
            config.getConfig().fillModelConfigWrapper(configWrapper);
            config.getProvider().fillModelConfigWrapper(configWrapper);
            Model simpleModel = chatModelFactory.getSimpleModel(configWrapper);
            ReActAgent agent = ReActAgent.builder()
                    .name("CHECK_MODEL_AGENT")
                    .model(simpleModel)
                    .sysPrompt("For user inquiries, you must always respond with \"Connection test successful.\"")
                    .build();
            Msg response = agent.call(Msg.builder().textContent("hello").build()).block();
            if (response == null || response.getTextContent() == null) {
                updateConnectivityResult(modelId, "FAILED", "模型无响应");
                return R.data(new CheckModelResult(false, "模型无响应"));
            }
            updateConnectivityResult(modelId, "CONNECTED", null);
            return R.data(new CheckModelResult(true, "连接成功"));
        } catch (Exception e) {
            updateConnectivityResult(modelId, "FAILED", e.getMessage());
            return R.data(new CheckModelResult(false, e.getMessage()));
        }
    }

    private void updateConnectivityResult(Long modelId, String status, String message) {
        ModelConfig entity = new ModelConfig();
        entity.setId(modelId);
        entity.setConnectivityStatus(status);
        entity.setConnectivityMessage(message);
        entity.setLastConnectivityCheck(LocalDateTime.now());
        modelConfigService.updateById(entity);
    }
}
