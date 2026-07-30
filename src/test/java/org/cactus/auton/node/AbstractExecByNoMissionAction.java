package org.cactus.auton.node;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.leaf.Action;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

public abstract class AbstractExecByNoMissionAction extends Action<VehicleState> {

    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public AbstractExecByNoMissionAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status doExecute(TickContext<VehicleState> context) {
        String state = getState(context);
        //有任务且是正常状态
        if (State.APPLY.equalsValue(state)
                ||State.RUNNING.equalsValue(state)) {

            //无mission操作
            return Status.BuiltIn.RUNNING;
        }else if(State.START.equalsValue(state)
                ||State.ERROR.equalsValue(state)){
            //创建任务并返回RUNNING
            Actor<VehicleState> actor = execService(context);
            actorRepository.save(actor);
            return Status.BuiltIn.RUNNING;
        }else if(State.FINISH.equalsValue(state)){
            //无mission操作
            return Status.BuiltIn.SUCCESS;
        }
        return Status.BuiltIn.FAILURE;
    }


    protected  abstract String getState(TickContext<VehicleState> context);
    protected  abstract Actor<VehicleState> execService(TickContext<VehicleState> context);


}
