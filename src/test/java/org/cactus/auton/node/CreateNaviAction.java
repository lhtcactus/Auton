package org.cactus.auton.node;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.service.CreateNaviParam;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

public class CreateNaviAction extends AbstractExecByMissionAction {
    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public CreateNaviAction(String id, String name) {
        super(id, name);
    }


    @Override
    protected String getState(TickContext<VehicleState> context) {
        return context.actor().state().getNavigation();
    }

    @Override
    protected Actor<VehicleState> execService(TickContext<VehicleState> context) {
        CreateNaviParam param = context.blackboard().get("createNaviParam");

        VehicleService vehicleService = ServiceRegistry.get(VehicleService.class);
        vehicleService.createNavi(param);
        //设置
        Actor<VehicleState> actor = context.actor();
        actor.state().setNavigation(State.APPLY.getValue());
        return actor;
    }
}
