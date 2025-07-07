package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Robot.Direction;
import za.co.sww.rwars.backend.model.Robot.RobotStatus;

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
    private int defaultArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.default-height", defaultValue = "50")
    private int defaultArenaHeight;

    @Inject
    @ConfigProperty(name = "battle.arena.min-width", defaultValue = "10")
    private int minArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.min-height", defaultValue = "10")
    private int minArenaHeight;

    @Inject
    @ConfigProperty(name = "battle.arena.max-width", defaultValue = "1000")
    private int maxArenaWidth;

    @Inject
    @ConfigProperty(name = "battle.arena.max-height", defaultValue = "1000")
    private int maxArenaHeight;

    @Inject
    @ConfigProperty(name = "battle.robot.movement-time-seconds", defaultValue = "1")
    private double robotMovementTimeSeconds;

    /**
     * Gets the default arena width.
     *
     * @return The default arena width
     */
    public int getDefaultArenaWidth() {
        return defaultArenaWidth;
    }

    /**
     * Gets the default arena height.
     *
     * @return The default arena height
     */
    public int getDefaultArenaHeight() {
        return defaultArenaHeight;
    }

    /**
     * Gets the minimum arena width.
     *
     * @return The minimum arena width
     */
    public int getMinArenaWidth() {
        return minArenaWidth;
    }

    /**
     * Gets the minimum arena height.
     *
     * @return The minimum arena height
     */
    public int getMinArenaHeight() {
        return minArenaHeight;
    }

    /**
     * Gets the maximum arena width.
     *
     * @return The maximum arena width
     */
    public int getMaxArenaWidth() {
        return maxArenaWidth;
    }

    /**
     * Gets the maximum arena height.
     *
     * @return The maximum arena height
     */
    public int getMaxArenaHeight() {
        return maxArenaHeight;
    }

    /**
     * Gets the robot movement time in seconds per block.
     *
     * @return The robot movement time in seconds per block
     */
    public double getRobotMovementTimeSeconds() {
        return robotMovementTimeSeconds;
    }

    /**
     * Creates a new battle with the given name and default arena dimensions.
     *
     * @param battleName The name of the battle
     * @return The created battle
     */
    public Battle createBattle(String battleName) {
        return createBattle(battleName, defaultArenaWidth, defaultArenaHeight, robotMovementTimeSeconds);
    }

    /**
     * Creates a new battle with the given name and custom robot movement time.
     *
     * @param battleName The name of the battle
     * @param movementTimeSeconds The time in seconds it takes for a robot to move one block
     * @return The created battle
     */
    public Battle createBattle(String battleName, double movementTimeSeconds) {
        return createBattle(battleName, defaultArenaWidth, defaultArenaHeight, movementTimeSeconds);
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
        return createBattle(battleName, width, height, robotMovementTimeSeconds);
    }

    /**
     * Creates a new battle with the given name, arena dimensions, and robot movement time.
     *
     * @param battleName The name of the battle
     * @param width The width of the arena
     * @param height The height of the arena
     * @param movementTimeSeconds The time in seconds it takes for a robot to move one block
     * @return The created battle
     * @throws IllegalArgumentException if the arena dimensions are invalid
     */
    public Battle createBattle(String battleName, int width, int height, double movementTimeSeconds) {
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

        currentBattle = new Battle(battleName, width, height, movementTimeSeconds);
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

        // Randomly position the robot within the arena boundaries
        int arenaWidth = currentBattle.getArenaWidth();
        int arenaHeight = currentBattle.getArenaHeight();

        // Generate random position within arena boundaries
        int randomX = (int) (Math.random() * arenaWidth);
        int randomY = (int) (Math.random() * arenaHeight);

        robot.setPositionX(randomX);
        robot.setPositionY(randomY);

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
     * Gets a specific robot's details.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The robot if found
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     */
    public Robot getRobotDetails(String battleId, String robotId) {
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

        return robot;
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

    /**
     * Moves a robot in the specified direction for the specified number of blocks.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param directionStr The direction to move
     * @param blocks The number of blocks to move
     * @return The robot with updated position
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     * @throws IllegalStateException if the battle is not in progress
     */
    public Robot moveRobot(String battleId, String robotId, String directionStr, int blocks) {
        return moveRobot(battleId, robotId, directionStr, blocks, robotMovementTimeSeconds);
    }

    /**
     * Moves a robot in the specified direction for the specified number of blocks with a custom movement time.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param directionStr The direction to move
     * @param blocks The number of blocks to move
     * @param movementTimeSeconds The time in seconds it takes to move one block
     * @return The robot with updated position
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     * @throws IllegalStateException if the battle is not in progress
     */
    public Robot moveRobot(String battleId, String robotId, String directionStr, int blocks,
                            double movementTimeSeconds) {
        if (!isValidBattleAndRobotId(battleId, robotId)) {
            throw new IllegalArgumentException("Invalid battle ID or robot ID");
        }

        if (currentBattle.getState() != Battle.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Battle is not in progress");
        }

        Robot robot = robotsById.get(robotId);

        // Parse the direction
        Direction direction;
        try {
            direction = Direction.valueOf(directionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid direction: " + directionStr);
        }

        // Set the robot's direction and status
        robot.setDirection(direction);
        robot.setStatus(RobotStatus.MOVING);
        robot.setTargetBlocks(blocks);
        robot.setBlocksRemaining(blocks);

        // Start the movement process
        startRobotMovement(robot, movementTimeSeconds);

        return robot;
    }

    /**
     * Updates the position of a robot (for testing purposes only).
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param positionX The X coordinate
     * @param positionY The Y coordinate
     * @return The robot with updated position
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     */
    public Robot updateRobotPosition(String battleId, String robotId, int positionX, int positionY) {
        if (!isValidBattleAndRobotId(battleId, robotId)) {
            throw new IllegalArgumentException("Invalid battle ID or robot ID");
        }

        Robot robot = robotsById.get(robotId);
        robot.setPositionX(positionX);
        robot.setPositionY(positionY);

        return robot;
    }

    /**
     * Starts the robot movement process.
     *
     * @param robot The robot to move
     */
    private void startRobotMovement(Robot robot) {
        startRobotMovement(robot, robotMovementTimeSeconds);
    }

    /**
     * Starts the robot movement process with a custom movement time.
     *
     * @param robot The robot to move
     * @param movementTimeSeconds The time in seconds it takes to move one block
     */
    private void startRobotMovement(Robot robot, double movementTimeSeconds) {
        // Convert movement time to milliseconds for more precise scheduling
        long movementTimeMillis = (long) (movementTimeSeconds * 1000);

        // Use a virtual thread to handle the robot movement
        Thread.startVirtualThread(() -> {
            try {
                // Continue moving until the robot reaches its target or crashes
                while (robot.getBlocksRemaining() > 0 && robot.getStatus() != RobotStatus.CRASHED) {
                    // Move the robot one block in the specified direction
                    moveRobotOneBlock(robot);

                    // Sleep for the specified movement time
                    Thread.sleep(movementTimeMillis);
                }

                // Update the robot's status if it's not crashed
                if (robot.getStatus() != RobotStatus.CRASHED) {
                    robot.setStatus(RobotStatus.IDLE);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Moves a robot one block in its current direction.
     *
     * @param robot The robot to move
     */
    private void moveRobotOneBlock(Robot robot) {
        if (robot.getBlocksRemaining() <= 0) {
            return;
        }

        int newX = robot.getPositionX();
        int newY = robot.getPositionY();

        // Calculate the new position based on the direction
        switch (robot.getDirection()) {
            case NORTH:
                newY++;
                break;
            case SOUTH:
                newY--;
                break;
            case EAST:
                newX++;
                break;
            case WEST:
                newX--;
                break;
            case NE:
                newX++;
                newY++;
                break;
            case NW:
                newX--;
                newY++;
                break;
            case SE:
                newX++;
                newY--;
                break;
            case SW:
                newX--;
                newY--;
                break;
            default:
                // No movement for unknown direction
                break;
        }

        // Check if the new position is within the arena boundaries
        if (newX < 0 || newX >= currentBattle.getArenaWidth()
            || newY < 0 || newY >= currentBattle.getArenaHeight()) {
            // Robot has crashed into the arena boundary
            robot.setStatus(RobotStatus.CRASHED);
            return;
        }

        // Update the robot's position
        robot.setPositionX(newX);
        robot.setPositionY(newY);

        // Decrement the blocks remaining
        robot.setBlocksRemaining(robot.getBlocksRemaining() - 1);
    }
}
