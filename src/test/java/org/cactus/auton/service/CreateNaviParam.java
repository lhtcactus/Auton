package org.cactus.auton.service;

public class CreateNaviParam {
    private String deviceId;
    private String missionId;
    private String destCode;

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

    public String getDestCode() {
        return destCode;
    }

    public void setDestCode(String destCode) {
        this.destCode = destCode;
    }

    @Override
    public String toString() {
        return "CreateNaviParam{" +
                "deviceId='" + deviceId + '\'' +
                ", missionId='" + missionId + '\'' +
                ", destCode='" + destCode + '\'' +
                '}';
    }
}
