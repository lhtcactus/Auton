package org.cactus.auton.context;

import org.cactus.auton.blackboard.Blackboard;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.command.Command;

/**
 * Tick 执行上下文默认实现。
 *
 * @param <S> 业务状态类型
 */
public class DefaultContext<S> implements TickContext<S> {

    private final Command cmd;
    private final Actor<S> actor;
    private final Blackboard blackboard;
    private final TreeTracer<S> tracer;

    public DefaultContext(Command cmd,
                          Actor<S> actor,
                          Blackboard blackboard,
                          TreeTracer<S> tracer) {
        this.cmd = cmd;
        this.actor = actor;
        this.blackboard = blackboard;
        this.tracer = tracer;
    }

    @Override
    public Command command() { return this.cmd; }

    @Override
    public Actor<S> actor() { return this.actor; }

    @Override
    public Blackboard blackboard() { return this.blackboard; }

    @Override
    public TreeTracer<S> tracer() { return this.tracer; }
}
