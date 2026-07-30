package org.cactus.auton.config;

import org.cactus.auton.state.VehicleState;
import org.cactus.auton.subject.Actor;
import org.cactus.auton.subject.ActorRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Actor 仓库实现。
 *
 */
public class MapActorRepository implements ActorRepository<VehicleState> {

    private final Map<String, Actor<VehicleState>> store = new ConcurrentHashMap<>();

    @Override
    public Actor<VehicleState> find(String id) {

        return store.get(id);
    }

    @Override
    public void save(Actor<VehicleState> actor) {
        store.put(actor.id(), actor);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}
