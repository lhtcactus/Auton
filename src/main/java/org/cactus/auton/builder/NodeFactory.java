package org.cactus.auton.builder;

import org.cactus.auton.node.Node;

/**
 * 节点工厂。
 * <p>函数式接口，根据 {@link TreeNodeConfig} 创建节点实例。
 * 内置类型（Sequence/Selector/Parallel/Inverter/UntilSuccess）由框架注册，
 * 用户自定义类型通过 {@link TreeBuilder.NodeFactoryRegistry#register} 注册。</p>
 *
 * @param <S> 业务状态类型
 */
@FunctionalInterface
public interface NodeFactory<S> {

    /**
     * 创建节点。
     *
     * @param config 节点配置，包含 id、name、props、children 等信息
     * @return 节点实例
     */
    Node<S> create(TreeNodeConfig config);
}
