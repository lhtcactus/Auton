package org.cactus.auton.cmd;

import org.cactus.auton.blackboard.Blackboard;
import org.cactus.auton.command.AbstractCommand;
import org.cactus.auton.service.CancelMissionParam;
import org.cactus.auton.service.CreateNaviParam;
import org.cactus.auton.service.CreateTaskParam;
import org.cactus.auton.state.VehicleState;

public class CreateTaskAndNaviCmd extends AbstractCommand<CreateTaskAndNaviCmd.CreateMission, VehicleState> {
    @Override
    public String type() {
        return "CREATE_TASK_AND_Navi";
    }

    @Override
    public void fillParams(Blackboard blackboard) {
        CancelMissionParam cancelMissionParam = new CancelMissionParam();
        blackboard.put("cancelMissionParam", cancelMissionParam);

        CreateMission.Navi navi = param.getNavi();
        CreateNaviParam createNaviParam = new CreateNaviParam();
        createNaviParam.setMissionId(navi.getMissionId());
        createNaviParam.setDestCode(navi.getDestCode());
        blackboard.put("createNaviParam", createNaviParam);

        CreateMission.Task task = param.getTask();
        CreateTaskParam createTaskParam = new CreateTaskParam();
        createTaskParam.setMissionId(task.getMissionId());
        blackboard.put("createTaskParam", createTaskParam);
    }

    @Override
    public String toString() {
        return "CreateTaskAndNaviCmd{" +
                "id='" + id + '\'' +
                ", missionId='" + missionId + '\'' +
                ", type='" + type + '\'' +
                ", actorId='" + actorId + '\'' +
                ", param=" + param +
                '}';
    }

    public static class CreateMission {
        private Task task;
        private Navi navi;

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public Navi getNavi() {
            return navi;
        }

        public void setNavi(Navi navi) {
            this.navi = navi;
        }

        public static class Task{
            private String missionId;

            public String getMissionId() {
                return missionId;
            }

            public void setMissionId(String missionId) {
                this.missionId = missionId;
            }
        }

        public static class Navi{
            private String missionId;
            private String destCode;

            public String getMissionId() {
                return missionId;
            }

            public void setMissionId(String missionId) {
                this.missionId = missionId;
            }

            public String getDestCode() {
                return destCode;
            }

            public void setDestCode(String destCode) {
                this.destCode = destCode;
            }
        }
    }
}
