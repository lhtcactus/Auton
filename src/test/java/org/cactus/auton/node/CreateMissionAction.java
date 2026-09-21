package org.cactus.auton.node;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.node.leaf.Action;
import org.cactus.auton.service.CreateTaskParam;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;

import java.util.Objects;

public class CreateMissionAction extends Action<VehicleState> {
    /**
     * 创建动作节点。
     *
     * @param id   节点唯一标识
     * @param name 节点名称
     */
    public CreateMissionAction(String id, String name) {
        super(id, name);
    }

    @Override
    protected Status doExecute(TickContext<VehicleState> context) {
        String state =  context.actor().state().getTask();
        Object currentCmdId = context.actor().attributes().get("cmdId");
        boolean isCurrentCmd = Objects.equals(context.command().id(),currentCmdId);
        // 如果为空 则表示当前cmd
        if(Objects.isNull(currentCmdId)){
            isCurrentCmd = true;
        }

        // 如果取消任务是出于刚发送或者已接收，则终止本次tick
        // todo 如果车端掉线咋办，也不能创建新任务了。。。不能自己恢复啊
        if(State.RUNNING.equalsValue(context.actor().state().getCancelTask()) || State.APPLY.equalsValue(context.actor().state().getCancelTask())){
            return Status.BuiltIn.ABORT;
        }

        //有任务且是正常状态
        if (State.RUNNING.equalsValue(state)) {

            //如果任务ID与上下文中任务id相同，则认为是同一任务
            if (context.command().missionId().equals( context.actor().state().getMissionId())&&isCurrentCmd) {
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
        }else if(State.APPLY.equalsValue(state)||State.FINISH.equalsValue(state)){
            //如果任务ID与上下文中任务id相同，则认为是同一任务
            if (context.command().missionId().equals( context.actor().state().getMissionId())&&isCurrentCmd) {
                //是本CMD则返回SUCCESS
                return Status.BuiltIn.SUCCESS;
            } else {
                //不是则返回FAILURE
                return Status.BuiltIn.FAILURE;
            }
        }
        return Status.BuiltIn.FAILURE;
    }


    private Actor<VehicleState> execService(TickContext<VehicleState> context) {
        CreateTaskParam param = context.blackboard().get("createTaskParam");

        VehicleService vehicleService = ServiceRegistry.get(VehicleService.class);
        vehicleService.createMission(param);
        //设置
        Actor<VehicleState> actor = context.actor();
        actor.state().setTask(State.RUNNING.getValue());
        actor.state().setMissionId(context.command().missionId());
        actor.attributes().put("cmdId",context.command().id());
        return actor;
    }
}
