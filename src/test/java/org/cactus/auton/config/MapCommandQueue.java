package org.cactus.auton.config;

import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandQueue;
import org.cactus.auton.state.VehicleState;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 命令队列Map实现。
 */
public class MapCommandQueue implements CommandQueue<VehicleState> {

    private final Map<String, Deque<Command<VehicleState>>> commandMap = new ConcurrentHashMap<>();

    @Override
    public void enqueue(Command<VehicleState> command) {

        commandMap.computeIfAbsent(command.actorId(), k -> new LinkedBlockingDeque<>())
                .add(command);
    }

    @Override
    public void insertFirst(Command<VehicleState> command) {

        commandMap.computeIfAbsent(command.actorId(), k -> new LinkedBlockingDeque<>())
                .addFirst(command);
    }

    @Override
    public Command<VehicleState> peek(String actorId) {
        Deque<Command<VehicleState>> actorQueue = commandMap.get(actorId);
        return actorQueue == null ? null : actorQueue.peek();
    }

    @Override
    public void clearActorCommands(String actorId) {
        commandMap.remove(actorId);
    }

    @Override
    public void removeByCmdId(String actorId,String commandId) {
        Deque<Command<VehicleState>> actorQueue = commandMap.get(actorId);
        if(actorQueue!=null){
            actorQueue.removeIf(command -> command.id().equals(commandId));
        }
    }

    @Override
    public void clear() {
        commandMap.clear();
    }
}
