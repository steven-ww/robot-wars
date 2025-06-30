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
    private final Map<String, Robot> robotsByBattleId = new HashMap<>();

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
        robotsByBattleId.put(robot.getBattleId(), robot);

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
        robotsByBattleId.clear();
    }
}
