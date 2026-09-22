package org.cactus.auton.cmd;

import org.cactus.auton.blackboard.Blackboard;
import org.cactus.auton.command.AbstractCommand;
import org.cactus.auton.state.VehicleState;

public class NoEmergencyCmd extends AbstractCommand<String, VehicleState> {
    @Override
    public String type() {
        return "NO_EMERGENCY_CMD";
    }

    @Override
    public void fillParams(Blackboard blackboard) {
    }

    @Override
    public String toString() {
        return "NoEmergencyCmd{" +
                "id='" + id + '\'' +
                ", missionId='" + missionId + '\'' +
                ", type='" + type + '\'' +
                ", actorId='" + actorId + '\'' +
                ", param=" + param +
                '}';
    }
}
