package org.cactus.auton.node.composite;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;

/**
 * 顺序节点。
 * <p>按子节点排列顺序依次执行，全部返回 SUCCESS 才算 SUCCESS。
 * 无状态设计：每次 tick 从第一个子节点开始重新评估，
 * 已完成的子节点应快速返回 SUCCESS（幂等设计）。</p>
 */
public class Sequence<S> extends Composite<S> {

    /**
     * 创建顺序节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Sequence(String id, String name) {
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
            if (Status.BuiltIn.FAILURE.sameStateAs(status)) {
                return status;
            }
        }
        return Status.BuiltIn.SUCCESS;
    }
}
