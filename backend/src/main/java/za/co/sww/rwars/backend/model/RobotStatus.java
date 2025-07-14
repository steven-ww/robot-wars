package za.co.sww.rwars.backend.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a robot's status information without revealing its absolute position.
 * This is what robots should know about themselves - their identity and state,
 * but not their absolute coordinates in the arena.
 */
@Schema(description = "Robot status information without revealing absolute position")
public class RobotStatus {
    @Schema(description = "Unique identifier of the robot", example = "robot-123")
    private String id;

    @Schema(description = "Name of the robot", example = "DestroyerBot")
    private String name;

    @Schema(description = "ID of the battle this robot is participating in", example = "battle-456")
    private String battleId;

    @Schema(description = "Current direction the robot is facing")
    private Robot.Direction direction;

    @Schema(description = "Current status of the robot")
    private Robot.RobotStatus status;

    @Schema(description = "Target number of blocks to move", example = "5")
    private int targetBlocks;

    @Schema(description = "Number of blocks remaining to move", example = "2")
    private int blocksRemaining;

    @Schema(description = "Current hit points of the robot", example = "85")
    private int hitPoints;

    @Schema(description = "Maximum hit points the robot can have", example = "100")
    private int maxHitPoints;

    public RobotStatus() {
    }

    /**
     * Creates a RobotStatus from a Robot, excluding position information.
     *
     * @param robot The robot to create status from
     */
    public RobotStatus(Robot robot) {
        this.id = robot.getId();
        this.name = robot.getName();
        this.battleId = robot.getBattleId();
        this.direction = robot.getDirection();
        this.status = robot.getStatus();
        this.targetBlocks = robot.getTargetBlocks();
        this.blocksRemaining = robot.getBlocksRemaining();
        this.hitPoints = robot.getHitPoints();
        this.maxHitPoints = robot.getMaxHitPoints();
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

    public Robot.Direction getDirection() {
        return direction;
    }

    public void setDirection(Robot.Direction direction) {
        this.direction = direction;
    }

    public Robot.RobotStatus getStatus() {
        return status;
    }

    public void setStatus(Robot.RobotStatus status) {
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
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public void setMaxHitPoints(int maxHitPoints) {
        this.maxHitPoints = maxHitPoints;
    }
}
