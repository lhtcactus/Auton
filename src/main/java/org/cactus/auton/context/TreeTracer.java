package org.cactus.auton.context;

import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;

/**
 * 行为树执行追踪器，用于可观测性（日志、监控、调试）。
 * <p>所有方法均为 default 空实现，用户按需覆写。</p>
 */
public interface TreeTracer<S> {

    /** 进入节点 */
    default void onNodeEnter(Node<S> node, TickContext<S> ctx) {}

    /** 离开节点 */
    default void onNodeExit(Node<S> node, TickContext<S> ctx, Status status) {}
}
