package org.cactus.auton.subject;

/**
 * Actor 仓库接口。
 * <p>管理 Actor 的持久化操作，提供查找、保存和删除能力。
 * 用户可根据实际场景实现（如数据库、缓存、RPC 等）。</p>
 *
 * @param <S> 业务状态类型，与 {@link Actor} 的泛型一致
 */
public interface ActorRepository<S> {

    /**
     * 根据 ID 查找 Actor。
     *
     * @param id Actor 标识
     * @return Actor 实例，不存在时返回 null
     */
    Actor<S> find(String id);

    /**
     * 保存 Actor。首次保存时分配初始版本号，后续保存递增版本号。
     *
     * @param actor Actor 实例
     */
    void save(Actor<S> actor);

    /**
     * 删除 Actor。
     *
     * @param id Actor 标识
     */
    void delete(String id);
}
