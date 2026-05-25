package com.htam.agent.repo.mybatis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.entity.AgentDefinition;
import com.htam.agent.common.entity.AgentRun;
import com.htam.agent.common.entity.AgentRunStep;
import com.htam.agent.common.entity.AgentToolCall;
import com.htam.agent.common.entity.ChatMessage;
import com.htam.agent.repo.mybatis.type.JsonNodeTypeHandler;
import org.junit.jupiter.api.Test;

class DomainMybatisAnnotationHandlerTest {

    private final DomainMybatisAnnotationHandler handler = new DomainMybatisAnnotationHandler();

    @Test
    void exposesDomainTableMetadataAsMybatisTableName() {
        TableName tableName = handler.getAnnotation(AgentDefinition.class, TableName.class);

        assertEquals(TableConst.AGENT, tableName.value());
        assertTrue(tableName.autoResultMap());
    }

    @Test
    void exposesDomainIdMetadataAsMybatisTableId() throws NoSuchFieldException {
        TableId tableId = handler.getAnnotation(ChatMessage.class.getDeclaredField("id"), TableId.class);

        assertEquals(IdType.AUTO, tableId.type());
    }

    @Test
    void exposesRunLedgerDomainMetadata() throws NoSuchFieldException {
        TableName runTable = handler.getAnnotation(AgentRun.class, TableName.class);
        TableId runId = handler.getAnnotation(AgentRun.class.getDeclaredField("runId"), TableId.class);
        TableName stepTable = handler.getAnnotation(AgentRunStep.class, TableName.class);
        TableId stepId = handler.getAnnotation(AgentRunStep.class.getDeclaredField("stepId"), TableId.class);
        TableName toolCallTable = handler.getAnnotation(AgentToolCall.class, TableName.class);
        TableId toolCallId = handler.getAnnotation(AgentToolCall.class.getDeclaredField("toolCallId"), TableId.class);

        assertEquals(TableConst.AGENT_RUN, runTable.value());
        assertEquals(IdType.INPUT, runId.type());
        assertEquals(TableConst.AGENT_RUN_STEP, stepTable.value());
        assertEquals(IdType.INPUT, stepId.type());
        assertEquals(TableConst.AGENT_TOOL_CALL, toolCallTable.value());
        assertEquals(IdType.INPUT, toolCallId.type());
    }

    @Test
    void exposesJsonFieldMetadataAsMybatisTypeHandler() throws NoSuchFieldException {
        TableField tableField =
                handler.getAnnotation(AgentDefinition.class.getDeclaredField("modelParamsOverride"), TableField.class);

        assertEquals(JsonNodeTypeHandler.class, tableField.typeHandler());
    }
}
