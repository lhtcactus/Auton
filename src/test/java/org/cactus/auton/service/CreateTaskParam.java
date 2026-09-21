package org.cactus.auton.service;

public class CreateTaskParam {
    private String missionId;
    private String deviceId;

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "CreateTaskParam{" +
                "missionId='" + missionId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
