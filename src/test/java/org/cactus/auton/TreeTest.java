package org.cactus.auton;

import org.cactus.auton.builder.TreeBuilder;
import org.cactus.auton.cmd.CreateTaskAndNaviCmd;
import org.cactus.auton.cmd.EmergencyCmd;
import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandQueue;
import org.cactus.auton.config.MapActorRepository;
import org.cactus.auton.config.MapCommandQueue;
import org.cactus.auton.config.VehicleBehaviorEngine;
import org.cactus.auton.engine.CommandBehaviorTreeEngine;
import org.cactus.auton.node.EmergencyAction;
import org.cactus.auton.node.Node;
import org.cactus.auton.service.VehicleService;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TreeTest {
    private CommandBehaviorTreeEngine<VehicleState> engine;

    private CommandQueue<VehicleState> commandQueue;
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
                .nodeRegistry(registry -> registry.scanPackages("org.cactus.behavior.node"))
                .build();


        Map<String,Node<VehicleState>> treeMap = new HashMap<>();
        treeMap.put("CREATE_TASK_AND_Navi", createTaskAndNaviCmd);

        treeMap.put("EMERGENCY_CMD", new EmergencyAction("EMERGENCY_CMD", "急停命令"));
        commandQueue =  new MapCommandQueue();
        // 创建行为树引擎
        engine = new VehicleBehaviorEngine(treeMap, new MapActorRepository(), commandQueue);

        // 注入Service
        ServiceRegistry.register(VehicleService.class, new VehicleService());

    }



    @Test
    void test(){
        // 模拟车辆登录
        engine.updateActor("A501", actor -> {
            actor.state().setLogin(State.FINISH.getValue());
            return actor;
        });

        // 创建任务并导航
        CreateTaskAndNaviCmd cmd = new CreateTaskAndNaviCmd();
        cmd.setId("123");
        cmd.setActorId("A501");
        cmd.setMissionId("123456789");
        CreateTaskAndNaviCmd.CreateMission param = new CreateTaskAndNaviCmd.CreateMission();
        param.setNavi(new CreateTaskAndNaviCmd.CreateMission.Navi());
        param.getNavi().setMissionId("123456789");
        param.getNavi().setDestCode("D001");
        param.setTask(new CreateTaskAndNaviCmd.CreateMission.Task());
        param.getTask().setMissionId("123456789");
        cmd.setParam(param);
        System.out.println("添加命令");
        engine.addCommand(cmd);

        // 模拟车辆反馈任务接收，实际上车辆会返回的202接收任务，接收任务后不会立刻完成，所以对于CreateMissionAction节点
        System.out.println("模拟车辆反馈任务接收");
        engine.updateActor("A501", actor -> {
            actor.state().setTask(State.RUNNING.getValue());
            return actor;
        });
        System.out.println("模拟车辆反馈导航接收");
        engine.updateActor("A501", actor -> {
            actor.state().setNavigation(State.RUNNING.getValue());
            return actor;
        });

        System.out.println("重复模拟车辆反馈导航接收");
        engine.updateActor("A501", actor -> {
            actor.state().setNavigation(State.RUNNING.getValue());
            return actor;
        });

        System.out.println("模拟插入急停命令");
        EmergencyCmd emergencyCmd = new EmergencyCmd();
        emergencyCmd.setId("111");
        emergencyCmd.setActorId("A501");
        //急停命令不需要missionId
//        emergencyCmd.setMissionId("987654321");
        engine.addCommandFirst(emergencyCmd);

        System.out.println("模拟车辆反馈急停反馈-接收");
        engine.updateActor("A501", actor -> {
            actor.state().setEmergency(State.RUNNING.getValue());
            return actor;
        });

        System.out.println("模拟车辆反馈急停反馈-完成");
        engine.updateActor("A501", actor -> {
            actor.state().setEmergency(State.FINISH.getValue());
            return actor;
        });

        System.out.println("模拟车辆反馈急停恢复反馈-完成");
        engine.updateActor("A501", actor -> {
            actor.state().setEmergency(State.START.getValue());
            return actor;
        });
        System.out.println("模拟车辆反馈导航完成");
        engine.updateActor("A501", actor -> {
            actor.state().setNavigation(State.FINISH.getValue());
            return actor;
        });

        Command<VehicleState> command = commandQueue.peek("A501");
        System.out.println("command: " + command);

    }




}
