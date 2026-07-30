package org.cactus.auton.config;

import org.cactus.auton.command.CommandQueue;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.context.TreeTracer;
import org.cactus.auton.engine.CommandBehaviorTreeEngine;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;
import org.cactus.auton.subject.DefaultActor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class VehicleBehaviorEngine extends CommandBehaviorTreeEngine<VehicleState> {

    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public VehicleBehaviorEngine(Map<String, Node<VehicleState>> nodeCommandMap, ActorRepository<VehicleState> actorRepository, CommandQueue<VehicleState> commandQueue) {
        super(nodeCommandMap, actorRepository, commandQueue);
    }


    @Override
    protected TreeTracer<VehicleState> getTreeTracer() {
        return new TreeTracer<VehicleState>() {
            @Override
            public void onNodeEnter(Node<VehicleState> node, TickContext<VehicleState> ctx) {
                System.out.println("Node entered: " + node.getName() + " for actor " + ctx.actor());
            }
            @Override
            public void onNodeExit(Node<VehicleState> node, TickContext<VehicleState> ctx, Status status) {
                System.out.println("Node exited: " + node.getName() + " for actor " + ctx.actor() + " with status " + status);
            }
        };
    }

    @Override
    protected boolean afterTick(Status status, TickContext<VehicleState> context) {
        boolean isCloseCommand = false;
        if (Status.BuiltIn.SUCCESS.sameStateAs(status)){
            isCloseCommand = true;
            System.out.println("Command completed: " + context.command());
        }else if (Status.BuiltIn.FAILURE.sameStateAs(status)){
            isCloseCommand = true;
            System.out.println("Command failed: " + context.command());
        }else{
            System.out.println("Command is running/abort: " + context.command());
        }

        return isCloseCommand;
    }

    @Override
    protected Actor<VehicleState> createDefaultActor(String actorId) {
        DefaultActor<VehicleState> defaultActor = new DefaultActor<>();
        defaultActor.setId(actorId);
        VehicleState vehicleState = new VehicleState();
        vehicleState.setNavigation(State.START.getValue());
        vehicleState.setTask(State.START.getValue());
        vehicleState.setEmergency(State.START.getValue());
        vehicleState.setLogin(State.START.getValue());

        defaultActor.setState(vehicleState);
        return defaultActor;
    }

    @Override
    protected Lock getLock(String actorId) {
        return  lockMap.computeIfAbsent(
                actorId,
                k -> new Lock() {
                    final ReentrantLock reentrantLock = new ReentrantLock();
                    @Override
                    public void lock() {
                        reentrantLock.lock();
                    }

                    @Override
                    public void unlock() {
                        reentrantLock.unlock();
                    }
                }
        );
    }

}
