package org.cactus.auton.blackboard;

import java.util.HashMap;
import java.util.Map;

/**
 * 黑板默认实现，基于 HashMap 的内存存储。
 */
public class DefaultBlackboard implements Blackboard {
    private final Map<String, Object> map = new HashMap<>();
    @Override
    public void put(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public <T> T get(String key) {
        Object obj = map.get(key);
        return obj == null ? null : (T) obj;
    }
}
