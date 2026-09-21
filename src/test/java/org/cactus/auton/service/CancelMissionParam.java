package org.cactus.auton.service;

public class CancelMissionParam {
    private String deviceId;
    private String missionId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    @Override
    public String toString() {
        return "CancelMissionParam{" +
                "deviceId='" + deviceId + '\'' +
                ", missionId='" + missionId + '\'' +
                '}';
    }
}
