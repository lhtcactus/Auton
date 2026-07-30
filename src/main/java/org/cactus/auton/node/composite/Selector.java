package org.cactus.auton.node.composite;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;

/**
 * 选择节点。
 * <p>按子节点排列顺序依次执行，任意一个返回 SUCCESS 即算 SUCCESS。
 * 无状态设计：每次 tick 从第一个子节点开始重新评估，
 * 优先级高的子节点（靠前）会优先被评估。</p>
 */
public class Selector<S> extends Composite<S> {

    /**
     * 创建选择节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Selector(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        for (Node<S> child : children) {
            Status status = child.tick(context);
            if (Status.BuiltIn.RUNNING.sameStateAs(status)) {
                return status;
            }
            if (Status.BuiltIn.ABORT.sameStateAs(status)) {
                return status;
            }
            if (Status.BuiltIn.SUCCESS.sameStateAs(status)) {
                return status;
            }
        }
        return Status.BuiltIn.FAILURE;
    }
}
