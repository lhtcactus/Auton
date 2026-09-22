package org.cactus.auton.config;

import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;
import org.cactus.auton.state.VehicleState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapCommandManger implements CommandManger<VehicleState> {
    private final Map<String, LinkedList<Command<VehicleState>>> map = new HashMap<>();
    private final Map<String,Command<VehicleState>> safetyMap = new HashMap<>();
    @Override
    public void submit(String operation, Command<VehicleState> command) {

        switch (operation) {

            case "APPEND":
                map
                    .computeIfAbsent(command.actorId(),s -> new LinkedList<>())
                    .add(command);
                break;

            case "REPLACE":
                LinkedList<Command<VehicleState>> commands = new LinkedList<>();
                commands.add(command);
                map.put(command.actorId(),commands);
                break;

            case "SAFETY":
                Command<?> safeCommand = safetyMap.get(command.actorId());
                if(safeCommand!=null){
                    throw new RuntimeException("当前"+command.actorId()+"已有安全指令，不能下发新的安全指令");
                }
                safetyMap.put(command.actorId(),command);
                break;
        }

    }

    @Override
    public Command<VehicleState> next(String actorId) {
        //优先检查安全指令
        Command<VehicleState> safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null){
            return safeCommand;
        }
        LinkedList<Command<VehicleState>> l = map.get(actorId);
        if(l != null && !l.isEmpty()){
            return l.getFirst();
        }
        return null;
    }

    @Override
    public List<Command<VehicleState>> findCommands(String actorId) {
        LinkedList<Command<VehicleState>> list = map.getOrDefault(actorId,new LinkedList<>());
        Command<VehicleState> safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null){
            LinkedList<Command<VehicleState>> l =  new LinkedList<>(list);
            l.addFirst(safeCommand);
            return l;
        }
        return list;
    }

    @Override
    public void removeById(String actorId, String commandId) {
        Command<VehicleState> safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null && safeCommand.id().equals(commandId)) {
            safetyMap.remove(actorId);
            return;
        }
        LinkedList<Command<VehicleState>> list = map.getOrDefault(actorId,new LinkedList<>());
        list.removeIf(vehicleStateCommand -> vehicleStateCommand.id().equals(commandId));
    }

    @Override
    public void clearActorCommands(String actorId) {
        safetyMap.remove(actorId);
        map.remove(actorId);
    }

    @Override
    public void clear() {
        safetyMap.clear();
        map.clear();
    }
}
