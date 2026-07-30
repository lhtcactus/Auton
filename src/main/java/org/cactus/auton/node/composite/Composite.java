package org.cactus.auton.node.composite;

import org.cactus.auton.node.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 复合节点基类。
 * <p>持有多个子节点，按特定策略控制子节点的执行流程。
 * 子类实现不同的执行策略（顺序、选择、并行等）。</p>
 *
 * @param <S> 业务状态类型
 */
public abstract class Composite<S> extends Node<S> {

    protected final List<Node<S>> children = new ArrayList<>();

    /**
     * 创建复合节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Composite(String id, String name) {
        super(id, name);
    }

    /**
     * 添加子节点，支持链式调用。
     *
     * @param child 子节点
     * @return this
     */
    public Composite<S> addChild(Node<S> child) {
        children.add(child);
        return this;
    }

    /**
     * 获取所有子节点。
     *
     * @return 子节点列表
     */
    public List<Node<S>> getChildren() {
        return children;
    }
}
