package org.cactus.auton.command;

import java.util.List;

/**
 * 命令管理器，用于管理命令
 */
public interface CommandManger {
    /**
     * 提交命令
     * @param  operation 操作符，有实现者自定义。例如对命令进行追加、覆盖、安全命令等
     * @param  command 命令
     */
    void submit(String operation,Command command);
    /**
     * 获取Actor的当前/下一个命令
     * @param  actorId Actor 标识
     */
    Command next(String actorId);
    /**
     * 获取Actor所有存活命令
     * @param  actorId Actor 标识
     */
    List<Command> findCommands(String actorId);
    /**
     * 按命令 ID 移除命令。
     * @param  actorId Actor 标识
     * @param  commandId 命令标识
     */
    void removeById(String actorId,String commandId);
    /**
     * 按主体 ID 移除命令。
     *
     * @param actorId Actor 标识
     */
    void clearActorCommands(String actorId);
    /**
     * 清空所有命令
     */
    void clear();
}
