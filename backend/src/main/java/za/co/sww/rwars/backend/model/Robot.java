package za.co.sww.rwars.backend.model;

import java.util.UUID;

/**
 * Represents a robot in the battle.
 */
public class Robot {
    /**
     * Enum for robot status.
     */
    public enum RobotStatus {
        IDLE,
        MOVING,
        CRASHED,
        DESTROYED
    }

    /**
     * Enum for robot direction.
     */
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

    private String id;
    private String name;
    private String battleId;
    private int positionX;
    private int positionY;
    private Direction direction;
    private RobotStatus status;
    private int targetBlocks;
    private int blocksRemaining;
    private int hitPoints;
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
