package org.cactus.auton.command;

/**
 * Command 骨架实现，封装通用字段和 getter/setter，子类只需覆写 {@link Command#fillParams}。
 *
 * @param <T> 命令携带的业务参数类型
 * @param <S> Actor 业务状态类型
 */
public abstract class AbstractCommand<T, S> implements Command<S> {
    protected String id;
    protected String missionId;
    protected String type;
    protected String actorId;
    /** 业务参数 */
    protected T param;

    /** @param missionId 业务任务标识 */
    public void setMissionId(String missionId) { this.missionId = missionId; }

    @Override
    public String missionId() { return missionId; }

    /** @param actorId Actor 标识 */
    public void setActorId(String actorId) { this.actorId = actorId; }

    /** @param type 命令类型（对应行为树节点的注册 key） */
    public void setType(String type) { this.type = type; }

    /** @param id 命令唯一标识 */
    public void setId(String id) { this.id = id; }

    /** @param param 业务参数 */
    public void setParam(T param) { this.param = param; }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String type() {
        return this.type;
    }

    @Override
    public String actorId() {
        return this.actorId;
    }

}
