package org.cactus.auton.subject;

import java.io.Serializable;
import java.util.Map;

/**
 * 行为树执行主体。
 * <p>持有唯一标识、业务状态和附加属性。由 {@link ActorRepository} 管理持久化。</p>
 *
 * @param <S> 业务状态类型
 */
public interface Actor<S> extends Serializable {

    /** Actor 唯一标识 */
    String id();

    /** 业务状态 */
    S state();

    /** 附加属性 */
    Map<String, Object> attributes();
}
