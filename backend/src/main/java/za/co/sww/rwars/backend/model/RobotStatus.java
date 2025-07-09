package za.co.sww.rwars.backend.model;

/**
 * Represents a robot's status information without revealing its absolute position.
 * This is what robots should know about themselves - their identity and state,
 * but not their absolute coordinates in the arena.
 */
public class RobotStatus {
    private String id;
    private String name;
    private String battleId;
    private Robot.Direction direction;
    private Robot.RobotStatus status;
    private int targetBlocks;
    private int blocksRemaining;
    private int hitPoints;
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
