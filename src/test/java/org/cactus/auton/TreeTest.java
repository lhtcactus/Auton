package org.cactus.auton;

import jakarta.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TreeTest {
    /** 默认测试车辆 */
    private static final String DEFAULT_ACTOR_ID = "A501";

    /** 反馈码：接收 */
    private static final int CODE_ACCEPT = 202;
    /** 反馈码：完成 */
    private static final int CODE_FINISH = 200;
    /** 反馈码：异常 */
    private static final int CODE_ERROR = 500;

    private CommandBehaviorTreeEngine<VehicleState> engine;

    private CommandManger<VehicleState> commandManger;
    private ActorRepository<VehicleState> actorRepository;

    @PostConstruct
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
        ServiceRegistry.register(VehicleService.class, new VehicleService());

    }

    // ==================== 模拟接收外部消息（供接口测试使用） ====================

    /** 模拟车辆登录 */
    @PostMapping("/vehicle/login")
    public Map<String, Object> login(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        engine.updateActor(actorId, actor -> {
            actor.state().setLogin(State.FINISH.getValue());
            return actor;
        });
        return ok("车辆已登录: " + actorId, buildState(actorId));
    }

    /** 模拟调度下发「创建任务 + 导航」命令（追加到命令队列） */
    @PostMapping("/mission/create")
    public Map<String, Object> createMission(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                             @RequestParam(value = "cmdId", required = false) String cmdId,
                                             @RequestParam(value = "missionId", required = false) String missionId,
                                             @RequestParam(value = "destCode", defaultValue = "D001") String destCode) {
        CreateTaskAndNaviCmd cmd = buildCreateTaskCmd(actorId, cmdId, missionId, destCode);
        engine.submit("APPEND", cmd);
        return ok("已下发创建任务命令，cmdId=" + cmd.id() + "，missionId=" + cmd.missionId(), buildState(actorId));
    }

    /** 模拟调度下发任务更新（覆盖当前命令） */
    @PostMapping("/mission/replace")
    public Map<String, Object> replaceMission(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                              @RequestParam(value = "cmdId", required = false) String cmdId,
                                              @RequestParam(value = "missionId", required = false) String missionId,
                                              @RequestParam(value = "destCode", defaultValue = "D001") String destCode) {
        CreateTaskAndNaviCmd cmd = buildCreateTaskCmd(actorId, cmdId, missionId, destCode);
        engine.submit("REPLACE", cmd);
        return ok("已下发任务更新命令，cmdId=" + cmd.id() + "，missionId=" + cmd.missionId(), buildState(actorId));
    }

    /** 模拟下发急停命令（安全命令，优先执行） */
    @PostMapping("/emergency")
    public Map<String, Object> emergency(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                         @RequestParam(value = "cmdId", required = false) String cmdId) {
        EmergencyCmd cmd = new EmergencyCmd();
        cmd.setId(cmdId == null || cmdId.isBlank() ? UUID.randomUUID().toString() : cmdId);
        cmd.setActorId(actorId);
        engine.submit("SAFETY", cmd);
        return ok("已下发急停命令，cmdId=" + cmd.id(), buildState(actorId));
    }

    // ---------- 车辆反馈：任务 ----------

    /** 模拟接收到「任务接收」(202) */
    @PostMapping("/feedback/task/accept")
    public Map<String, Object> taskAccept(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                          @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "createMission", missionId, CODE_ACCEPT);
    }

    /** 模拟接收到「任务完成」(200) */
    @PostMapping("/feedback/task/finish")
    public Map<String, Object> taskFinish(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                          @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "createMission", missionId, CODE_FINISH);
    }

    // ---------- 车辆反馈：导航 ----------

    /** 模拟接收到「导航接受」(202) */
    @PostMapping("/feedback/navi/accept")
    public Map<String, Object> naviAccept(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                          @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "createNavi", missionId, CODE_ACCEPT);
    }

    /** 模拟接收到「导航到达」(200) */
    @PostMapping("/feedback/navi/arrive")
    public Map<String, Object> naviArrive(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                          @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "createNavi", missionId, CODE_FINISH);
    }

    /** 模拟接收到「导航异常」(500) */
    @PostMapping("/feedback/navi/error")
    public Map<String, Object> naviError(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                         @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "createNavi", missionId, CODE_ERROR);
    }

    // ---------- 车辆反馈：取消任务 ----------

    /** 模拟接收到「取消任务接收」(202) */
    @PostMapping("/feedback/cancel/accept")
    public Map<String, Object> cancelAccept(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                            @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "cancelMission", missionId, CODE_ACCEPT);
    }

    /** 模拟接收到「取消任务完成」(200) */
    @PostMapping("/feedback/cancel/finish")
    public Map<String, Object> cancelFinish(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                            @RequestParam(value = "missionId", required = false) String missionId) {
        return applyFeedback(actorId, "cancelMission", missionId, CODE_FINISH);
    }

    // ---------- 车辆反馈：急停 ----------

    /** 模拟接收到「急停接收」(202) */
    @PostMapping("/feedback/emergency/accept")
    public Map<String, Object> emergencyAccept(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        return applyFeedback(actorId, "emergency", null, CODE_ACCEPT);
    }

    /** 模拟接收到「急停完成」(200) */
    @PostMapping("/feedback/emergency/finish")
    public Map<String, Object> emergencyFinish(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        return applyFeedback(actorId, "emergency", null, CODE_FINISH);
    }

    // ---------- 车辆反馈：急停恢复 ----------

    /** 模拟接收到「急停恢复接收」(202) */
    @PostMapping("/feedback/recover/accept")
    public Map<String, Object> recoverAccept(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        return applyFeedback(actorId, "noEmergency", null, CODE_ACCEPT);
    }

    /** 模拟接收到「急停恢复完成」(200) */
    @PostMapping("/feedback/recover/finish")
    public Map<String, Object> recoverFinish(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        return applyFeedback(actorId, "noEmergency", null, CODE_FINISH);
    }

    /** 通用反馈接口：type=createMission|createNavi|cancelMission|emergency|noEmergency，code=202|200|500 */
    @PostMapping("/feedback")
    public Map<String, Object> feedback(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId,
                                        @RequestParam(value = "type") String type,
                                        @RequestParam(value = "missionId", required = false) String missionId,
                                        @RequestParam(value = "code", defaultValue = "202") int code) {
        return applyFeedback(actorId, type, missionId, code);
    }

    // ---------- 查询与重置 ----------

    /** 查询车辆当前状态与命令队列 */
    @GetMapping("/state")
    public Map<String, Object> state(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        return ok("ok", buildState(actorId));
    }

    /** 重置车辆：清空命令队列与状态 */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam(value = "actorId", defaultValue = DEFAULT_ACTOR_ID) String actorId) {
        commandManger.clearActorCommands(actorId);
        actorRepository.delete(actorId);
        return ok("已重置车辆: " + actorId, null);
    }

    // ==================== 内部工具方法 ====================

    /** 构造「创建任务 + 导航」命令 */
    private CreateTaskAndNaviCmd buildCreateTaskCmd(String actorId, String cmdId, String missionId, String destCode) {
        String mid = blank(missionId) ? UUID.randomUUID().toString() : missionId;
        CreateTaskAndNaviCmd cmd = new CreateTaskAndNaviCmd();
        cmd.setId(blank(cmdId) ? UUID.randomUUID().toString() : cmdId);
        cmd.setActorId(actorId);
        cmd.setMissionId(mid);

        CreateTaskAndNaviCmd.CreateMission param = new CreateTaskAndNaviCmd.CreateMission();
        param.setDeviceId(actorId);
        param.setNavi(new CreateTaskAndNaviCmd.CreateMission.Navi());
        param.getNavi().setMissionId(mid);
        param.getNavi().setDestCode(destCode);
        param.setTask(new CreateTaskAndNaviCmd.CreateMission.Task());
        param.getTask().setMissionId(mid);
        cmd.setParam(param);
        return cmd;
    }

    /**
     * 模拟接收到车辆反馈，按 type + code 更新车辆状态并触发 tick。
     */
    private Map<String, Object> applyFeedback(String actorId, String type, String missionId, int code) {
        if (!isSupportType(type)) {
            return fail("不支持的反馈类型: " + type
                    + "，可选 createMission|createNavi|cancelMission|emergency|noEmergency");
        }
        if (code != CODE_ACCEPT && code != CODE_FINISH && code != CODE_ERROR) {
            return fail("不支持的反馈码: " + code + "，可选 202(接收)|200(完成)|500(异常)");
        }
        // missionId 未传时取车辆当前任务
        String mid = missionId;
        if (blank(mid)) {
            Actor<VehicleState> actor = actorRepository.find(actorId);
            mid = actor == null ? null : actor.state().getMissionId();
        }
        // 与任务相关的反馈需校验 missionId，不一致则忽略
        if (needMissionId(type)) {
            Actor<VehicleState> actor = actorRepository.find(actorId);
            String currentMissionId = actor == null ? null : actor.state().getMissionId();
            if (!Objects.equals(currentMissionId, mid)) {
                return fail("missionId 不匹配，已忽略该反馈。当前任务=" + currentMissionId + "，反馈任务=" + mid);
            }
        }

        engine.updateActor(actorId, actor -> {
            VehicleState state = actor.state();
            switch (type) {
                case "createMission" -> state.setTask(stateValue(code));
                case "createNavi" -> state.setNavigation(stateValue(code));
                case "cancelMission" -> {
                    state.setCancelTask(stateValue(code));
                    if (code == CODE_FINISH) {
                        state.setTask(State.START.getValue());
                        state.setNavigation(State.START.getValue());
                        state.setMissionId(null);
                    }
                }
                case "emergency" -> state.setEmergency(stateValue(code));
                case "noEmergency" -> {
                    state.setRecover(stateValue(code));
                    if (code == CODE_FINISH) {
                        state.setEmergency(State.START.getValue());
                    }
                }
                default -> throw new IllegalArgumentException("不支持的反馈类型: " + type);
            }
            return actor;
        });
        return ok("已模拟接收反馈 type=" + type + "，code=" + code + "，missionId=" + mid, buildState(actorId));
    }

    /** 组装状态快照，避免直接序列化 Actor/Command（二者不是标准 JavaBean） */
    private Map<String, Object> buildState(String actorId) {
        Actor<VehicleState> actor = actorRepository.find(actorId);
        List<Command<VehicleState>> commands = commandManger.findCommands(actorId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("actorId", actorId);
        if (actor != null) {
            Map<String, Object> actorMap = new LinkedHashMap<>();
            actorMap.put("id", actor.id());
            actorMap.put("state", actor.state());
            actorMap.put("attributes", actor.attributes());
            data.put("actor", actorMap);
        } else {
            data.put("actor", null);
        }
        List<Map<String, Object>> commandList = new java.util.ArrayList<>();
        if (commands != null) {
            for (Command<VehicleState> cmd : commands) {
                Map<String, Object> cmdMap = new LinkedHashMap<>();
                cmdMap.put("id", cmd.id());
                cmdMap.put("type", cmd.type());
                cmdMap.put("actorId", cmd.actorId());
                cmdMap.put("missionId", cmd.missionId());
                cmdMap.put("detail", String.valueOf(cmd));
                commandList.add(cmdMap);
            }
        }
        data.put("commands", commandList);
        return data;
    }

    private String stateValue(int code) {
        if (code == CODE_ACCEPT) {
            return State.APPLY.getValue();
        }
        if (code == CODE_FINISH) {
            return State.FINISH.getValue();
        }
        return State.ERROR.getValue();
    }

    private boolean needMissionId(String type) {
        return "createMission".equals(type) || "createNavi".equals(type) || "cancelMission".equals(type);
    }

    private boolean isSupportType(String type) {
        return "createMission".equals(type) || "createNavi".equals(type) || "cancelMission".equals(type)
                || "emergency".equals(type) || "noEmergency".equals(type);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, Object> ok(String message, Object data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", message);
        if (data != null) {
            result.put("data", data);
        }
        return result;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }




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
