package org.cactus.auton;

import jakarta.annotation.PostConstruct;
import org.cactus.auton.builder.TreeBuilder;
import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;
import org.cactus.auton.command.GeneralCommand;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Consumer;

@RestController
@RequestMapping("/api/test")
public class TreeTest {
    enum Commands{
        CREATE_TASK_AND_NAVI,EMERGENCY_CMD,NO_EMERGENCY_CMD
    }

    /** 反馈码：接收 */
    private static final int CODE_ACCEPT = 202;
    /** 反馈码：完成 */
    private static final int CODE_FINISH = 200;
    /** 反馈码：异常 */
    private static final int CODE_ERROR = 500;

    private CommandBehaviorTreeEngine<VehicleState> engine;

    private CommandManger commandManger;
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

        // 注册命令
        Map<String,Node<VehicleState>> treeMap = new HashMap<>();
        treeMap.put(Commands.CREATE_TASK_AND_NAVI.name(), createTaskAndNaviCmd);

        treeMap.put(Commands.EMERGENCY_CMD.name(), new EmergencyAction(Commands.EMERGENCY_CMD.name(), "急停命令"));
        treeMap.put(Commands.NO_EMERGENCY_CMD.name(), new EmergencyAction(Commands.NO_EMERGENCY_CMD.name(), "急停恢复命令"));
        commandManger =  new MapCommandManger();
        actorRepository = new MapActorRepository();
        // 创建行为树引擎
        engine = new VehicleBehaviorEngine(treeMap, actorRepository, commandManger);

        // 注入Service
        ServiceRegistry.register(VehicleService.class, new VehicleService());

    }

    // ==================== 模拟接收外部消息（供接口测试使用） ====================

    /** 模拟车辆登录 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam(value = "actorId") String actorId) {
        engine.updateActor(actorId, actor -> {
            actor.state().setLogin(State.FINISH.getValue());
            return actor;
        });
        return ok("车辆已登录: " + actorId, buildState(actorId));
    }

    /** 模拟调度下发「创建任务 + 导航」命令（追加到命令队列） */
    @PostMapping("/mission/create")
    public Map<String, Object> createMission(@RequestParam(value = "actorId") String actorId,
                                             @RequestParam(value = "missionId") String missionId,
                                             @RequestParam(value = "destCode", defaultValue = "D001") String destCode,
                                             @RequestParam(value = "operation") String operation
                                             ) {

        GeneralCommand command = buildCmd(
                Commands.CREATE_TASK_AND_NAVI.name(),
                actorId,
                missionId,
                (params)->{
                    params.put("destCode",destCode);
                });
        engine.submit(operation, command);
        return ok("已下发创建任务命令，cmdId=" + command.id() + "，missionId=" + command.missionId(), buildState(actorId));
    }


    /** 模拟调度下发「急停/急停恢复」 */
    @PostMapping("/emergency")
    public Map<String, Object> emergency(@RequestParam(value = "actorId") String actorId,
                                             @RequestParam(value = "stop", required = false) Boolean stop) {

        String cmdName = Commands.EMERGENCY_CMD.name();
        if(!stop){
            cmdName = Commands.NO_EMERGENCY_CMD.name();
        }
        GeneralCommand command = buildCmd(
                cmdName,
                actorId,
                null,
                (params)->{
                });
        engine.submit("SAFETY", command);
        return ok("已下发创建任务命令，cmdId=" + command.id() + "，missionId=" + command.missionId(), buildState(actorId));
    }


    /** 模拟车辆反馈 */
    @PostMapping("/feedback")
    public Map<String, Object> feedback(@RequestParam(value = "actorId") String actorId,
                                          @RequestParam(value = "missionId") String missionId,
                                          @RequestParam(value = "callbackType") String type,
                                          @RequestParam(value = "code") int code

                                          ) {
        if (code != CODE_ACCEPT && code != CODE_FINISH && code != CODE_ERROR) {
            return fail("不支持的反馈码: " + code + "，可选 202(接收)|200(完成)|500(异常)");
        }
        // 与任务相关的反馈需校验 missionId，不一致则忽略
        if (needMissionId(type)) {
            Actor<VehicleState> actor = actorRepository.find(actorId);
            String currentMissionId = actor == null ? null : actor.state().getMissionId();
            if (!Objects.equals(currentMissionId, missionId)) {
                return fail("missionId 不匹配，已忽略该反馈。当前任务=" + currentMissionId + "，反馈任务=" + missionId);
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
        return ok("已模拟接收反馈 type=" + type + "，code=" + code + "，missionId=" + missionId, buildState(actorId));
    }


    // ---------- 查询与重置 ----------

    /** 查询车辆当前状态与命令队列 */
    @GetMapping("/state")
    public Map<String, Object> state(@RequestParam(value = "actorId") String actorId) {
        return ok("ok", buildState(actorId));
    }

    /** 重置车辆：清空命令队列与状态 */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam(value = "actorId") String actorId) {
        commandManger.clearActorCommands(actorId);
        actorRepository.delete(actorId);
        return ok("已重置车辆: " + actorId, null);
    }

    // ==================== 内部工具方法 ====================



    /** 组装状态快照，避免直接序列化 Actor/Command（二者不是标准 JavaBean） */
    private Map<String, Object> buildState(String actorId) {
        Actor<VehicleState> actor = actorRepository.find(actorId);
        List<Command> commands = commandManger.findCommands(actorId);

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
            for (Command cmd : commands) {
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

    private GeneralCommand buildCmd(String type, String actorId, String missionId, Consumer<Map<String,Object>> paramsConsumer){
        GeneralCommand command = new GeneralCommand();
        command.setId(UUID.randomUUID().toString());
        command.setActorId(actorId);
        command.setType(type);
        command.setMissionId(missionId);
        Map<String,Object> params = new HashMap<>();
        paramsConsumer.accept(params);
        command.setParams(params);
        return command;
    }

}
