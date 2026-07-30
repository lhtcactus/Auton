package org.cactus.auton.subject;

import java.util.HashMap;
import java.util.Map;

/**
 * Actor 默认实现，基于属性字段的简单 POJO。
 *
 * @param <S> 业务状态类型
 */
public class DefaultActor<S> implements Actor<S> {

    private String id;
    private S state;
    private final Map<String, Object> attributes = new HashMap<>();


    /** @param id Actor 唯一标识 */
    public void setId(String id) { this.id = id; }

    /** @param state 业务状态 */
    public void setState(S state) { this.state = state; }


    @Override
    public String id() {
        return id;
    }

    @Override
    public S state() {
        return state;
    }

    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "DefaultActor{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", attributes=" + attributes +
                '}';
    }
}
