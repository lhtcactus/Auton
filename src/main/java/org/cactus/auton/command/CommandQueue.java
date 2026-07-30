package org.cactus.auton.command;

/**
 * 命令队列接口。
 * <p>管理待执行的命令队列，支持追加、出栈、插队、删除等操作。
 * 用户可自行实现（如基于数据库、Redis 等持久化方案）。</p>
 */
public interface CommandQueue<S> {

    /**
     * 追加命令到队尾。
     *
     * @param command 命令
     */
    void enqueue(Command<S> command);

    /**
     * 插入命令到队首。
     *
     * @param command 命令
     */
    void insertFirst(Command<S> command);

    /**
     * 查看队首命令但不移除。
     *
     * @param actorId Actor 标识
     * @return 队首命令，队列为空时返回 null
     */
    Command<S> peek(String actorId);

    /**
     * 按主体 ID 移除命令。
     *
     * @param actorId Actor 标识
     */
    void clearActorCommands(String actorId);

    /**
     * 按命令 ID 移除命令。
     *
     * @param actorId Actor 标识
     * @param commandId 命令标识
     */
    void removeByCmdId(String actorId,String commandId);

    /**
     * 清空队列。
     */
    void clear();
}
