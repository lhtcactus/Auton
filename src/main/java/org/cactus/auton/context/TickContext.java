package org.cactus.auton.context;

import org.cactus.auton.blackboard.Blackboard;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.command.Command;

/**
 * Tick 执行上下文接口。
 * 单次 tick 执行期间共享的上下文对象，包含当前命令、Actor 快照和黑板。
 *
 * @param <S> 业务状态类型
 */
public interface TickContext<S> {

    /**
     * 获取当前执行的命令。
     *
     * @return 当前命令
     */
    Command<S> command();

    /**
     * 获取 Actor 快照。
     *
     * @return 当前 Actor 快照
     */
    Actor<S> actor();

    /**
     * 获取黑板。
     *
     * @return 黑板实例
     */
    Blackboard blackboard();

    /**
     * 获取执行追踪器。
     *
     * @return 追踪器实例
     */
    TreeTracer<S> tracer();
}
