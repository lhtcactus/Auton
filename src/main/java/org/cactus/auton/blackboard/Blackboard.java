package org.cactus.auton.blackboard;

/**
 * 黑板接口，用于节点间共享数据。
 */
public interface Blackboard {

    /**
     * 写入数据到黑板。
     *
     * @param key   键
     * @param value 值
     */
    void put(String key, Object value);

    /**
     * 从黑板读取数据。
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值，不存在时返回 null
     */
    <T> T get(String key);
}
