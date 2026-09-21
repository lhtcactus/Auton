package org.cactus.auton;

import org.cactus.auton.builder.TreeBuilder;
import org.cactus.auton.cmd.CreateTaskAndNaviCmd;
import org.cactus.auton.cmd.EmergencyCmd;
import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;
import org.cactus.auton.config.MapActorRepository;
import org.cactus.auton.config.MapCommandManger;
import org.cactus.auton.config.VehicleBehaviorEngine;
import org.cactus.auton.engine.CommandBehaviorTreeEngine;
import org.cactus.auton.node.EmergencyAction;
import org.cactus.auton.node.Node;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class TreeTest {
    private CommandBehaviorTreeEngine<VehicleState> engine;

    private CommandManger<VehicleState> commandManger;
    private ActorRepository<VehicleState> actorRepository;
    @BeforeEach
    void setUpEngine(){
        // 定义行为树
        String tree =
                "id: root\n" +
                "name: 创建并导航树根节点\n" +
                "node: sequence\n" +
                "factory: sequence\n" +
                "children:\n" +
                "  - id: c1\n" +
                "    name: 已登录检查\n" +
                "    node: loginCondition\n" +
                "  - id: c2\n" +
                "    name: 未急停检查\n" +
                "    node: noEmergencyCondition\n" +
                "  - id: c3\n" +
                "    name: 任务流程\n" +
                "    node: selector\n" +
                "    children:\n" +
                "      - id: c3.1\n" +
                "        name: 创建任务\n" +
                "        node: createMissionAction\n" +
                "      - id: c3.2\n" +
                "        name: 取消任务\n" +
                "        node: cancelMissionAction\n" +
                "  - id: c4\n" +
                "    name: 创建导航\n" +
                "    node: createNaviAction\n";
        TreeBuilder<VehicleState> treeBuilder = new TreeBuilder<>();
        Node<VehicleState> createTaskAndNaviCmd = treeBuilder
                .loader(loader -> loader.load(tree))
                .nodeRegistry(registry -> registry.scanPackages("org.cactus.auton.node"))
                .build();


        Map<String,Node<VehicleState>> treeMap = new HashMap<>();
        treeMap.put("CREATE_TASK_AND_Navi", createTaskAndNaviCmd);

        treeMap.put("EMERGENCY_CMD", new EmergencyAction("EMERGENCY_CMD", "急停命令"));
        commandManger =  new MapCommandManger();
        actorRepository = new MapActorRepository();
        // 创建行为树引擎
        engine = new VehicleBehaviorEngine(treeMap, actorRepository, commandManger);

        // 注入Service
        ServiceRegistry.register(VehicleService.class, new VehicleService(engine));

    }



    @Test
    void test() throws InterruptedException {
        // 模拟车辆登录
        engine.updateActor("A501", actor -> {
            actor.state().setLogin(State.FINISH.getValue());
            return actor;
        });
//        // 打印当前状态
//        getCurrentState();
        // 创建任务并导航
        CreateTaskAndNaviCmd cmd = new CreateTaskAndNaviCmd();
        cmd.setId("123");
        cmd.setActorId("A501");
        cmd.setMissionId("123456789");
        CreateTaskAndNaviCmd.CreateMission param = new CreateTaskAndNaviCmd.CreateMission();
        param.setDeviceId("A501");
        param.setNavi(new CreateTaskAndNaviCmd.CreateMission.Navi());
        param.getNavi().setMissionId("123456789");
        param.getNavi().setDestCode("D001");
        param.setTask(new CreateTaskAndNaviCmd.CreateMission.Task());
        param.getTask().setMissionId("123456789");
        cmd.setParam(param);
        System.out.println("添加命令");
        engine.submit("APPEND",cmd);

        // 一直等待
        new CountDownLatch(1).await();
//
//        // 打印当前状态
//        getCurrentState();
//        // 创建任务并导航
//        CreateTaskAndNaviCmd cmd1 = new CreateTaskAndNaviCmd();
//        cmd1.setId("1234");
//        cmd1.setActorId("A501");
//        cmd1.setMissionId("9877654321");
//        CreateTaskAndNaviCmd.CreateMission param1 = new CreateTaskAndNaviCmd.CreateMission();
//        param1.setNavi(new CreateTaskAndNaviCmd.CreateMission.Navi());
//        param1.getNavi().setMissionId("9877654321");
//        param1.getNavi().setDestCode("D001");
//        param1.setTask(new CreateTaskAndNaviCmd.CreateMission.Task());
//        param1.getTask().setMissionId("9877654321");
//        cmd1.setParam(param);
//        System.out.println("再次添加命令，这是任务更新");
//        engine.submit("REPLACE",cmd1);

//        // 打印当前状态
//        getCurrentState();
//        System.out.println("模拟车辆反馈导航接收");
//        engine.updateActor("A501", actor -> {
//            actor.state().setNavigation(State.APPLY.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();

//        System.out.println("重复模拟车辆反馈导航接收");
//        engine.updateActor("A501", actor -> {
//            actor.state().setNavigation(State.APPLY.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟插入急停命令");
//        EmergencyCmd emergencyCmd = new EmergencyCmd();
//        emergencyCmd.setId("111");
//        emergencyCmd.setActorId("A501");
//        //急停命令不需要missionId
////        emergencyCmd.setMissionId("987654321");
//        engine.submit("SAFETY",emergencyCmd);
//
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟车辆反馈急停反馈-接收");
//        engine.updateActor("A501", actor -> {
//            actor.state().setEmergency(State.APPLY.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟车辆反馈急停反馈-完成");
//        engine.updateActor("A501", actor -> {
//            actor.state().setEmergency(State.FINISH.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟车辆反馈急停恢复反馈-完成");
//        // 注意：没有模拟下发急停恢复指令，直接模拟的急停恢复完成
//        engine.updateActor("A501", actor -> {
//            actor.state().setRecover(State.FINISH.getValue());
//            actor.state().setEmergency(State.START.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟车辆反馈导航完成");
//        engine.updateActor("A501", actor -> {
//            actor.state().setNavigation(State.FINISH.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        // 模拟车辆反馈取消任务接收，因为前面给了两个指令
//        System.out.println("模拟车辆取消任务接收");
//        engine.updateActor("A501", actor -> {
//            actor.state().setCancelTask(State.APPLY.getValue());
//            return actor;
//        });
//        // 打印当前状态
//        getCurrentState();
//
//        System.out.println("模拟车辆取消任务完成");
//        engine.updateActor("A501", actor -> {
//            actor.state().setCancelTask(State.FINISH.getValue());
//            actor.state().setTask(State.START.getValue());
//            actor.state().setNavigation(State.START.getValue());
//            actor.state().setMissionId(null);
//            return actor;
//        });
//
//        // 打印当前状态
//        getCurrentState();
//
//
//        // 模拟车辆反馈任务接收，实际上车辆会返回的202接收任务，接收任务后不会立刻完成，所以对于CreateMissionAction节点

//        // 打印当前状态
//        getCurrentState();

    }

    void getCurrentState(){
        Actor<VehicleState> actor = actorRepository.find("A501");

        List<Command<VehicleState>> commands = commandManger.findCommands("A501");
        System.out.println("当前状态：");
        System.out.println("    actor:"+actor);
        System.out.println("    commands: " + commands);
        System.out.println("                  ===========================                                   ");
        System.out.println("                  ===========================                                   ");
        System.out.println(" ");
    }




}
