package za.co.sww.rwars.backend.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.RadarResponse;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Wall;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for RadarService.
 */
@QuarkusTest
class RadarServiceTest {

    @Inject
    private RadarService radarService;

    private Battle battle;
    private Robot scanningRobot;

    @BeforeEach
    void setUp() {
        // Create a 10x10 arena for testing
        battle = new Battle();
        battle.setArenaWidth(10);
        battle.setArenaHeight(10);
        battle.setWalls(new ArrayList<>());
        battle.setRobots(new ArrayList<>());

        // Create a robot at position (5, 5) for scanning
        scanningRobot = new Robot();
        scanningRobot.setName("Scanner");
        scanningRobot.setPositionX(5);
        scanningRobot.setPositionY(5);
        battle.getRobots().add(scanningRobot);
    }

    @Test
    void testArenaEdgeDetectionAccuracy() {
        // Test robot at center can detect all arena edges within range
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 5);

        // Should detect arena boundaries at edges
        List<RadarResponse.Detection> wallDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.WALL)
            .filter(d -> d.getDetails().contains("Arena boundary"))
            .toList();

        // Verify we detect boundary walls
        assertTrue(wallDetections.size() > 0, "Should detect arena boundary walls");

        // Verify specific boundary positions are detected correctly
        boolean foundLeftBoundary = wallDetections.stream()
            .anyMatch(d -> d.getX() == -5 && d.getY() >= -5 && d.getY() <= 4);
        boolean foundRightBoundary = wallDetections.stream()
            .anyMatch(d -> d.getX() == 4 && d.getY() >= -5 && d.getY() <= 4);
        boolean foundTopBoundary = wallDetections.stream()
            .anyMatch(d -> d.getY() == -5 && d.getX() >= -5 && d.getX() <= 4);
        boolean foundBottomBoundary = wallDetections.stream()
            .anyMatch(d -> d.getY() == 4 && d.getX() >= -5 && d.getX() <= 4);

        assertTrue(foundLeftBoundary, "Should detect left boundary wall");
        assertTrue(foundRightBoundary, "Should detect right boundary wall");
        assertTrue(foundTopBoundary, "Should detect top boundary wall");
        assertTrue(foundBottomBoundary, "Should detect bottom boundary wall");
    }

    @Test
    void testInnerWallDetectionAccuracy() {
        // Add a square wall at position (3,3) to (6,6)
        Wall squareWall = new Wall(Wall.WallType.SQUARE);
        for (int x = 3; x <= 6; x++) {
            for (int y = 3; y <= 6; y++) {
                squareWall.addPosition(x, y);
            }
        }
        battle.getWalls().add(squareWall);

        // Scan from robot at (5,5) - should detect wall positions
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 5);

        List<RadarResponse.Detection> wallDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.WALL)
            .filter(d -> d.getDetails().contains("Wall of type SQUARE"))
            .toList();

        // Should detect multiple wall positions
        assertTrue(wallDetections.size() > 0, "Should detect inner wall positions");

        // Verify specific wall positions are detected with correct relative coordinates
        boolean foundWallAtRelativeNeg2Neg2 = wallDetections.stream()
            .anyMatch(d -> d.getX() == -2 && d.getY() == -2);
        boolean foundWallAtRelative1Pos1 = wallDetections.stream()
            .anyMatch(d -> d.getX() == 1 && d.getY() == 1);

        assertTrue(foundWallAtRelativeNeg2Neg2, "Should detect wall at relative position (-2, -2)");
        assertTrue(foundWallAtRelative1Pos1, "Should detect wall at relative position (1, 1)");
    }

    @Test
    void testOtherRobotDetection() {
        // Add other robots to the battle
        Robot robot1 = new Robot();
        robot1.setName("Enemy1");
        robot1.setPositionX(3);
        robot1.setPositionY(3);
        battle.getRobots().add(robot1);

        Robot robot2 = new Robot();
        robot2.setName("Enemy2");
        robot2.setPositionX(7);
        robot2.setPositionY(7);
        battle.getRobots().add(robot2);

        // Scan from robot at (5,5) with range 4
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 4);

        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();

        // Should detect robots within range but not the scanning robot itself
        assertEquals(2, robotDetections.size(), "Should detect exactly two robots within range");

        // Should detect Enemy1 at relative position (-2, -2)
        boolean foundEnemy1 = robotDetections.stream()
            .anyMatch(d -> d.getX() == -2 && d.getY() == -2 && d.getDetails().contains("Enemy1"));
        assertTrue(foundEnemy1, "Should detect Enemy1 at relative position (-2, -2)");

        // Should detect Enemy2 at relative position (2, 2)
        boolean foundEnemy2 = robotDetections.stream()
            .anyMatch(d -> d.getX() == 2 && d.getY() == 2 && d.getDetails().contains("Enemy2"));
        assertTrue(foundEnemy2, "Should detect Enemy2 at relative position (2, 2)");
    }

    @Test
    void testRelativeCoordinatePositioning() {
        // Add objects at known absolute positions
        Robot targetRobot = new Robot();
        targetRobot.setName("Target");
        targetRobot.setPositionX(8);
        targetRobot.setPositionY(2);
        battle.getRobots().add(targetRobot);

        // Robot scanning from (5, 5) - Manhattan distance = 6
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 6);

        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .filter(d -> d.getDetails().contains("Target"))
            .toList();

        assertEquals(1, robotDetections.size(), "Should detect target robot");

        RadarResponse.Detection detection = robotDetections.get(0);
        // Target at (8,2), scanner at (5,5) -> relative position should be (3, -3)
        assertEquals(3, detection.getX(), "Relative X coordinate should be 3");
        assertEquals(-3, detection.getY(), "Relative Y coordinate should be -3");
    }

    @Test
    void testXYCoordinateRepresentationAccuracy() {
        // Test multiple positions to verify X and Y are represented correctly
        Robot rightRobot = new Robot();
        rightRobot.setName("Right");
        rightRobot.setPositionX(7);
        rightRobot.setPositionY(5);
        battle.getRobots().add(rightRobot);

        Robot leftRobot = new Robot();
        leftRobot.setName("Left");
        leftRobot.setPositionX(3);
        leftRobot.setPositionY(5);
        battle.getRobots().add(leftRobot);

        Robot upRobot = new Robot();
        upRobot.setName("Up");
        upRobot.setPositionX(5);
        upRobot.setPositionY(3);
        battle.getRobots().add(upRobot);

        Robot downRobot = new Robot();
        downRobot.setName("Down");
        downRobot.setPositionX(5);
        downRobot.setPositionY(7);
        battle.getRobots().add(downRobot);

        RadarResponse response = radarService.scanArea(battle, scanningRobot, 3);

        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();

        assertEquals(4, robotDetections.size(), "Should detect all 4 positioned robots");

        // Verify each robot's relative position
        for (RadarResponse.Detection detection : robotDetections) {
            if (detection.getDetails().contains("Right")) {
                assertEquals(2, detection.getX(), "Right robot should have positive X");
                assertEquals(0, detection.getY(), "Right robot should have Y=0");
            } else if (detection.getDetails().contains("Left")) {
                assertEquals(-2, detection.getX(), "Left robot should have negative X");
                assertEquals(0, detection.getY(), "Left robot should have Y=0");
            } else if (detection.getDetails().contains("Up")) {
                assertEquals(0, detection.getX(), "Up robot should have X=0");
                assertEquals(-2, detection.getY(), "Up robot should have negative Y");
            } else if (detection.getDetails().contains("Down")) {
                assertEquals(0, detection.getX(), "Down robot should have X=0");
                assertEquals(2, detection.getY(), "Down robot should have positive Y");
            }
        }
    }

    @Test
    void testRangeLimit() {
        // Test that radar respects range limits
        Robot farRobot = new Robot();
        farRobot.setName("FarRobot");
        farRobot.setPositionX(1);
        farRobot.setPositionY(1);
        battle.getRobots().add(farRobot);

        // Distance from (5,5) to (1,1) = 8
        // Scan with small range (should not detect robot at distance 8)
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 7);

        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();

        assertEquals(0, robotDetections.size(), "Should not detect robots outside range");

        // Scan with larger range (should detect robot at distance 8)
        RadarResponse largerResponse = radarService.scanArea(battle, scanningRobot, 8);

        List<RadarResponse.Detection> largerRobotDetections = largerResponse.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();

        assertEquals(1, largerRobotDetections.size(), "Should detect robot within larger range");
    }

    @Test
    void testRobotDoesNotDetectItself() {
        // Scan with the robot - should not detect itself
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 5);

        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .filter(d -> d.getDetails().contains("Scanner"))
            .toList();

        assertEquals(0, robotDetections.size(), "Robot should not detect itself");
    }

    @Test
    void testCoordinateSystemConsistencyWithMovement() {
        // Test that radar coordinates are consistent with movement directions
        // Based on BattleService.moveRobotOneBlock:
        // - NORTH increases Y coordinate
        // - SOUTH decreases Y coordinate  
        // - EAST increases X coordinate
        // - WEST decreases X coordinate
        
        // Place robots in cardinal directions from scanning robot at (5,5)
        Robot northRobot = new Robot();
        northRobot.setName("NorthRobot");
        northRobot.setPositionX(5);
        northRobot.setPositionY(6); // North = higher Y
        battle.getRobots().add(northRobot);
        
        Robot southRobot = new Robot();
        southRobot.setName("SouthRobot");
        southRobot.setPositionX(5);
        southRobot.setPositionY(4); // South = lower Y
        battle.getRobots().add(southRobot);
        
        Robot eastRobot = new Robot();
        eastRobot.setName("EastRobot");
        eastRobot.setPositionX(6); // East = higher X
        eastRobot.setPositionY(5);
        battle.getRobots().add(eastRobot);
        
        Robot westRobot = new Robot();
        westRobot.setName("WestRobot");
        westRobot.setPositionX(4); // West = lower X
        westRobot.setPositionY(5);
        battle.getRobots().add(westRobot);
        
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 3);
        
        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();
        
        assertEquals(4, robotDetections.size(), "Should detect all 4 directional robots");
        
        // Verify radar coordinates match movement coordinate system
        for (RadarResponse.Detection detection : robotDetections) {
            if (detection.getDetails().contains("NorthRobot")) {
                assertEquals(0, detection.getX(), "North robot should have X=0");
                assertEquals(1, detection.getY(), "North robot should have positive Y (consistent with NORTH movement)");
            } else if (detection.getDetails().contains("SouthRobot")) {
                assertEquals(0, detection.getX(), "South robot should have X=0");
                assertEquals(-1, detection.getY(), "South robot should have negative Y (consistent with SOUTH movement)");
            } else if (detection.getDetails().contains("EastRobot")) {
                assertEquals(1, detection.getX(), "East robot should have positive X (consistent with EAST movement)");
                assertEquals(0, detection.getY(), "East robot should have Y=0");
            } else if (detection.getDetails().contains("WestRobot")) {
                assertEquals(-1, detection.getX(), "West robot should have negative X (consistent with WEST movement)");
                assertEquals(0, detection.getY(), "West robot should have Y=0");
            } else {
                fail("Unexpected robot detected: " + detection.getDetails());
            }
        }
    }

    @Test
    void testDiagonalCoordinateConsistency() {
        // Test diagonal positions to ensure coordinate system is consistent
        // in all quadrants relative to scanning robot at (5,5)
        
        Robot northEastRobot = new Robot();
        northEastRobot.setName("NorthEastRobot");
        northEastRobot.setPositionX(7); // East = higher X
        northEastRobot.setPositionY(7); // North = higher Y
        battle.getRobots().add(northEastRobot);
        
        Robot southWestRobot = new Robot();
        southWestRobot.setName("SouthWestRobot");
        southWestRobot.setPositionX(3); // West = lower X
        southWestRobot.setPositionY(3); // South = lower Y
        battle.getRobots().add(southWestRobot);
        
        Robot northWestRobot = new Robot();
        northWestRobot.setName("NorthWestRobot");
        northWestRobot.setPositionX(3); // West = lower X
        northWestRobot.setPositionY(7); // North = higher Y
        battle.getRobots().add(northWestRobot);
        
        Robot southEastRobot = new Robot();
        southEastRobot.setName("SouthEastRobot");
        southEastRobot.setPositionX(7); // East = higher X
        southEastRobot.setPositionY(3); // South = lower Y
        battle.getRobots().add(southEastRobot);
        
        RadarResponse response = radarService.scanArea(battle, scanningRobot, 5);
        
        List<RadarResponse.Detection> robotDetections = response.getDetections().stream()
            .filter(d -> d.getType() == RadarResponse.DetectionType.ROBOT)
            .toList();
        
        assertEquals(4, robotDetections.size(), "Should detect all 4 diagonal robots");
        
        // Verify diagonal coordinates match movement coordinate system
        for (RadarResponse.Detection detection : robotDetections) {
            if (detection.getDetails().contains("NorthEastRobot")) {
                assertEquals(2, detection.getX(), "NorthEast robot should have positive X");
                assertEquals(2, detection.getY(), "NorthEast robot should have positive Y");
            } else if (detection.getDetails().contains("SouthWestRobot")) {
                assertEquals(-2, detection.getX(), "SouthWest robot should have negative X");
                assertEquals(-2, detection.getY(), "SouthWest robot should have negative Y");
            } else if (detection.getDetails().contains("NorthWestRobot")) {
                assertEquals(-2, detection.getX(), "NorthWest robot should have negative X");
                assertEquals(2, detection.getY(), "NorthWest robot should have positive Y");
            } else if (detection.getDetails().contains("SouthEastRobot")) {
                assertEquals(2, detection.getX(), "SouthEast robot should have positive X");
                assertEquals(-2, detection.getY(), "SouthEast robot should have negative Y");
            } else {
                fail("Unexpected robot detected: " + detection.getDetails());
            }
        }
    }
}
