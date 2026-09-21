package org.cactus.auton.node;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

/**
 * 急停恢复
 */
public class NoEmergencyAction extends AbstractExecByNoMissionAction {
    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public NoEmergencyAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected String getState(TickContext<VehicleState> context) {
        return context.actor().state().getRecover();
    }

    @Override
    protected Actor<VehicleState> execService(TickContext<VehicleState> context) {
        ServiceRegistry.<VehicleService>get(VehicleService.class).noEmergency(context.actor().id());

        Actor<VehicleState> actor = context.actor();
        actor.state().setRecover(State.RUNNING.getValue());
        return actor;
    }
}
