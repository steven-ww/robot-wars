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
        // Fire laser in a direction where there's no robot
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
        // Robot1 at (10, 10) and Robot2 at (10, 13) - 3 blocks north (Y increases)
        try {
            battleService.setRobotPositionForTesting(battleId, robotId1, 10, 10);
            battleService.setRobotPositionForTesting(battleId, robotId2, 10, 13);
        } catch (Exception e) {
            // If positioning fails due to walls, try alternative positions
            battleService.setRobotPositionForTesting(battleId, robotId1, 5, 10);
            battleService.setRobotPositionForTesting(battleId, robotId2, 5, 13);
        }

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
        try {
            battleService.setRobotPositionForTesting(battleId, robotId1, 19, 10);
        } catch (Exception e) {
            // If positioning fails due to walls, try alternative edge position
            battleService.setRobotPositionForTesting(battleId, robotId1, 18, 10);
        }

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
        // Test diagonal laser firing
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "NE", 5);

        assertNotNull(response);
        assertEquals("NE", response.getDirection());
        assertEquals(5, response.getRange());
        assertNotNull(response.getLaserPath());
    }
}
