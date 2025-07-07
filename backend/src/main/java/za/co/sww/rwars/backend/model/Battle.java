package za.co.sww.rwars.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a battle with its state, arena dimensions, and participating robots.
 */
public class Battle {

    public enum BattleState {
        WAITING_ON_ROBOTS,
        READY,
        IN_PROGRESS,
        COMPLETED
    }

    private String id;
    private String name;
    private int arenaWidth;
    private int arenaHeight;
    private double robotMovementTimeSeconds;
    private List<Robot> robots;
    private BattleState state;

    public Battle() {
        this.id = UUID.randomUUID().toString();
        this.robots = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
        this.robotMovementTimeSeconds = 1.0; // Default value
    }

    public Battle(String name, int arenaWidth, int arenaHeight) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.robots = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
        this.robotMovementTimeSeconds = 1.0; // Default value
    }

    public Battle(String name, int arenaWidth, int arenaHeight, double robotMovementTimeSeconds) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.robots = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
        this.robotMovementTimeSeconds = robotMovementTimeSeconds;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getArenaWidth() {
        return arenaWidth;
    }

    public void setArenaWidth(int arenaWidth) {
        this.arenaWidth = arenaWidth;
    }

    public int getArenaHeight() {
        return arenaHeight;
    }

    public void setArenaHeight(int arenaHeight) {
        this.arenaHeight = arenaHeight;
    }

    public double getRobotMovementTimeSeconds() {
        return robotMovementTimeSeconds;
    }

    public void setRobotMovementTimeSeconds(double robotMovementTimeSeconds) {
        this.robotMovementTimeSeconds = robotMovementTimeSeconds;
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
