package org.cactus.auton.node.decorator;

import org.cactus.auton.node.Node;

/**
 * 装饰节点基类。
 * <p>持有单个子节点，对子节点的执行结果进行修饰或变换。
 * 子类实现不同的修饰策略（取反、重复、直到成功等）。</p>
 */
public abstract class Decorator<S> extends Node<S> {

    protected Node<S> child;

    public Decorator(String id, String name) {
        super(id, name);
    }

    /**
     * 设置子节点，支持链式调用。
     *
     * @param child 子节点
     * @return this
     */
    public Decorator<S> setChild(Node<S> child) {
        this.child = child;
        return this;
    }

    /** 获取子节点 */
    public Node<S> getChild() {
        return child;
    }
}
