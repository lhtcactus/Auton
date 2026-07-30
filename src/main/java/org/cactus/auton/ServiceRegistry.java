package org.cactus.auton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局服务注册表。
 * <p>静态容器，用于框架内各组件的依赖注入。
 * 引擎在构造时自动注册，用户也可手动注册自定义实现。</p>
 */
public class ServiceRegistry {

    private static final Map<Class<?>, Object> SERVICES = new ConcurrentHashMap<>();

    /** 获取已注册的服务实例。 */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<?> type) {
        return (T) SERVICES.get(type);
    }

    /** 注册服务实例。 */
    public static <T> void register(Class<T> type, T instance) {
        SERVICES.put(type, instance);
    }
}
