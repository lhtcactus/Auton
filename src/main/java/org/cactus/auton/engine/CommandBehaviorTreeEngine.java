package org.cactus.auton.engine;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.blackboard.DefaultBlackboard;
import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;
import org.cactus.auton.context.DefaultContext;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.context.TreeTracer;
import org.cactus.auton.exception.BehaviorException;
import org.cactus.auton.exception.NotFoundException;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 命令驱动行为树引擎。
 * <p>维护命令队列与 Actor 状态，按键（command.type → Node）路由执行，
 * 支持分布式锁和异步续 tick，用户只需实现 {@link #afterTick} 和 {@link #getLock}。</p>
 *
 * @param <S> Actor 业务状态类型
 */
public abstract class CommandBehaviorTreeEngine<S> {

    protected final CommandManger commandManger;
    protected final ActorRepository<S> actorRepository;
    protected final Map<String, Node<S>> nodes;
    protected final AsyncExecutor asyncExecutor;
    protected TreeTracer<S> treeTracer;

    /**
     * @param nodeTypeMap 命令 type → 行为树根节点映射
     * @param actorRepository Actor 仓库
     * @param commandManger 命令管理器
     */
    public CommandBehaviorTreeEngine(Map<String, Node<S>> nodeTypeMap,
                                        ActorRepository<S> actorRepository,
                                     CommandManger commandManger) {
        this.nodes = new ConcurrentHashMap<>(nodeTypeMap);
        this.actorRepository = actorRepository;
        this.commandManger = commandManger;
        this.asyncExecutor = getAsyncExecutor();
        this.treeTracer = getTreeTracer();
        ServiceRegistry.register(CommandBehaviorTreeEngine.class, this);
        ServiceRegistry.register(CommandManger.class, commandManger);
        ServiceRegistry.register(ActorRepository.class, actorRepository);
    }

    // ========== 用户调用入口 ==========

    /**
     * 提交命令,自动触发 tick
     * @param  operation 操作符，有实现者自定义。例如对命令进行追加、覆盖、安全命令等
     * @param  command 命令
     */
    public void submit(String operation,Command command)throws BehaviorException{
        Lock lock = getLock(command.actorId());
        try {
            lock.lock();
            commandManger.submit(operation,command);
            tick(command.actorId());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新 Actor 并保存，自动触发 tick。
     * <p>引擎负责 find → 回调更新 → save 全流程，
     * 外部无需接触 ActorRepository，后续加锁也在此统一控制。</p>
     */
    public void updateActor(String actorId, Function<Actor<S>, Actor<S>> updater) throws BehaviorException{
        Lock lock = getLock(actorId);
        try {
            lock.lock();
            Actor<S> actor = actorRepository.find(actorId);
            if (actor == null){
                actor = createDefaultActor(actorId);
            }
            actor = updater.apply(actor);
            actorRepository.save(actor);
            tick(actorId);
        }finally {
            lock.unlock();
        }
    }

    // ========== 用户实现 ==========

    /**
     * tick 执行完毕后的业务处理。
     * @return true — 命令完成，出队并继续下一个；false — 停止等待下次触发
     */
    protected abstract boolean afterTick(Status status, TickContext<S> context);

    /**
     * 构建并返回 tick 的执行上下文
     * @param  command 命令
     */
    protected TickContext<S> buildContext(Command command) {
        Actor<S> actor = actorRepository.find(command.actorId());
        if (actor == null){
            actor = createDefaultActor(command.actorId());
        }
        DefaultBlackboard blackboard = new DefaultBlackboard();
        command.fillParams(blackboard);
        return new DefaultContext<>(command, actor, blackboard, treeTracer);
    }

    /**
     * 创建默认的 Actor 对象
     * @param  actorId Actor ID
     */
    protected abstract Actor<S> createDefaultActor(String actorId);

    // ========== 引擎内部 ==========



    /**
     * 执行单次 tick：取队首命令 → 构建上下文 → 路由到对应 Node 执行。
     * 执行完毕后，若 afterTick 返回 true 则异步续 tick，释放当前线程的锁。
     */
    protected void tick(String actorId) {
        Command cmd =  commandManger.next(actorId);
        if (cmd == null) {
            return;
        }
        TickContext<S> context = buildContext(cmd);
        Node<S> node = nodes.get(cmd.type());
        if (node == null){
            throw new NotFoundException("Node not found for command: " + cmd.type());
        }
        Status status = node.tick(context);
        if (!afterTick(status, context)) {
            return;
        }
        commandManger.removeById(actorId,cmd.id());
        // 继续下一次 tick，避免阻塞当前 tick
        asyncExecutor.submit(() -> {
            Lock lock = getLock(actorId);
            try{
                lock.lock();
                tick(actorId);
            }finally {
                lock.unlock();
            }
        });
    }

    /** 设置行为树追踪器 */
    public void setTreeTracer(TreeTracer<S> treeTracer) { this.treeTracer = treeTracer; }

    /** 获取追踪器，默认空实现，子类或用户调用 setTreeTracer 替换 */
    protected TreeTracer<S> getTreeTracer() { return new TreeTracer<S>() {}; }

    /** 根据 actorId 获取对应的锁，子类实现分布式锁接入 */
    public abstract Lock getLock(String actorId);

    /** 异步执行器，默认使用固定线程池，子类可按需替换 */
    protected AsyncExecutor getAsyncExecutor() {
        // 默认实现为线程池
        return new AsyncExecutor() {
            final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            @Override
            public void submit(Runnable task) {
                executor.submit(task);
            }
        };
    }

    /** 分布式锁抽象 */
    public interface Lock {
        void lock() throws BehaviorException;
        void unlock();
    }

    /** 异步任务执行器 */
    public interface AsyncExecutor {
        void submit(Runnable task);
    }
}
