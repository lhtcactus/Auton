package org.cactus.auton.command;

import org.cactus.auton.blackboard.Blackboard;

/**
 * 命令接口，对行为树下达的指令任务。
 */
public interface Command {

    /** 命令唯一标识 */
    String id();

    /** 业务任务标识 */
    String missionId();

    /** 命令类型 */
    String type();

    /** Actor 标识 */
    String actorId();

    /**
     * 将命令参数填充到黑板，供行为树节点读取。
     * 引擎在 buildContext 时调用，节点无需感知 Command 类型。
     */
    default void fillParams(Blackboard blackboard){};
}
