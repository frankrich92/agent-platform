package com.htam.agent.governance.iam.account.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import com.htam.agent.common.util.CryptoUtils;
import org.junit.jupiter.api.Test;

class AccountServiceImplTest {

    @Test
    void passwordMatchesAcceptsAllMigratedPasswordFormats() throws Exception {
        AccountServiceImpl service = service();
        String rawPassword = "admin123";
        String salt = "1";
        String clientMd5 = CryptoUtils.md5(rawPassword);

        assertTrue(passwordMatches(service, rawPassword, salt, CryptoUtils.md5(rawPassword, salt)));
        assertTrue(passwordMatches(service, rawPassword, salt, CryptoUtils.md5(clientMd5, salt)));
        assertTrue(passwordMatches(service, rawPassword, salt, clientMd5));
        assertTrue(passwordMatches(service, clientMd5, salt, clientMd5));
    }

    @Test
    void passwordMatchesRejectsBlankOrDifferentPassword() throws Exception {
        AccountServiceImpl service = service();

        assertFalse(passwordMatches(service, "admin123", "1", CryptoUtils.md5("other", "1")));
        assertFalse(passwordMatches(service, "", "1", CryptoUtils.md5("admin123", "1")));
        assertFalse(passwordMatches(service, "admin123", "1", ""));
    }

    private static AccountServiceImpl service() {
        return new AccountServiceImpl(null, null, null, null, null, null, null);
    }

    private static boolean passwordMatches(AccountServiceImpl service, String requestPassword, String salt,
            String storedPassword) throws Exception {
        Method method = AccountServiceImpl.class.getDeclaredMethod(
                "passwordMatches", String.class, String.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(service, requestPassword, salt, storedPassword);
    }
}
