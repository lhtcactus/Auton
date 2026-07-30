package org.cactus.auton.node.leaf;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;
import org.cactus.auton.subject.ActorRepository;

/**
 * 动作节点。
 * <p>叶子节点，执行具体业务动作。子类覆写 {@link #doExecute} 实现业务逻辑，
 * 返回 SUCCESS / FAILURE / RUNNING。</p>
 */
public abstract class Action<S> extends Node<S> {

    protected ActorRepository<S> actorRepository;

    public Action(String id, String name) {
        super(id, name);
        actorRepository = ServiceRegistry.get(ActorRepository.class);
    }

    @Override
    protected Status execute(TickContext<S> context) {
        if(actorRepository==null){
            actorRepository = ServiceRegistry.get(ActorRepository.class);
        }
        return doExecute(context);
    }

    /**
     * 执行具体动作。
     * @param context 执行上下文
     * @return SUCCESS / FAILURE / RUNNING
     */
    protected abstract Status doExecute(TickContext<S> context);
}
