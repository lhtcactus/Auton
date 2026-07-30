package org.cactus.auton.node;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

public class EmergencyAction extends AbstractExecByNoMissionAction {
    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public EmergencyAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected String getState(TickContext<VehicleState> context) {
        return context.actor().state().getEmergency();
    }

    @Override
    protected Actor<VehicleState> execService(TickContext<VehicleState> context) {
        ServiceRegistry.<VehicleService>get(VehicleService.class).emergency(context.actor().id());

        Actor<VehicleState> actor = context.actor();
        actor.state().setEmergency(State.APPLY.getValue());
        return actor;
    }
}
