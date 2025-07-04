package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

    @Inject
    @ConfigProperty(name = "battle.arena.default-width", defaultValue = "50")
    int defaultArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.default-height", defaultValue = "50")
    int defaultArenaHeight;

    @Inject
    @ConfigProperty(name = "battle.arena.min-width", defaultValue = "10")
    int minArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.min-height", defaultValue = "10")
    int minArenaHeight;

    @Inject
    @ConfigProperty(name = "battle.arena.max-width", defaultValue = "1000")
    int maxArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.max-height", defaultValue = "1000")
    int maxArenaHeight;

    /**
     * Creates a new battle with the given name and default arena dimensions.
     *
     * @param battleName The name of the battle
     * @return The created battle
     */
    public Battle createBattle(String battleName) {
        return createBattle(battleName, defaultArenaWidth, defaultArenaHeight);
    }

    /**
     * Creates a new battle with the given name and arena dimensions.
     *
     * @param battleName The name of the battle
     * @param width The width of the arena
     * @param height The height of the arena
     * @return The created battle
     * @throws IllegalArgumentException if the arena dimensions are invalid
     */
    public Battle createBattle(String battleName, int width, int height) {
        if (width < minArenaWidth || height < minArenaHeight) {
            throw new IllegalArgumentException(
                    String.format("Arena dimensions must be at least %dx%d", minArenaWidth, minArenaHeight));
        }

        if (width > maxArenaWidth || height > maxArenaHeight) {
            throw new IllegalArgumentException(
                    String.format("Arena dimensions must be at most %dx%d", maxArenaWidth, maxArenaHeight));
        }

        if (currentBattle != null && currentBattle.getState() == Battle.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot create a new battle while another is in progress");
        }

        currentBattle = new Battle(battleName, width, height);
        return currentBattle;
    }

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
            // Create a default battle with a generated name and default dimensions
            currentBattle = new Battle("Default Battle", defaultArenaWidth, defaultArenaHeight);
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
