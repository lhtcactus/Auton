package org.cactus.auton.node;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;

import org.cactus.auton.node.leaf.Action;
import org.cactus.auton.service.CancelMissionParam;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

public class CancelMissionAction extends Action<VehicleState> {

    public CancelMissionAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status doExecute(TickContext<VehicleState> context) {
        CancelMissionParam param = new CancelMissionParam();
        param.setDeviceId(context.actor().id());
        param.setMissionId(context.actor().state().getMissionId());
        VehicleService vehicleService = ServiceRegistry.get(VehicleService.class);
        vehicleService.cancelMission(param);

        Actor<VehicleState> actor = context.actor();
        actor.state().setCancelTask(State.RUNNING.getValue());
        actorRepository.save(actor);
        return Status.BuiltIn.RUNNING;
    }
}
