package za.co.sww.rwars.backend.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.LaserResponse;
import za.co.sww.rwars.backend.model.Robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for laser firing functionality.
 */
@QuarkusTest
class LaserServiceTest {

    @Inject
    private BattleService battleService;

    private String battleId;
    private String robotId1;
    private String robotId2;
    private static int testCounter = 0;

    @BeforeEach
    void setUp() {
        // Create a unique battle for each test
        testCounter++;
        Battle battle = battleService.createBattle("LaserTestBattle" + testCounter, 20, 20, 1.0);
        battleId = battle.getId();

        // Register two robots
        Robot robot1 = battleService.registerRobotForBattle("LaserBot", battleId);
        Robot robot2 = battleService.registerRobotForBattle("TargetBot", battleId);
        robotId1 = robot1.getId();
        robotId2 = robot2.getId();

        // Start the battle
        battleService.startBattle(battleId);
    }

    @Test
    void testLaserFireMiss() {
        // Position robot1 in a known location where laser will miss
        // Position it in center of arena where it has clear path
        int[][] positions = {{10, 10}, {5, 10}, {15, 10}, {8, 10}, {12, 10}};
        boolean positioned = false;
        for (int[] pos : positions) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId1, pos[0], pos[1]);
                positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next position
            }
        }
        assertTrue(positioned, "Failed to position robot in a wall-free location");

        // Position robot2 far away to ensure no hit
        int[][] robot2Positions = {{1, 1}, {2, 2}, {3, 3}, {1, 2}, {2, 1}};
        boolean robot2Positioned = false;
        for (int[] pos : robot2Positions) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId2, pos[0], pos[1]);
                robot2Positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next position
            }
        }
        assertTrue(robot2Positioned, "Failed to position robot2 in a wall-free location");

        // Fire laser in a direction where there's no robot (NORTH with limited range)
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "NORTH", 5);

        assertNotNull(response);
        assertFalse(response.isHit());
        assertEquals("NORTH", response.getDirection());
        assertEquals(5, response.getRange());
        assertNotNull(response.getLaserPath());
        assertTrue(response.getLaserPath().size() > 0);
    }

    @Test
    void testLaserFireHit() {
        // Position robots at known locations to ensure a guaranteed hit
        // Try multiple position pairs until we find one that works (avoiding walls)
        int[][][] positionPairs = {
            {{10, 10}, {10, 13}}, // Robot1 at (10, 10) and Robot2 at (10, 13) - 3 blocks north
            {{5, 10}, {5, 13}},   // Alternative: Robot1 at (5, 10) and Robot2 at (5, 13)
            {{15, 10}, {15, 13}}, // Alternative: Robot1 at (15, 10) and Robot2 at (15, 13)
            {{8, 8}, {8, 11}},    // Alternative: Robot1 at (8, 8) and Robot2 at (8, 11)
            {{12, 8}, {12, 11}}   // Alternative: Robot1 at (12, 8) and Robot2 at (12, 11)
        };

        boolean positioned = false;
        for (int[][] pair : positionPairs) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId1, pair[0][0], pair[0][1]);
                battleService.setRobotPositionForTesting(battleId, robotId2, pair[1][0], pair[1][1]);
                positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next position pair
            }
        }
        assertTrue(positioned, "Failed to position robots in wall-free locations for hit test");

        // Fire laser north from robot1 towards robot2
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "NORTH", 10);

        assertNotNull(response);
        assertTrue(response.isHit());
        assertEquals(robotId2, response.getHitRobotId());
        assertEquals("TargetBot", response.getHitRobotName());
        assertEquals(20, response.getDamageDealt()); // Default damage
        assertEquals("NORTH", response.getDirection());
        assertEquals(10, response.getRange());
        assertNotNull(response.getHitPosition());
        assertEquals("ROBOT", response.getBlockedBy());

        // Verify target robot took damage
        Robot hitRobot = battleService.getRobotDetails(battleId, robotId2);
        assertEquals(80, hitRobot.getHitPoints()); // 100 - 20 = 80
    }

    @Test
    void testLaserFireInvalidDirection() {
        try {
            battleService.fireLaser(battleId, robotId1, "INVALID", 5);
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid direction"));
        }
    }

    @Test
    void testLaserFireInactiveRobot() {
        // Crash the robot
        Robot robot = battleService.getRobotDetails(battleId, robotId1);
        robot.setStatus(Robot.RobotStatus.CRASHED);

        try {
            battleService.fireLaser(battleId, robotId1, "NORTH", 5);
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Robot is not active"));
        }
    }

    @Test
    void testLaserFireBoundaryBlock() {
        // Position robot near edge using proper testing method
        // Try multiple edge positions until one works (avoiding walls)
        int[][] edgePositions = {{19, 10}, {18, 10}, {17, 10}, {19, 8}, {18, 8}};
        boolean positioned = false;
        for (int[] pos : edgePositions) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId1, pos[0], pos[1]);
                positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next edge position
            }
        }
        assertTrue(positioned, "Failed to position robot near edge in wall-free location");

        // Fire laser east (should hit boundary)
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "EAST", 5);

        assertNotNull(response);
        assertFalse(response.isHit());
        assertEquals("BOUNDARY", response.getBlockedBy());
        assertEquals("EAST", response.getDirection());
    }

    @Test
    void testLaserFireDefaultRange() {
        // Position robot in center of arena to ensure it won't hit boundaries
        // Try multiple positions until one works (avoiding walls)
        int[][] positions = {{10, 15}, {5, 15}, {15, 15}, {8, 15}, {12, 15}};
        boolean positioned = false;
        for (int[] pos : positions) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId1, pos[0], pos[1]);
                positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next position
            }
        }
        assertTrue(positioned, "Failed to position robot in a wall-free location");
        // Fire laser without specifying range (should use default)
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "SOUTH", 0);

        assertNotNull(response);
        assertEquals(10, response.getRange()); // Default range
    }

    @Test
    void testLaserFireDiagonalDirection() {
        // Position robot1 in a known location for diagonal laser test
        int[][] positions = {{10, 10}, {5, 10}, {15, 10}, {8, 10}, {12, 10}};
        boolean positioned = false;
        for (int[] pos : positions) {
            try {
                battleService.setRobotPositionForTesting(battleId, robotId1, pos[0], pos[1]);
                positioned = true;
                break;
            } catch (IllegalArgumentException e) {
                // Try next position
            }
        }
        assertTrue(positioned, "Failed to position robot in a wall-free location");

        // Test diagonal laser firing
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "NE", 5);

        assertNotNull(response);
        assertEquals("NE", response.getDirection());
        assertEquals(5, response.getRange());
        assertNotNull(response.getLaserPath());
    }
}
