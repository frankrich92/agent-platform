package com.htam.agent.repo.mybatis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htam.agent.common.consts.TableConst;
import com.htam.agent.common.entity.AgentDefinition;
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
    void exposesJsonFieldMetadataAsMybatisTypeHandler() throws NoSuchFieldException {
        TableField tableField =
                handler.getAnnotation(AgentDefinition.class.getDeclaredField("modelParamsOverride"), TableField.class);

        assertEquals(JsonNodeTypeHandler.class, tableField.typeHandler());
    }
}
