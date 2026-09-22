package org.cactus.auton.service;

public class VehicleService {


    /**
     * 创建任务
     */
    public void createMission(CreateTaskParam createTaskParam) {
        System.out.println("VehicleService.createMission: " + createTaskParam);

    }
    public void createNavi(CreateNaviParam createNaviParam){
        System.out.println("VehicleService.createNavi: " + createNaviParam);
    }
    public void cancelMission(CancelMissionParam cancelMissionParam) {
        System.out.println("VehicleService.cancelMission: " + cancelMissionParam);
    }

    public void emergency(String vehicleId) {
        System.out.println("VehicleService.emergency: " + vehicleId);
    }
    public void noEmergency(String vehicleId) {
        System.out.println("VehicleService.noEmergency: " + vehicleId);
    }


}
