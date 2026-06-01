package com.htam.agent.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

/**
 * Agent 元数据存储，用于跨模块共享 Agent 的元数据（如 threadId）
 *
 * @author huxuehao
 **/
public final class AgentMetadataStore {
    private static final Map<Object, Map<String, Object>> STORE = new ConcurrentHashMap<>();

    private AgentMetadataStore() {}

    /**
     * 设置 Agent 元数据
     *
     * @param agent Agent 实例或兼容的历史标识
     * @param key   元数据键
     * @param value 元数据值
     */
    public static void put(Object agent, String key, Object value) {
        STORE.computeIfAbsent(storeKey(agent), ignored -> new ConcurrentHashMap<>()).put(key, value);
    }

    /**
     * 获取 Agent 元数据
     *
     * @param agent Agent 实例或兼容的历史标识
     * @param key   元数据键
     * @param <T>   值类型
     * @return 元数据值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Object agent, String key) {
        Map<String, Object> meta = STORE.get(storeKey(agent));
        return meta == null ? null : (T) meta.get(key);
    }

    /**
     * 获取 Agent 全部元数据
     *
     * @param agent Agent 实例或兼容的历史标识
     * @return 元数据 Map
     */
    public static Map<String, Object> getAll(Object agent) {
        Map<String, Object> meta = STORE.get(storeKey(agent));
        return meta == null ? Map.of() : Map.copyOf(meta);
    }

    /**
     * 移除 Agent 元数据
     *
     * @param agent Agent 实例或兼容的历史标识
     */
    public static void remove(Object agent) {
        STORE.remove(storeKey(agent));
    }

    public static int size() {
        return STORE.size();
    }

    public static int removeIf(BiPredicate<Object, Map<String, Object>> predicate) {
        AtomicInteger removedCount = new AtomicInteger();
        STORE.forEach((key, meta) -> {
            if (predicate.test(ownerOf(key), Map.copyOf(meta)) && STORE.remove(key, meta)) {
                removedCount.incrementAndGet();
            }
        });
        return removedCount.get();
    }

    static void clear() {
        STORE.clear();
    }

    private static Object storeKey(Object agent) {
        if (agent == null) {
            throw new IllegalArgumentException("agent 不能为空");
        }
        if (agent instanceof CharSequence || agent instanceof Number) {
            return agent;
        }
        return new IdentityKey(agent);
    }

    private static Object ownerOf(Object key) {
        return key instanceof IdentityKey identityKey ? identityKey.agent : key;
    }

    private static final class IdentityKey {
        private final Object agent;
        private final int hashCode;

        private IdentityKey(Object agent) {
            this.agent = agent;
            this.hashCode = System.identityHashCode(agent);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof IdentityKey identityKey && agent == identityKey.agent;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
