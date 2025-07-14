package za.co.sww.rwars.backend.model;

import java.time.LocalDateTime;

/**
 * Represents a robot action that occurred during a battle.
 * Used to track what robots are doing for display in the frontend.
 */
public class RobotAction {
    private String robotId;
    private String robotName;
    private String action;
    private LocalDateTime timestamp;

    /**
     * Default constructor for JSON serialization.
     */
    public RobotAction() {
    }

    /**
     * Constructor for creating a new robot action.
     *
     * @param robotId The ID of the robot performing the action
     * @param robotName The name of the robot performing the action
     * @param action The action being performed (e.g., "move", "radar", "fire_laser")
     * @param timestamp When the action occurred
     */
    public RobotAction(String robotId, String robotName, String action, LocalDateTime timestamp) {
        this.robotId = robotId;
        this.robotName = robotName;
        this.action = action;
        this.timestamp = timestamp;
    }

    /**
     * Convenience constructor that uses the current time.
     *
     * @param robotId The ID of the robot performing the action
     * @param robotName The name of the robot performing the action
     * @param action The action being performed
     */
    public RobotAction(String robotId, String robotName, String action) {
        this(robotId, robotName, action, LocalDateTime.now());
    }

    // Getters and setters
    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "RobotAction{" +
                "robotId='" + robotId + '\'' +
                ", robotName='" + robotName + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
