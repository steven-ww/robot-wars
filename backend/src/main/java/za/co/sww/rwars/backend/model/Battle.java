package za.co.sww.rwars.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a battle with its state and participating robots.
 */
public class Battle {

    public enum BattleState {
        WAITING_ON_ROBOTS,
        READY,
        IN_PROGRESS,
        COMPLETED
    }

    private String id;
    private List<Robot> robots;
    private BattleState state;

    public Battle() {
        this.id = UUID.randomUUID().toString();
        this.robots = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Robot> getRobots() {
        return robots;
    }

    public void setRobots(List<Robot> robots) {
        this.robots = robots;
    }

    public void addRobot(Robot robot) {
        this.robots.add(robot);
        updateState();
    }

    public BattleState getState() {
        return state;
    }

    public void setState(BattleState state) {
        this.state = state;
    }

    public int getRobotCount() {
        return robots.size();
    }

    private void updateState() {
        if (robots.size() >= 2) {
            this.state = BattleState.READY;
        }
    }

    public void startBattle() {
        if (this.state == BattleState.READY) {
            this.state = BattleState.IN_PROGRESS;
        }
    }
}
