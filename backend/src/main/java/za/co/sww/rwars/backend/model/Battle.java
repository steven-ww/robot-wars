package za.co.sww.rwars.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a battle with its state, arena dimensions, and participating robots.
 */
@Schema(description = "Entity representing a battle arena")
public class Battle {

    @Schema(description = "Represents the current state of the battle")
    public enum BattleState {
        WAITING_ON_ROBOTS,
        READY,
        IN_PROGRESS,
        COMPLETED
    }

    @Schema(description = "Unique identifier of the battle", example = "123e4567-e89b-12d3-a456-556642440000")
    private String id;

    @Schema(description = "Name of the battle", example = "Epic Battle")
    private String name;

    @Schema(description = "Width of the arena in grid units", example = "50")
    private int arenaWidth;

    @Schema(description = "Height of the arena in grid units", example = "50")
    private int arenaHeight;

    @Schema(description = "Time allowed for robot movements in seconds", example = "1.0")
    private double robotMovementTimeSeconds;

    @Schema(description = "List of robots participating in the battle")
    private List<Robot> robots;

    @Schema(description = "Current state of the battle")
    private BattleState state;

    @Schema(description = "List of walls in the arena")
    private List<Wall> walls;

    @Schema(description = "ID of the winning robot", example = "robot-123")
    private String winnerId;

    @Schema(description = "Name of the winning robot", example = "Crusher")
    private String winnerName;

    public Battle() {
        this.id = UUID.randomUUID().toString();
        this.robots = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
        this.robotMovementTimeSeconds = 1.0; // Default value
    }

    public Battle(String name, int arenaWidth, int arenaHeight) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.robots = new ArrayList<>();
        this.walls = new ArrayList<>();
        this.state = BattleState.WAITING_ON_ROBOTS;
        this.robotMovementTimeSeconds = 1.0; // Default value
    }

    public Battle(String name, int arenaWidth, int arenaHeight, double robotMovementTimeSeconds) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.robots = new ArrayList<>();
        this.walls = new ArrayList<>();
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

    public List<Wall> getWalls() {
        return walls;
    }

    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }

    public void addWall(Wall wall) {
        this.walls.add(wall);
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public void declareWinner(Robot winner) {
        this.winnerId = winner.getId();
        this.winnerName = winner.getName();
        this.state = BattleState.COMPLETED;
    }

    public boolean isPositionOccupiedByWall(int x, int y) {
        return walls.stream().anyMatch(wall -> wall.containsPosition(x, y));
    }

    public long getActiveRobotCount() {
        return robots.stream().filter(Robot::isActive).count();
    }

    public Robot getActiveRobot() {
        return robots.stream().filter(Robot::isActive).findFirst().orElse(null);
    }
}
