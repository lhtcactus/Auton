package org.cactus.auton.command;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用命令实现：所有命令共用这一个实现类，直接实现 {@link Command} 接口。
 */
public class GeneralCommand implements Command, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String type;
    private String actorId;
    private String missionId;
    /**
     * 业务参数：key 为节点读取时使用的黑板 key，value 为参数对象。
     * transient —— 参数对象不要求实现 Serializable，统一以 JSON 形式序列化。
     */
    private Map<String, Object> params = new HashMap<>();


    @Override
    public String id() {
        return id;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String actorId() {
        return actorId;
    }

    @Override
    public String missionId() {
        return missionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

}
