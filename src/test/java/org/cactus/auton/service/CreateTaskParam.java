package org.cactus.auton.service;

public class CreateTaskParam {
    private String missionId;

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    @Override
    public String toString() {
        return "CreateTaskParam{" +
                "missionId='" + missionId + '\'' +
                '}';
    }
}
