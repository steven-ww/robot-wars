package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage battles and robots.
 */
@ApplicationScoped
public class BattleService {

    private Battle currentBattle;
    private final Map<String, Robot> robotsById = new HashMap<>();

    /**
     * Registers a robot for the battle.
     *
     * @param robotName The name of the robot
     * @return The registered robot with battle ID
     * @throws IllegalStateException if a battle is in progress
     */
    public Robot registerRobot(String robotName) {
        if (currentBattle != null && currentBattle.getState() == Battle.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot join a battle in progress");
        }

        if (currentBattle == null) {
            currentBattle = new Battle();
        }

        Robot robot = new Robot(robotName, currentBattle.getId());
        currentBattle.addRobot(robot);
        robotsById.put(robot.getId(), robot);

        return robot;
    }

    /**
     * Gets the battle status.
     *
     * @param battleId The battle ID
     * @return The battle if found
     * @throws IllegalArgumentException if the battle ID is invalid
     */
    public Battle getBattleStatus(String battleId) {
        if (currentBattle == null || !currentBattle.getId().equals(battleId)) {
            throw new IllegalArgumentException("Invalid battle ID");
        }

        return currentBattle;
    }

    /**
     * Gets the battle status for a specific robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The battle if found
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     */
    public Battle getBattleStatusForRobot(String battleId, String robotId) {
        if (currentBattle == null || !currentBattle.getId().equals(battleId)) {
            throw new IllegalArgumentException("Invalid battle ID");
        }

        if (!robotsById.containsKey(robotId)) {
            throw new IllegalArgumentException("Invalid robot ID");
        }

        Robot robot = robotsById.get(robotId);
        if (!robot.getBattleId().equals(battleId)) {
            throw new IllegalArgumentException("Robot does not belong to this battle");
        }

        return currentBattle;
    }

    /**
     * Starts the battle.
     *
     * @param battleId The battle ID
     * @return The battle
     * @throws IllegalArgumentException if the battle ID is invalid
     * @throws IllegalStateException if the battle is not ready to start
     */
    public Battle startBattle(String battleId) {
        if (currentBattle == null || !currentBattle.getId().equals(battleId)) {
            throw new IllegalArgumentException("Invalid battle ID");
        }

        if (currentBattle.getState() != Battle.BattleState.READY) {
            throw new IllegalStateException("Battle is not ready to start");
        }

        currentBattle.startBattle();
        return currentBattle;
    }

    /**
     * Checks if a battle ID is valid.
     *
     * @param battleId The battle ID to check
     * @return true if the battle ID is valid, false otherwise
     */
    public boolean isValidBattleId(String battleId) {
        return currentBattle != null && currentBattle.getId().equals(battleId);
    }

    /**
     * Checks if a robot ID is valid.
     *
     * @param robotId The robot ID to check
     * @return true if the robot ID is valid, false otherwise
     */
    public boolean isValidRobotId(String robotId) {
        return robotsById.containsKey(robotId);
    }

    /**
     * Checks if a battle ID and robot ID combination is valid.
     *
     * @param battleId The battle ID to check
     * @param robotId The robot ID to check
     * @return true if both IDs are valid and match, false otherwise
     */
    public boolean isValidBattleAndRobotId(String battleId, String robotId) {
        if (!isValidBattleId(battleId) || !isValidRobotId(robotId)) {
            return false;
        }
        Robot robot = robotsById.get(robotId);
        return robot.getBattleId().equals(battleId);
    }

    /**
     * Gets the current battle.
     *
     * @return The current battle, or null if no battle exists
     */
    public Battle getCurrentBattle() {
        return currentBattle;
    }

    /**
     * Resets the current battle for testing purposes.
     */
    public void resetBattle() {
        currentBattle = null;
        robotsById.clear();
    }
}
