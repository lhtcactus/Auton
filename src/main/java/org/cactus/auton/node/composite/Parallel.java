package org.cactus.auton.node.composite;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Status;
import org.cactus.auton.node.Node;

/**
 * 并行节点。
 * <p>同时执行所有子节点，根据模式判定最终结果：
 * <ul>
 *   <li>AND（默认）：全部 SUCCESS 才算 SUCCESS，任一 FAILURE 即 FAILURE</li>
 *   <li>OR：任一 SUCCESS 即 SUCCESS，全部 FAILURE 才 FAILURE</li>
 * </ul>
 * 无状态设计：每次 tick 重新执行所有子节点并统计结果。</p>
 */
public class Parallel<S> extends Composite<S> {

    private final ParallelMode mode;

    /**
     * 创建并行节点，默认 AND 模式。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public Parallel(String id, String name) {
        this(id, name, ParallelMode.AND.name());
    }

    /**
     * 创建并行节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     * @param mode 执行模式，"AND" 或 "OR"
     */
    public Parallel(String id, String name, String mode) {
        super(id, name);
        this.mode = ParallelMode.valueOf(mode);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        int successCount = 0;
        int failureCount = 0;

        for (Node<S> child : children) {
            Status status = child.tick(context);
            if (Status.BuiltIn.ABORT.sameStateAs(status)) {
                return status;
            }
            if (Status.BuiltIn.SUCCESS.sameStateAs(status)) {
                successCount++;
            }
            if (Status.BuiltIn.FAILURE.sameStateAs(status)) {
                failureCount++;
            }
        }

        if (mode == ParallelMode.OR) {
            if (successCount > 0) {
                return Status.BuiltIn.SUCCESS;
            }
            if (failureCount == children.size()) {
                return Status.BuiltIn.FAILURE;
            }
            return Status.BuiltIn.RUNNING;
        } else {
            if (failureCount > 0) {
                return Status.BuiltIn.FAILURE;
            }
            if (successCount == children.size()) {
                return Status.BuiltIn.SUCCESS;
            }
            return Status.BuiltIn.RUNNING;
        }
    }
    /** AND: 全成功才成功；OR: 任一成功即成功 */
    private enum ParallelMode {
        AND,
        OR
    }

}
