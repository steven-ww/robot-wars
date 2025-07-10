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
    BattleService battleService;

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
        // Get robot positions
        Robot robot1 = battleService.getRobotDetails(battleId, robotId1);
        Robot robot2 = battleService.getRobotDetails(battleId, robotId2);

        // Position robot2 directly north of robot1 for a guaranteed hit
        robot2.setPositionX(robot1.getPositionX());
        robot2.setPositionY(robot1.getPositionY() - 3); // 3 blocks north

        // Fire laser north
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
        // Position robot near edge
        Robot robot = battleService.getRobotDetails(battleId, robotId1);
        robot.setPositionX(19); // Near right edge of 20x20 arena
        robot.setPositionY(10);

        // Fire laser east (should hit boundary)
        LaserResponse response = battleService.fireLaser(battleId, robotId1, "EAST", 5);

        assertNotNull(response);
        assertFalse(response.isHit());
        assertEquals("BOUNDARY", response.getBlockedBy());
        assertEquals("EAST", response.getDirection());
    }

    @Test
    void testLaserFireDefaultRange() {
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
