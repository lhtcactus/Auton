package org.cactus.auton.node.decorator;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Status;

/**
 * 重复执行直到成功装饰节点。
 * <p>不断执行子节点，直到子节点返回 SUCCESS 为止。
 * FAILURE 和 RUNNING 均导致本节点返回 RUNNING，
 * 等待下一次 tick 继续尝试。ABORT 直接传递。</p>
 */
public class UntilSuccess<S> extends Decorator<S> {

    /**
     * 创建重复执行直到成功装饰节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public UntilSuccess(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        Status status = child.tick(context);
        if (Status.BuiltIn.ABORT.sameStateAs(status)) {
            return status;
        }
        if (Status.BuiltIn.SUCCESS.sameStateAs(status)) {
            return status;
        }
        return Status.BuiltIn.RUNNING;
    }
}
