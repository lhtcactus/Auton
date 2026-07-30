package org.cactus.auton.node;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;

/**
 * 判断是否急停。
 */

public class NoEmergencyCondition extends Node<VehicleState> {
    /**
     * 创建条件节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public NoEmergencyCondition(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status execute(TickContext<VehicleState> context) {
        String emergencyState =  context.actor().state().getEmergency();
        if (State.APPLY.equalsValue(emergencyState)||State.RUNNING.equalsValue(emergencyState)||State.FINISH.equalsValue(emergencyState)){
            return Status.BuiltIn.ABORT;
        }
        return Status.BuiltIn.SUCCESS;
    }
}
