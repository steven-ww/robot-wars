package za.co.sww.rwars.backend.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Wall;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for BattleService robot spawning logic.
 */
@QuarkusTest
class BattleServiceTest {

    @Inject
    private BattleService battleService;

    @Inject
    private WallService wallService;

    private String battleId;
    private static int testCounter = 0;

    @BeforeEach
    void setUp() {
        // Create a new battle for each test with unique name
        testCounter++;
        Battle battle = battleService.createBattle("TestBattle" + testCounter, 20, 20);
        battleId = battle.getId();
    }

    @Test
    void testRobotNeverSpawnsOnWalls() {
        // Get the battle and add walls to create obstacles
        Battle battle = battleService.getBattleStatus(battleId);

        // Add various types of walls to the battle
        addTestWalls(battle);

        // Collect all wall positions for verification
        Set<String> wallPositions = collectWallPositions(battle);

        // Spawn multiple robots to test the spawning logic
        int numberOfRobots = 50;
        for (int i = 0; i < numberOfRobots; i++) {
            Robot robot = battleService.registerRobotForBattle("TestRobot" + i, battleId);

            assertNotNull(robot, "Robot should be created successfully");

            // Verify robot position is not on any wall
            String robotPosition = robot.getPositionX() + "," + robot.getPositionY();
            assertFalse(wallPositions.contains(robotPosition),
                String.format("Robot %s spawned at position (%d, %d) which is occupied by a wall",
                    robot.getName(), robot.getPositionX(), robot.getPositionY()));

            // Verify robot position is within arena boundaries
            assertTrue(robot.getPositionX() >= 0 && robot.getPositionX() < battle.getArenaWidth(),
                "Robot X position should be within arena boundaries");
            assertTrue(robot.getPositionY() >= 0 && robot.getPositionY() < battle.getArenaHeight(),
                "Robot Y position should be within arena boundaries");
        }
    }

    @Test
    void testRobotSpawningWithDenseWallCoverage() {
        // Create a battle with very dense wall coverage to stress test the spawning logic
        Battle battle = battleService.getBattleStatus(battleId);

        // Add walls covering most of the arena (but not all)
        addDenseWallCoverage(battle);

        // Collect all wall positions
        Set<String> wallPositions = collectWallPositions(battle);

        // Ensure there are still some free positions
        int totalPositions = battle.getArenaWidth() * battle.getArenaHeight();
        assertTrue(wallPositions.size() < totalPositions,
            "There should be at least some free positions for robot spawning");

        // Try to spawn robots even with dense wall coverage
        int numberOfRobots = 10;
        for (int i = 0; i < numberOfRobots; i++) {
            Robot robot = battleService.registerRobotForBattle("DenseTestRobot" + i, battleId);

            assertNotNull(robot, "Robot should be created successfully even with dense wall coverage");

            // Verify robot position is not on any wall
            String robotPosition = robot.getPositionX() + "," + robot.getPositionY();
            assertFalse(wallPositions.contains(robotPosition),
                String.format("Robot %s spawned at position (%d, %d) which is occupied by a wall",
                    robot.getName(), robot.getPositionX(), robot.getPositionY()));
        }
    }

    @Test
    void testRobotSpawningWithAllWallTypes() {
        // Test spawning with all different wall types
        Battle battle = battleService.getBattleStatus(battleId);

        // Add one of each wall type
        addSquareWall(battle, 5, 5);
        addLongWall(battle, 10, 10, true); // horizontal
        addLongWall(battle, 15, 2, false); // vertical
        addUShapeWall(battle, 2, 15);

        // Collect all wall positions
        Set<String> wallPositions = collectWallPositions(battle);

        // Spawn robots and verify they don't spawn on any wall type
        int numberOfRobots = 20;
        for (int i = 0; i < numberOfRobots; i++) {
            Robot robot = battleService.registerRobotForBattle("WallTypeTestRobot" + i, battleId);

            assertNotNull(robot, "Robot should be created successfully");

            // Verify robot position is not on any wall
            String robotPosition = robot.getPositionX() + "," + robot.getPositionY();
            assertFalse(wallPositions.contains(robotPosition),
                String.format("Robot %s spawned at position (%d, %d) which is occupied by a wall of some type",
                    robot.getName(), robot.getPositionX(), robot.getPositionY()));
        }
    }

    /**
     * Helper method to add various test walls to the battle.
     */
    private void addTestWalls(Battle battle) {
        // Add a square wall
        addSquareWall(battle, 3, 3);

        // Add a long horizontal wall
        addLongWall(battle, 8, 8, true);

        // Add a long vertical wall
        addLongWall(battle, 12, 5, false);

        // Add a U-shape wall
        addUShapeWall(battle, 15, 15);
    }

