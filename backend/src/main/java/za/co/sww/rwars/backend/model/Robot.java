package za.co.sww.rwars.backend.model;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a robot in the battle.
 */
@Schema(description = "Entity representing a robot participant in the battle")
public class Robot {
    /**
     * Enum for robot status.
     */
    @Schema(description = "Current status of the robot")
    public enum RobotStatus {
        IDLE,
        MOVING,
        CRASHED,
        DESTROYED
    }

    /**
     * Enum for robot direction.
     */
    @Schema(description = "Direction the robot is facing or moving")
    public enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        NE,
        NW,
        SE,
        SW
    }

    @Schema(description = "Unique identifier of the robot", example = "robot-123")
    private String id;

    @Schema(description = "Name of the robot", example = "DestroyerBot")
    private String name;

    @Schema(description = "ID of the battle this robot is participating in", example = "battle-456")
    private String battleId;

    @Schema(description = "X coordinate position in the arena", example = "25")
    private int positionX;

    @Schema(description = "Y coordinate position in the arena", example = "30")
    private int positionY;

    @Schema(description = "Current direction the robot is facing")
    private Direction direction;

    @Schema(description = "Current status of the robot")
    private RobotStatus status;

    @Schema(description = "Target number of blocks to move", example = "5")
    private int targetBlocks;

    @Schema(description = "Number of blocks remaining to move", example = "2")
    private int blocksRemaining;

    @Schema(description = "Current hit points of the robot", example = "85")
    private int hitPoints;

    @Schema(description = "Maximum hit points the robot can have", example = "100")
    private int maxHitPoints;

    public Robot() {
        this.id = UUID.randomUUID().toString();
        this.positionX = 0;
        this.positionY = 0;
        this.direction = Direction.NORTH;
        this.status = RobotStatus.IDLE;
        this.targetBlocks = 0;
        this.blocksRemaining = 0;
        this.hitPoints = 100; // Default hit points
        this.maxHitPoints = 100;
    }

    public Robot(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.positionX = 0;
        this.positionY = 0;
        this.direction = Direction.NORTH;
        this.status = RobotStatus.IDLE;
        this.targetBlocks = 0;
        this.blocksRemaining = 0;
        this.hitPoints = 100; // Default hit points
        this.maxHitPoints = 100;
    }

    public Robot(String name, String battleId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.battleId = battleId;
        this.positionX = 0;
        this.positionY = 0;
        this.direction = Direction.NORTH;
        this.status = RobotStatus.IDLE;
        this.targetBlocks = 0;
        this.blocksRemaining = 0;
        this.hitPoints = 100; // Default hit points
        this.maxHitPoints = 100;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public RobotStatus getStatus() {
        return status;
    }

    public void setStatus(RobotStatus status) {
        this.status = status;
    }

    public int getTargetBlocks() {
        return targetBlocks;
    }

    public void setTargetBlocks(int targetBlocks) {
        this.targetBlocks = targetBlocks;
    }

    public int getBlocksRemaining() {
        return blocksRemaining;
    }

    public void setBlocksRemaining(int blocksRemaining) {
        this.blocksRemaining = blocksRemaining;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
        if (hitPoints <= 0 && status != RobotStatus.CRASHED) {
            this.status = RobotStatus.DESTROYED;
        }
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }

    public void takeDamage(int damage) {
        this.hitPoints = Math.max(0, this.hitPoints - damage);
        if (this.hitPoints == 0 && status != RobotStatus.CRASHED) {
            this.status = RobotStatus.DESTROYED;
        }
    }

    public boolean isActive() {
        return hitPoints > 0 && status != RobotStatus.CRASHED && status != RobotStatus.DESTROYED;
    }
}
