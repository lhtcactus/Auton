package org.cactus.auton.service;

import org.cactus.auton.ServiceRegistry;
import org.cactus.auton.engine.CommandBehaviorTreeEngine;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class VehicleService {
    private final CommandBehaviorTreeEngine<VehicleState> engine;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public VehicleService(CommandBehaviorTreeEngine<VehicleState> engine) {
        this.engine = engine;
    }

    /**
     * 创建任务
     */
    public void createMission(CreateTaskParam createTaskParam) {
        System.out.println("VehicleService.createMission: " + createTaskParam);
        executor.submit(()->{
            feedback("createMission",createTaskParam.getDeviceId(),createTaskParam.getMissionId(),createTaskParam);
        });

    }
    public void createNavi(CreateNaviParam createNaviParam){
        System.out.println("VehicleService.createNavi: " + createNaviParam);
        executor.submit(()->{
            feedback("createNavi",createNaviParam.getDeviceId(),createNaviParam.getMissionId(),createNaviParam);
        });
    }
    public void cancelMission(CancelMissionParam cancelMissionParam) {
        System.out.println("VehicleService.cancelMission: " + cancelMissionParam);
        executor.submit(()->{
            feedback("cancelMission",cancelMissionParam.getDeviceId(),cancelMissionParam.getMissionId(),cancelMissionParam);
        });
    }

    public void emergency(String vehicleId) {
        System.out.println("VehicleService.emergency: " + vehicleId);
        executor.submit(()->{
            feedback("emergency",vehicleId,null,null);
        });
    }
    public void noEmergency(String vehicleId) {
        System.out.println("VehicleService.noEmergency: " + vehicleId);
        executor.submit(()->{
            feedback("noEmergency",vehicleId,null,null);
        });
    }


    void feedback(String type,String actorId, String missionId,Object params){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        switch (type){
            case "createMission":
                processVehicleMessage(type,actorId,missionId,202);
                break;
            case "createNavi":
                //导航接受
                processVehicleMessage(type,actorId,missionId,202);
                //导航到达
                processVehicleMessage(type,actorId,missionId,200);
                break;
            case "cancelMission":
                //命令接受
                processVehicleMessage(type,actorId,missionId,202);
                //命令完成
                processVehicleMessage(type,actorId,missionId,200);
                break;
            case "emergency":
                //急停接受
                processVehicleMessage(type,actorId,missionId,202);
                //急停完成
                processVehicleMessage(type,actorId,missionId,200);
                break;
            case "noEmergency":
                //急停恢复接受
                processVehicleMessage(type,actorId,missionId,202);
                //急停恢复完成
                processVehicleMessage(type,actorId,missionId,200);
                break;
        }
    }


    void processVehicleMessage(String type,String actorId, String missionId,int code){

        switch (type){
            case "createMission":
                withActorLock(actorId, vehicleStateActor -> {
                    System.out.println("模拟接收车辆反馈任务,missionId:"+missionId+",actorId:"+actorId+",code="+code);
                    // 如果返回的missionId与当前missionId一致，则修改状态，否则忽略
                    if(vehicleStateActor.state().getMissionId().equals(missionId)){
                        if(code==202){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setTask(State.APPLY.getValue());
                                return actor;
                            });
                        }else if (code==200){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setTask(State.FINISH.getValue());
                                return actor;
                            });
                        }else{
                            engine.updateActor(actorId, actor -> {
                                actor.state().setTask(State.ERROR.getValue());
                                return actor;
                            });
                        }

                    }else {
                        System.out.println("忽略了missionId:"+missionId);
                    }
                });
                break;
            case "createNavi":
                withActorLock(actorId, vehicleStateActor -> {
                    System.out.println("模拟车辆反馈导航接收,missionId:"+missionId+",actorId:"+actorId+",code="+code);
                    // 如果返回的missionId与当前missionId一致，则修改状态，否则忽略
                    if(vehicleStateActor.state().getMissionId().equals(missionId)){
                        if(code==202){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setNavigation(State.APPLY.getValue());
                                return actor;
                            });
                        }else if (code==200){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setNavigation(State.FINISH.getValue());
                                return actor;
                            });
                        }else {
                            engine.updateActor(actorId, actor -> {
                                actor.state().setNavigation(State.ERROR.getValue());
                                return actor;
                            });
                        }
                    }else {
                        System.out.println("忽略了missionId:"+missionId);
                    }
                });
                break;
            case "cancelMission":
                withActorLock(actorId, vehicleStateActor -> {
                    System.out.println("模拟车辆取消任务完成,missionId:"+missionId+",actorId:"+actorId+",code="+code);
                    // 如果返回的missionId与当前missionId一致，则修改状态，否则忽略
                    if(vehicleStateActor.state().getMissionId().equals(missionId)){
                        if(code==202){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setCancelTask(State.APPLY.getValue());
                                return actor;
                            });

                        }else if (code==200){
                            engine.updateActor(actorId, actor -> {
                                actor.state().setCancelTask(State.FINISH.getValue());
                                actor.state().setTask(State.START.getValue());
                                actor.state().setNavigation(State.START.getValue());
                                actor.state().setMissionId(null);
                                return actor;
                            });

                        }else {
                            engine.updateActor(actorId, actor -> {
                                actor.state().setCancelTask(State.ERROR.getValue());
                                return actor;
                            });
                        }

                    }else {
                        System.out.println("忽略了missionId:"+missionId);
                    }
                });
                break;
            case "emergency":
                withActorLock(actorId, vehicleStateActor -> {
                    System.out.println("模拟车辆急停完成,missionId:"+missionId+",actorId:"+actorId+",code="+code);
                    if(code==202){
                        engine.updateActor(actorId, actor -> {
                            actor.state().setEmergency(State.APPLY.getValue());
                            return actor;
                        });
                    }else if (code==200){
                        engine.updateActor(actorId, actor -> {
                            actor.state().setEmergency(State.FINISH.getValue());
                            return actor;
                        });
                    }else {
                        engine.updateActor(actorId, actor -> {
                            actor.state().setEmergency(State.ERROR.getValue());
                            return actor;
                        });
                    }
                });
                break;
            case "noEmergency":
                withActorLock(actorId, vehicleStateActor -> {
                    System.out.println("模拟车辆急停恢复完成,missionId:"+missionId+",actorId:"+actorId+",code="+code);
                    if(code==202){
                        engine.updateActor(actorId, actor -> {
                            actor.state().setRecover(State.APPLY.getValue());
                            return actor;
                        });
                    }else if (code==200){
                        engine.updateActor(actorId, actor -> {
                            actor.state().setRecover(State.FINISH.getValue());
                            actor.state().setEmergency(State.START.getValue());
                            return actor;
                        });
                    }else {
                        engine.updateActor(actorId, actor -> {
                            actor.state().setRecover(State.ERROR.getValue());
                            return actor;
                        });
                    }
                });
                break;

        }

    }

    void withActorLock(String actorId, Consumer<Actor<VehicleState>> consumer){
        CommandBehaviorTreeEngine.Lock lock = engine.getLock(actorId);
        try {
            lock.lock();
            ActorRepository<VehicleState> actorRepository = ServiceRegistry.get(ActorRepository.class);
            Actor<VehicleState> actor = actorRepository.find(actorId);
            consumer.accept(actor);
        }finally {
            lock.unlock();
        }
    }


}