    /**
     * Helper method to add dense wall coverage for stress testing.
     */
    private void addDenseWallCoverage(Battle battle) {
        // Add multiple walls to cover about 60% of the arena
        for (int x = 0; x < battle.getArenaWidth(); x += 6) {
            for (int y = 0; y < battle.getArenaHeight(); y += 6) {
                if (x + 4 <= battle.getArenaWidth() && y + 4 <= battle.getArenaHeight()) {
                    addSquareWall(battle, x, y);
                }
            }
        }
    }

    /**
     * Helper method to add a square wall at the specified position.
     */
    private void addSquareWall(Battle battle, int startX, int startY) {
        Wall wall = new Wall(Wall.WallType.SQUARE);
        for (int x = startX; x < startX + 4 && x < battle.getArenaWidth(); x++) {
            for (int y = startY; y < startY + 4 && y < battle.getArenaHeight(); y++) {
                wall.addPosition(x, y);
            }
        }
        battle.getWalls().add(wall);
    }

    /**
     * Helper method to add a long wall at the specified position.
     */
    private void addLongWall(Battle battle, int startX, int startY, boolean horizontal) {
        Wall wall = new Wall(Wall.WallType.LONG);
        if (horizontal) {
            for (int x = startX; x < startX + 10 && x < battle.getArenaWidth(); x++) {
                if (startY < battle.getArenaHeight()) {
                    wall.addPosition(x, startY);
                }
            }
        } else {
            for (int y = startY; y < startY + 10 && y < battle.getArenaHeight(); y++) {
                if (startX < battle.getArenaWidth()) {
                    wall.addPosition(startX, y);
                }
            }
        }
        battle.getWalls().add(wall);
    }

    /**
     * Helper method to add a U-shape wall at the specified position.
     */
    private void addUShapeWall(Battle battle, int startX, int startY) {
        Wall wall = new Wall(Wall.WallType.U_SHAPE);

        // Left vertical part of U
        for (int y = startY; y < startY + 10 && y < battle.getArenaHeight(); y++) {
            if (startX < battle.getArenaWidth()) {
                wall.addPosition(startX, y);
            }
        }

        // Bottom horizontal part of U
        for (int x = startX; x < startX + 4 && x < battle.getArenaWidth(); x++) {
            if (startY + 9 < battle.getArenaHeight()) {
                wall.addPosition(x, startY + 9);
            }
        }

        // Right vertical part of U
        for (int y = startY; y < startY + 10 && y < battle.getArenaHeight(); y++) {
            if (startX + 3 < battle.getArenaWidth()) {
                wall.addPosition(startX + 3, y);
            }
        }

        battle.getWalls().add(wall);
    }

    /**
     * Helper method to collect all wall positions for verification.
     */
    private Set<String> collectWallPositions(Battle battle) {
        Set<String> wallPositions = new HashSet<>();
        for (Wall wall : battle.getWalls()) {
            for (Wall.Position position : wall.getPositions()) {
                wallPositions.add(position.getX() + "," + position.getY());
            }
        }
        return wallPositions;
    }

    /**
     * Creates a deterministic battle with no walls for movement testing.
     * This ensures consistent test results by eliminating randomness.
     */
    private String createDeterministicBattle(String battleName) {
        // Create a battle with "Empty" in the name to avoid random wall generation
        Battle battle = battleService.createBattle(battleName + "Empty", 30, 30);

        // Add deterministic walls in known positions to test wall avoidance
        addDeterministicWalls(battle);

        return battle.getId();
    }

    /**
     * Adds deterministic walls to the battle in known positions.
     * This creates a predictable layout for testing.
     */
    private void addDeterministicWalls(Battle battle) {
        // Add walls in corners and edges, leaving the center area (5,5) to (25,25) mostly clear
        // Top-left corner wall
        addSquareWall(battle, 0, 0);

        // Top-right corner wall
        addSquareWall(battle, 25, 0);

        // Bottom-left corner wall
        addSquareWall(battle, 0, 25);

        // Bottom-right corner wall
        addSquareWall(battle, 25, 25);

        // Add some predictable walls that don't interfere with test positions
        addLongWall(battle, 20, 15, true); // horizontal wall at (20,15)
        addLongWall(battle, 5, 20, false); // vertical wall at (5,20)
    }

    @Test
    void testRobotMovementNorth() throws InterruptedException {
        // Create battle with deterministic setup
        String deterministicBattleId = createDeterministicBattle("NorthMovementTest");

        Robot robot = battleService.registerRobotForBattle("NorthTestRobot", deterministicBattleId);
        battleService.registerRobotForBattle("DummyRobot", deterministicBattleId); // Need 2 robots to start battle

        // Set deterministic position in known wall-free area
        int testX = 10;
        int testY = 10;
        battleService.setRobotPositionForTesting(deterministicBattleId, robot.getId(), testX, testY);

        battleService.startBattle(deterministicBattleId);

        int initialX = robot.getPositionX();
        int initialY = robot.getPositionY();

        // Verify the position is set correctly
        assertEquals(testX, initialX, "Robot should be positioned at X=" + testX);
        assertEquals(testY, initialY, "Robot should be positioned at Y=" + testY);

        battleService.moveRobot(deterministicBattleId, robot.getId(), "NORTH", 1);

        // Wait for robot to complete movement
        Thread.sleep(1500);

        assertTrue(robot.getPositionY() > initialY, "Y position should increase when moving NORTH");
        assertEquals(initialX, robot.getPositionX(), "X position should remain constant when moving NORTH");
        assertEquals(testY + 1, robot.getPositionY(), "Robot should be at Y=" + (testY + 1) + " after moving NORTH");
    }

