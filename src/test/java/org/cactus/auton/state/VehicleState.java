package org.cactus.auton.state;

public class VehicleState {
    private String missionId;
    private String login;
    private String emergency;
    //急停恢复
    private String recover;
    private String task;
    private String navigation;
    private String cancelTask;

    public String getRecover() {
        return recover;
    }

    public void setRecover(String recover) {
        this.recover = recover;
    }

    public String getCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(String cancelTask) {
        this.cancelTask = cancelTask;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getNavigation() {
        return navigation;
    }

    public void setNavigation(String navigation) {
        this.navigation = navigation;
    }

    @Override
    public String toString() {
        return "VehicleState{" +
                "missionId='" + missionId + '\'' +
                ", login='" + login + '\'' +
                ", emergency='" + emergency + '\'' +
                ", recover='" + recover + '\'' +
                ", task='" + task + '\'' +
                ", navigation='" + navigation + '\'' +
                ", cancelTask='" + cancelTask + '\'' +
                '}';
    }
}
