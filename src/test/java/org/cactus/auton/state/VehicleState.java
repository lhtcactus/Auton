package org.cactus.auton.state;

public class VehicleState {
    private String missionId;
    private String login;
    private String emergency;
    private String task;
    private String navigation;

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
                ", task='" + task + '\'' +
                ", navigation='" + navigation + '\'' +
                '}';
    }
}