    @Test
    void testRobotMovementSouth() throws InterruptedException {
        // Create battle with deterministic setup
        String deterministicBattleId = createDeterministicBattle("SouthMovementTest");

        Robot robot = battleService.registerRobotForBattle("SouthTestRobot", deterministicBattleId);
        battleService.registerRobotForBattle("DummyRobot", deterministicBattleId); // Need 2 robots to start battle

        // Set deterministic position in known wall-free area
        int testX = 10;
        int testY = 15; // Start at a higher Y to move south
        battleService.setRobotPositionForTesting(deterministicBattleId, robot.getId(), testX, testY);

        battleService.startBattle(deterministicBattleId);

        int initialX = robot.getPositionX();
        int initialY = robot.getPositionY();

        // Verify the position is set correctly
        assertEquals(testX, initialX, "Robot should be positioned at X=" + testX);
        assertEquals(testY, initialY, "Robot should be positioned at Y=" + testY);

        battleService.moveRobot(deterministicBattleId, robot.getId(), "SOUTH", 1);

        // Wait for robot to complete movement
        Thread.sleep(1500);

        assertTrue(robot.getPositionY() < initialY, "Y position should decrease when moving SOUTH");
        assertEquals(initialX, robot.getPositionX(), "X position should remain constant when moving SOUTH");
        assertEquals(testY - 1, robot.getPositionY(), "Robot should be at Y=" + (testY - 1) + " after moving SOUTH");
    }

    @Test
    void testRobotMovementEast() throws InterruptedException {
        // Create battle with deterministic setup
        String deterministicBattleId = createDeterministicBattle("EastMovementTest");

        Robot robot = battleService.registerRobotForBattle("EastTestRobot", deterministicBattleId);
        battleService.registerRobotForBattle("DummyRobot", deterministicBattleId); // Need 2 robots to start battle

        // Set deterministic position in known wall-free area
        int testX = 10;
        int testY = 10;
        battleService.setRobotPositionForTesting(deterministicBattleId, robot.getId(), testX, testY);

        battleService.startBattle(deterministicBattleId);

        int initialX = robot.getPositionX();
        int initialY = robot.getPositionY();

        // Verify the position is set correctly
        assertEquals(testX, initialX, "Robot should be positioned at X=" + testX);
        assertEquals(testY, initialY, "Robot should be positioned at Y=" + testY);

        battleService.moveRobot(deterministicBattleId, robot.getId(), "EAST", 1);

        // Wait for robot to complete movement
        Thread.sleep(1500);

        assertTrue(robot.getPositionX() > initialX, "X position should increase when moving EAST");
        assertEquals(initialY, robot.getPositionY(), "Y position should remain constant when moving EAST");
        assertEquals(testX + 1, robot.getPositionX(), "Robot should be at X=" + (testX + 1) + " after moving EAST");
    }

    @Test
    void testRobotMovementWest() throws InterruptedException {
        // Create battle with deterministic setup
        String deterministicBattleId = createDeterministicBattle("WestMovementTest");

        Robot robot = battleService.registerRobotForBattle("WestTestRobot", deterministicBattleId);
        battleService.registerRobotForBattle("DummyRobot", deterministicBattleId); // Need 2 robots to start battle

        // Set deterministic position in known wall-free area
        int testX = 15;
        int testY = 10; // Start at a higher X to move west
        battleService.setRobotPositionForTesting(deterministicBattleId, robot.getId(), testX, testY);

        battleService.startBattle(deterministicBattleId);

        int initialX = robot.getPositionX();
        int initialY = robot.getPositionY();

        // Verify the position is set correctly
        assertEquals(testX, initialX, "Robot should be positioned at X=" + testX);
        assertEquals(testY, initialY, "Robot should be positioned at Y=" + testY);

        battleService.moveRobot(deterministicBattleId, robot.getId(), "WEST", 1);

        // Wait for robot to complete movement
        Thread.sleep(1500);

        assertTrue(robot.getPositionX() < initialX, "X position should decrease when moving WEST");
        assertEquals(initialY, robot.getPositionY(), "Y position should remain constant when moving WEST");
        assertEquals(testX - 1, robot.getPositionX(), "Robot should be at X=" + (testX - 1) + " after moving WEST");
    }
}
