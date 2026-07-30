package org.cactus.auton.node.leaf;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Status;
import org.cactus.auton.node.Node;

/**
 * 条件节点。
 * <p>叶子节点，执行纯条件判断。子类覆写 {@link #doCheck}，
 * 返回 SUCCESS 或 FAILURE，不会返回 RUNNING。</p>
 */
public abstract class Condition<S> extends Node<S> {

    public Condition(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        return doCheck(context) ? Status.BuiltIn.SUCCESS : Status.BuiltIn.FAILURE;
    }

    /**
     * 条件判断。
     * @param context 执行上下文
     * @return true 条件满足
     */
    protected abstract boolean doCheck(TickContext<S> context);
}
