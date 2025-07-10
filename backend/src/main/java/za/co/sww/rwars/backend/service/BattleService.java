package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Robot.Direction;
import za.co.sww.rwars.backend.model.Robot.RobotStatus;
import za.co.sww.rwars.backend.model.Wall;
import za.co.sww.rwars.backend.model.RadarResponse;
import za.co.sww.rwars.backend.websocket.BattleStateSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service to manage battles and robots.
 */
@ApplicationScoped
public class BattleService {

    private final Map<String, Battle> battlesById = new HashMap<>();
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

    @Inject
    private BattleStateSocket battleStateSocket;

    @Inject
    private WallService wallService;

    @Inject
    private RadarService radarService;

    @ConfigProperty(name = "battle.robot.default-hit-points", defaultValue = "100")
    private int defaultHitPoints;

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

        // Check if battle name already exists
        for (Battle existingBattle : battlesById.values()) {
            if (battleName.equals(existingBattle.getName())) {
                throw new IllegalStateException("Battle with name '" + battleName + "' already exists");
            }
        }

        Battle newBattle = new Battle(battleName, width, height, movementTimeSeconds);

        // Generate random walls for the battle
        List<Wall> walls = wallService.generateWalls(newBattle);
        newBattle.setWalls(walls);

        battlesById.put(newBattle.getId(), newBattle);
        return newBattle;
    }

    /**
     * Registers a robot for the battle.
     * This method creates a default battle if none exists (for backward compatibility).
     *
     * @param robotName The name of the robot
     * @return The registered robot with battle ID
     * @throws IllegalStateException if a battle is in progress
     */
    public Robot registerRobot(String robotName) {
        // Find the first available battle (not in progress)
        Battle availableBattle = battlesById.values().stream()
                .filter(battle -> battle.getState() != Battle.BattleState.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        if (availableBattle == null) {
            // If no battles exist at all, create a default battle
            if (battlesById.isEmpty()) {
                availableBattle = new Battle("Default Battle", defaultArenaWidth, defaultArenaHeight);
                // Generate random walls for the battle
                List<Wall> walls = wallService.generateWalls(availableBattle);
                availableBattle.setWalls(walls);
                battlesById.put(availableBattle.getId(), availableBattle);
            } else {
                // All existing battles are in progress, can't join any
                throw new IllegalStateException("Cannot join a battle in progress");
            }
        }

        return registerRobotForBattle(robotName, availableBattle.getId());
    }

    /**
     * Registers a robot for a specific battle.
     *
     * @param robotName The name of the robot
     * @param battleId The ID of the battle to join
     * @return The registered robot with battle ID
     * @throws IllegalArgumentException if the battle ID is invalid
     * @throws IllegalStateException if the battle is in progress
     */
    public Robot registerRobotForBattle(String robotName, String battleId) {
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Invalid battle ID: " + battleId);
        }

        if (battle.getState() == Battle.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot join a battle in progress");
        }

        Robot robot = new Robot(robotName, battleId);
        robot.setHitPoints(defaultHitPoints);
        robot.setMaxHitPoints(defaultHitPoints);

        // Randomly position the robot within the arena boundaries, avoiding walls
        int arenaWidth = battle.getArenaWidth();
        int arenaHeight = battle.getArenaHeight();

        // Generate random position within arena boundaries that doesn't overlap with walls
        int randomX;
        int randomY;
        int attempts = 0;
        do {
            randomX = (int) (Math.random() * arenaWidth);
            randomY = (int) (Math.random() * arenaHeight);
            attempts++;
        } while (battle.isPositionOccupiedByWall(randomX, randomY) && attempts < 100);

        robot.setPositionX(randomX);
        robot.setPositionY(randomY);

        battle.addRobot(robot);
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
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Invalid battle ID: " + battleId);
        }
        return battle;
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
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Invalid battle ID: " + battleId);
        }

        if (!robotsById.containsKey(robotId)) {
            throw new IllegalArgumentException("Invalid robot ID: " + robotId);
        }

        Robot robot = robotsById.get(robotId);
        if (!robot.getBattleId().equals(battleId)) {
            throw new IllegalArgumentException("Robot does not belong to this battle");
        }

        return battle;
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
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Invalid battle ID: " + battleId);
        }

        if (!robotsById.containsKey(robotId)) {
            throw new IllegalArgumentException("Invalid robot ID: " + robotId);
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
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Invalid battle ID: " + battleId);
        }

        if (battle.getState() != Battle.BattleState.READY) {
            throw new IllegalStateException("Battle is not ready to start");
        }

        battle.startBattle();
        return battle;
    }

    /**
     * Checks if a battle ID is valid.
     *
     * @param battleId The battle ID to check
     * @return true if the battle ID is valid, false otherwise
     */
    public boolean isValidBattleId(String battleId) {
        return battlesById.containsKey(battleId);
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
     * Gets all battles.
     *
     * @return A list of all battles
     */
    public List<Battle> getAllBattles() {
        return new ArrayList<>(battlesById.values());
    }

    /**
     * Gets all battles with basic information (without robot positions).
     * This is useful for the frontend to display battle lists.
     *
     * @return A list of battle summaries
     */
    public List<BattleSummary> getAllBattleSummaries() {
        return battlesById.values().stream()
                .map(battle -> new BattleSummary(
                    battle.getId(),
                    battle.getName(),
                    battle.getArenaWidth(),
                    battle.getArenaHeight(),
                    battle.getRobotMovementTimeSeconds(),
                    battle.getState().toString(),
                    battle.getRobotCount(),
                    battle.getRobots().stream()
                            .map(robot -> new RobotSummary(
                                robot.getId(),
                                robot.getName(),
                                robot.getStatus().toString()
                            ))
                            .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Gets the current battle (first available battle for backward compatibility).
     *
     * @return The first available battle, or null if no battle exists
     */
    public Battle getCurrentBattle() {
        return battlesById.values().stream().findFirst().orElse(null);
    }

    /**
     * Resets all battles for testing purposes.
     */
    public void resetBattle() {
        battlesById.clear();
        robotsById.clear();
    }

    /**
     * Battle summary record for listing battles without sensitive robot position data.
     */
    public record BattleSummary(
            String id,
            String name,
            int arenaWidth,
            int arenaHeight,
            double robotMovementTimeSeconds,
            String state,
            int robotCount,
            List<RobotSummary> robots
    ) {
    }

    /**
     * Robot summary record for listing robots without position data.
     */
    public record RobotSummary(
            String id,
            String name,
            String status
    ) {
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

        Battle battle = battlesById.get(battleId);
        if (battle.getState() != Battle.BattleState.IN_PROGRESS) {
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

        // Broadcast the state change to WebSocket clients
        broadcastBattleStateUpdate(battleId);

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
                    // Broadcast the status change to IDLE
                    broadcastBattleStateUpdate(robot.getBattleId());
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

        // Get the battle this robot belongs to
        Battle battle = battlesById.get(robot.getBattleId());
        if (battle == null) {
            // This shouldn't happen, but handle it gracefully
            robot.setStatus(RobotStatus.CRASHED);
            return;
        }

        // Check if the new position is within the arena boundaries
        if (newX < 0 || newX >= battle.getArenaWidth()
            || newY < 0 || newY >= battle.getArenaHeight()) {
            // Robot has crashed into the arena boundary
            robot.setHitPoints(0);
            robot.setStatus(RobotStatus.CRASHED);
            // Broadcast the crash state change
            broadcastBattleStateUpdate(robot.getBattleId());
            // Check if battle should end
            checkBattleCompletion(battle);
            return;
        }

        // Check if the new position collides with a wall
        if (battle.isPositionOccupiedByWall(newX, newY)) {
            // Robot has crashed into a wall
            robot.setHitPoints(0);
            robot.setStatus(RobotStatus.CRASHED);
            // Broadcast the crash state change
            broadcastBattleStateUpdate(robot.getBattleId());
            // Check if battle should end
            checkBattleCompletion(battle);
            return;
        }

        // Update the robot's position
        robot.setPositionX(newX);
        robot.setPositionY(newY);

        // Decrement the blocks remaining
        robot.setBlocksRemaining(robot.getBlocksRemaining() - 1);

        // Broadcast the position change
        broadcastBattleStateUpdate(robot.getBattleId());
    }

    /**
     * Checks if a battle should be completed based on active robot count.
     *
     * @param battle The battle to check
     */
    private void checkBattleCompletion(Battle battle) {
        if (battle.getState() == Battle.BattleState.IN_PROGRESS) {
            long activeRobots = battle.getActiveRobotCount();
            if (activeRobots <= 1) {
                Robot winner = battle.getActiveRobot();
                if (winner != null) {
                    battle.declareWinner(winner);
                } else {
                    battle.setState(Battle.BattleState.COMPLETED);
                }
                broadcastBattleStateUpdate(battle.getId());
            }
        }
    }

    /**
     * Performs a radar scan for a robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param range The scan range
     * @return The radar response
     * @throws IllegalArgumentException if the battle ID or robot ID is invalid
     * @throws IllegalStateException if the battle is not in progress
     */
    public RadarResponse performRadarScan(String battleId, String robotId, int range) {
        if (!isValidBattleAndRobotId(battleId, robotId)) {
            throw new IllegalArgumentException("Invalid battle ID or robot ID");
        }

        Battle battle = battlesById.get(battleId);
        if (battle.getState() != Battle.BattleState.IN_PROGRESS) {
            throw new IllegalStateException("Battle is not in progress");
        }

        Robot robot = robotsById.get(robotId);
        if (!robot.isActive()) {
            throw new IllegalStateException("Robot is not active");
        }

        return radarService.scanArea(battle, robot, range);
    }

    /**
     * Deletes a completed battle and all associated data.
     *
     * @param battleId The battle ID to delete
     * @throws IllegalArgumentException if the battle ID is invalid
     * @throws IllegalStateException if the battle is not completed
     */
    public void deleteBattle(String battleId) {
        Battle battle = battlesById.get(battleId);
        if (battle == null) {
            throw new IllegalArgumentException("Battle not found");
        }

        if (battle.getState() != Battle.BattleState.COMPLETED) {
            throw new IllegalStateException("Cannot delete battle that is not completed");
        }

        // Remove all robots associated with this battle
        List<String> robotIdsToRemove = new ArrayList<>();
        for (Map.Entry<String, Robot> entry : robotsById.entrySet()) {
            if (entry.getValue().getBattleId().equals(battleId)) {
                robotIdsToRemove.add(entry.getKey());
            }
        }

        // Remove the robots from the robotsById map
        for (String robotId : robotIdsToRemove) {
            robotsById.remove(robotId);
        }

        // Remove the battle itself
        battlesById.remove(battleId);
    }

    /**
     * Broadcasts battle state updates to all connected WebSocket clients.
     * This method is called whenever robot state changes to ensure real-time updates.
     *
     * @param battleId The battle ID to broadcast updates for
     */
    private void broadcastBattleStateUpdate(String battleId) {
        if (battleStateSocket != null && battleId != null) {
            try {
                battleStateSocket.broadcastBattleState(battleId);
            } catch (Exception e) {
                // Log the error but don't fail the operation
                System.err.println("Error broadcasting battle state update for battle " + battleId + ": "
                        + e.getMessage());
            }
        }
    }
}
