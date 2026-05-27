package com.htam.agent.common.config.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.htam.agent.common.util.RequestHolder;
import com.htam.agent.repo.cache.CacheEntry;
import com.htam.agent.repo.cache.CacheRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class AuthInterceptorTest {

    @AfterEach
    void clearRequestHolder() {
        RequestHolder.clear();
    }

    @Test
    void preHandleClearsRequestHolderWhenAuthenticationFails() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(new EmptyCacheRepository());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handlerMethod("secured"));

        assertFalse(result);
        assertNull(RequestHolder.getRequest());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void preHandleClearsRequestHolderWhenAuthenticationBranchRejectsRequest() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(new EmptyCacheRepository());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "sk-any");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, handlerMethod("secured"));

        assertFalse(result);
        assertNull(RequestHolder.getRequest());
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void afterCompletionClearsRequestHolderAfterSuccessfulPreHandle() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor(new EmptyCacheRepository());
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handlerMethod = handlerMethod("open");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        assertSame(request, RequestHolder.getRequest());

        interceptor.afterCompletion(request, response, handlerMethod, null);

        assertNull(RequestHolder.getRequest());
    }

    private static HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getDeclaredMethod(methodName));
    }

    private static final class TestController {
        void secured() {
        }

        @PassAuth
        void open() {
        }
    }

    private static final class EmptyCacheRepository implements CacheRepository {
        @Override
        public CacheEntry put(String key, Object value, Duration ttl) {
            throw unsupported();
        }

        @Override
        public Optional<Object> get(String key) {
            return Optional.empty();
        }

        @Override
        public boolean contains(String key) {
            return false;
        }

        @Override
        public boolean evict(String key) {
            return false;
        }

        @Override
        public int purgeExpired() {
            return 0;
        }

        private static UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("not needed by this test");
        }
    }
}
