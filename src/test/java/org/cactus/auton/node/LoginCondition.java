package org.cactus.auton.node;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.leaf.Condition;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;

/**
 * 判断是否登录。
 */

public class LoginCondition extends Condition<VehicleState> {
    /**
     * 创建条件节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public LoginCondition(String id, String name) {
        super(id, name);
    }

    @Override
    protected boolean doCheck(TickContext<VehicleState> context) {
        String loginState =  context.actor().state().getLogin();
        return State.FINISH.equalsValue(loginState);
    }
}
