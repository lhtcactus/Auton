package org.cactus.auton.config;

import org.cactus.auton.command.Command;
import org.cactus.auton.command.CommandManger;
import org.cactus.auton.context.TickContext;
import org.cactus.auton.context.TreeTracer;
import org.cactus.auton.engine.CommandBehaviorTreeEngine;
import org.cactus.auton.exception.BehaviorException;
import org.cactus.auton.node.Node;
import org.cactus.auton.node.Status;
import org.cactus.auton.state.State;
import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;
import org.cactus.auton.subject.DefaultActor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class VehicleBehaviorEngine extends CommandBehaviorTreeEngine<VehicleState> {

    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();

    public VehicleBehaviorEngine(Map<String, Node<VehicleState>> nodeCommandMap, ActorRepository<VehicleState> actorRepository, CommandManger commandManger) {
        super(nodeCommandMap, actorRepository, commandManger);
    }


    @Override
    protected TreeTracer<VehicleState> getTreeTracer() {
        return new TreeTracer<VehicleState>() {
            @Override
            public void onNodeEnter(Node<VehicleState> node, TickContext<VehicleState> ctx) {
//                System.out.println("Node entered: " + node.getName() + " for actor " + ctx.actor());
            }
            @Override
            public void onNodeExit(Node<VehicleState> node, TickContext<VehicleState> ctx, Status status) {
//                System.out.println("Node exited: " + node.getName() + " for actor " + ctx.actor() + " with status " + status);
            }
        };
    }

    @Override
    protected boolean afterTick(Status status, TickContext<VehicleState> context) {
        boolean isCloseCommand = false;
        if (Status.BuiltIn.SUCCESS.sameStateAs(status)){
            isCloseCommand = true;
            System.out.println("    Command completed: " + context.command());
        }else if (Status.BuiltIn.FAILURE.sameStateAs(status)){
            isCloseCommand = true;
            System.out.println("    Command failed: " + context.command());
        }else{
            System.out.println("    Command is running/abort: " + context.command());
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
        vehicleState.setRecover(State.START.getValue());
        vehicleState.setCancelTask(State.START.getValue());

        defaultActor.setState(vehicleState);
        return defaultActor;
    }

    @Override
    public Lock getLock(String actorId) {
        return  lockMap.computeIfAbsent(
                actorId,
                k -> new Lock() {
                    final ReentrantLock reentrantLock = new ReentrantLock();
                    @Override
                    public void lock() throws BehaviorException {
                        try {
                            boolean b = reentrantLock.tryLock(10, TimeUnit.SECONDS);
                            if(!b){
                                throw new BehaviorException("无法获取锁");
                            }
                        } catch (InterruptedException e) {
                            throw new BehaviorException(e);
                        }
                    }

                    @Override
                    public void unlock() {
                        reentrantLock.unlock();
                    }
                }
        );
    }

}
