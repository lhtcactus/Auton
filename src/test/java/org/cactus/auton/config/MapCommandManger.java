package org.cactus.auton.config;

import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapCommandManger implements CommandManger {
    private final Map<String, LinkedList<Command>> map = new HashMap<>();
    private final Map<String,Command> safetyMap = new HashMap<>();
    @Override
    public void submit(String operation, Command command) {

        switch (operation) {

            case "APPEND":
                map
                    .computeIfAbsent(command.actorId(),s -> new LinkedList<>())
                    .add(command);
                break;

            case "REPLACE":
                LinkedList<Command> commands = new LinkedList<>();
                commands.add(command);
                map.put(command.actorId(),commands);
                break;

            case "SAFETY":
                Command safeCommand = safetyMap.get(command.actorId());
                if(safeCommand!=null){
                    throw new RuntimeException("当前"+command.actorId()+"已有安全指令，不能下发新的安全指令");
                }
                safetyMap.put(command.actorId(),command);
                break;
        }

    }

    @Override
    public Command next(String actorId) {
        //优先检查安全指令
        Command safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null){
            return safeCommand;
        }
        LinkedList<Command> l = map.get(actorId);
        if(l != null && !l.isEmpty()){
            return l.getFirst();
        }
        return null;
    }

    @Override
    public List<Command> findCommands(String actorId) {
        LinkedList<Command> list = map.getOrDefault(actorId,new LinkedList<>());
        Command safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null){
            LinkedList<Command> l =  new LinkedList<>(list);
            l.addFirst(safeCommand);
            return l;
        }
        return list;
    }

    @Override
    public void removeById(String actorId, String commandId) {
        Command safeCommand = safetyMap.get(actorId);
        if(safeCommand!=null && safeCommand.id().equals(commandId)) {
            safetyMap.remove(actorId);
            return;
        }
        LinkedList<Command> list = map.getOrDefault(actorId,new LinkedList<>());
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
