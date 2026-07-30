package org.cactus.auton.node;

import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.leaf.Action;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

public abstract class AbstractExecByMissionAction extends Action<VehicleState> {

    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public AbstractExecByMissionAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status doExecute(TickContext<VehicleState> context) {
        String state = getState(context);
        //有任务且是正常状态
        if (State.APPLY.equalsValue(state)
                ||State.RUNNING.equalsValue(state)) {

            //如果任务ID与上下文中任务id相同，则认为是同一任务
            if (context.command().missionId().equals( context.actor().state().getMissionId())) {
                //是本CMD则返回RUNNING
                return Status.BuiltIn.RUNNING;
            } else {
                //不是则返回FAILURE
                return Status.BuiltIn.FAILURE;
            }
        }else if(State.START.equalsValue(state)
                ||State.ERROR.equalsValue(state)){
            //创建任务并返回RUNNING
            Actor<VehicleState> actor = execService(context);
            actorRepository.save(actor);
            return Status.BuiltIn.RUNNING;
        }else if(State.FINISH.equalsValue(state)){
            //如果任务ID与上下文中任务id相同，则认为是同一任务
            if (context.command().missionId().equals( context.actor().state().getMissionId())) {
                //是本CMD则返回SUCCESS
                return Status.BuiltIn.SUCCESS;
            } else {
                //不是则返回FAILURE
                return Status.BuiltIn.FAILURE;
            }
        }
        return Status.BuiltIn.FAILURE;
    }


    protected  abstract String getState(TickContext<VehicleState> context);
    protected  abstract Actor<VehicleState> execService(TickContext<VehicleState> context);


}
