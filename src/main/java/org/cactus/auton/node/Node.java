package org.cactus.auton.node;

import org.cactus.auton.context.TickContext;

import java.util.Objects;
import java.util.UUID;

/**
 * 行为树节点基类。
 * <p>所有节点必须指定 id 和 name。id 用于唯一标识节点（如黑板状态共享的 key），
 * name 用于可读性描述和后续 DSL 映射。</p>
 *
 * @param <S> 业务状态类型
 */
public abstract class Node<S> {

    private final String id;
    private final String name;

    /**
     * 创建节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Node(String id, String name) {
        if(Objects.isNull(id)){
            id = UUID.randomUUID().toString();
        }
        this.id = id;
        this.name = name;
    }

    /**
     * 执行当前节点逻辑。
     *
     * @param context 执行上下文
     * @return 执行状态
     */
    public final Status tick(TickContext<S> context) {
        beforeTick(context);
        Status status = execute(context);
        afterTick(context, status);
        return status;
    }
    /**
     * 执行节点逻辑前的钩子，用于子类实现。
     * @param context 执行上下文
     */
    protected void beforeTick(TickContext<S> context) {
        context.tracer().onNodeEnter(this, context);
    }
    /**
     * 执行节点逻辑后的钩子，用于子类实现。
     * @param context 执行上下文
     * @param status 执行状态
     */
    protected void afterTick(TickContext<S> context, Status status) {
        context.tracer().onNodeExit(this, context, status);
    }
    /**
     * 执行节点逻辑，用于子类实现。
     * @param context 执行上下文
     */
    protected abstract Status execute(TickContext<S> context);

    /**
     * 获取节点唯一标识。
     *
     * @return 节点 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取节点名称。
     *
     * @return 节点名称
     */
    public String getName() {
        return name;
    }
}
