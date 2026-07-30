package org.cactus.auton.node.decorator;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Status;

/**
 * 取反装饰节点。
 * <p>将子节点的 SUCCESS 和 FAILURE 结果互换。
 * RUNNING 和 ABORT 保持原样传递。</p>
 */
public class Inverter<S> extends Decorator<S> {

    /**
     * 创建取反装饰节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Inverter(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        Status status = child.tick(context);
        if (Status.BuiltIn.ABORT.sameStateAs(status)) {
            return status;
        }
        if (Status.BuiltIn.SUCCESS.sameStateAs(status)) {
            return Status.BuiltIn.FAILURE;
        }
        if (Status.BuiltIn.FAILURE.sameStateAs(status)) {
            return Status.BuiltIn.SUCCESS;
        }
        return status;
    }
}
