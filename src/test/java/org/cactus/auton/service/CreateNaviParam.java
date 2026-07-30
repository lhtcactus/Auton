package org.cactus.auton.service;

public class CreateNaviParam {
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

    @Override
    public String toString() {
        return "CreateNaviParam{" +
                "missionId='" + missionId + '\'' +
                ", destCode='" + destCode + '\'' +
                '}';
    }
}
