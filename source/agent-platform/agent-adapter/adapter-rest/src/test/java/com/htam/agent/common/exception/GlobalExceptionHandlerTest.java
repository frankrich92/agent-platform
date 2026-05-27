package com.htam.agent.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.htam.agent.common.r.R;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolationHandlerHandlesNullMessage() {
        R<?> response = handler.dataIntegrityViolationHandler(new DataIntegrityViolationException(null));

        assertEquals(400, response.getCode());
        assertEquals("数据校验失败，请检查输入数据", response.getMsg());
    }

    @Test
    void sqlExceptionHandlerHandlesNullSqlStateAndMessage() {
        R<?> response = handler.sqlExceptionHandler(new SQLException((String) null, (String) null));

        assertEquals(500, response.getCode());
        assertEquals("数据库操作失败，请稍后重试", response.getMsg());
    }

    @Test
    void sqlExceptionHandlerKeepsGenericHy000AsDatabaseOperationFailure() {
        R<?> response = handler.sqlExceptionHandler(new SQLException("generic database error", "HY000"));

        assertEquals(500, response.getCode());
        assertEquals("数据库操作失败，请稍后重试", response.getMsg());
    }
}
