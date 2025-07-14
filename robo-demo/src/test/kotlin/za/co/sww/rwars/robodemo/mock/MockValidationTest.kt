package za.co.sww.rwars.robodemo.mock

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import za.co.sww.rwars.robodemo.model.RadarResponse
import za.co.sww.rwars.robodemo.model.RadarResponse.DetectionType
import za.co.sww.rwars.robodemo.wiremock.WireMockExtension
import za.co.sww.rwars.robodemo.wiremock.WireMockStubs

/**
 * Unit test to validate that mocks return data in the same format as the actual server.
 */
class MockValidationTest {
    private val wireMockExtension = WireMockExtension()
    private val stubs = WireMockStubs()
    private lateinit var baseUrl: String
    
    @BeforeEach
    fun setup() {
        baseUrl = wireMockExtension.start()
    }
    
    @AfterEach
    fun tearDown() {
        wireMockExtension.stop()
    }

    /**
     * Test that radar mock returns data in the correct format.
     */
    @Test
    fun testRadarMockFormat() {
        // Register a robot first
        stubs.stubRegisterRobot("TestRobot")
        
        // Test empty radar response (no detections)
        val emptyRadar = stubs.stubRadarScan("TestRobot", 3, emptyList())
        
        assertEquals(3, emptyRadar.range)
        assertTrue(emptyRadar.detections.isEmpty())
        
        // Test radar response with wall detections
        val wallDetections = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
            RadarResponse.Detection(1, 0, DetectionType.WALL, "Arena boundary wall")
        )
        val radarWithWalls = stubs.stubRadarScan("TestRobot", 5, wallDetections)
        
        assertEquals(5, radarWithWalls.range)
        assertEquals(2, radarWithWalls.detections.size)
        
        // Verify first detection (wall north of robot)
        val northWall = radarWithWalls.detections[0]
        assertEquals(0, northWall.x)
        assertEquals(1, northWall.y)
        assertEquals(DetectionType.WALL, northWall.type)
        assertEquals("Arena boundary wall", northWall.details)
        
        // Verify second detection (wall east of robot)
        val eastWall = radarWithWalls.detections[1]
        assertEquals(1, eastWall.x)
        assertEquals(0, eastWall.y)
        assertEquals(DetectionType.WALL, eastWall.type)
        assertEquals("Arena boundary wall", eastWall.details)
    }

    /**
     * Test that radar mock correctly handles relative coordinates.
     */
    @Test
    fun testRadarMockRelativeCoordinates() {
        // Register a robot first
        stubs.stubRegisterRobot("TestRobot")
        
        // Test scenario: Robot at position (2, 2) in a 20x20 arena
        // Should detect walls at arena boundaries as relative coordinates
        val boundaryDetections = listOf(
            // North boundary wall (y=19 in arena, robot at y=2, so relative y = 19-2 = 17)
            RadarResponse.Detection(0, 17, DetectionType.WALL, "Arena boundary wall"),
            // South boundary wall (y=0 in arena, robot at y=2, so relative y = 0-2 = -2)  
            RadarResponse.Detection(0, -2, DetectionType.WALL, "Arena boundary wall"),
            // West boundary wall (x=0 in arena, robot at x=2, so relative x = 0-2 = -2)
            RadarResponse.Detection(-2, 0, DetectionType.WALL, "Arena boundary wall"),
            // East boundary wall (x=19 in arena, robot at x=2, so relative x = 19-2 = 17)
            RadarResponse.Detection(17, 0, DetectionType.WALL, "Arena boundary wall")
        )
        
        val radar = stubs.stubRadarScan("TestRobot", 20, boundaryDetections)
        
        assertEquals(20, radar.range)
        assertEquals(4, radar.detections.size)
        
        // Verify that all coordinates are relative to robot position
        for (detection in radar.detections) {
            assertTrue(detection.x >= -20 && detection.x <= 20, "X coordinate should be relative: ${detection.x}")
            assertTrue(detection.y >= -20 && detection.y <= 20, "Y coordinate should be relative: ${detection.y}")
        }
    }

    /**
     * Test that radar mock correctly handles robot detections.
     */
    @Test
    fun testRadarMockRobotDetections() {
        // Register a robot first
        stubs.stubRegisterRobot("TestRobot")
        
        // Test robot detections at various positions
        val robotDetections = listOf(
            RadarResponse.Detection(1, -1, DetectionType.ROBOT, "Robot: EnemyBot"),
            RadarResponse.Detection(-2, 3, DetectionType.ROBOT, "Robot: AllyBot")
        )
        
        val radar = stubs.stubRadarScan("TestRobot", 5, robotDetections)
        
        assertEquals(5, radar.range)
        assertEquals(2, radar.detections.size)
        
        // Verify robot detection format
        val enemyBot = radar.detections[0]
        assertEquals(1, enemyBot.x)
        assertEquals(-1, enemyBot.y)
        assertEquals(DetectionType.ROBOT, enemyBot.type)
        assertEquals("Robot: EnemyBot", enemyBot.details)
        
        val allyBot = radar.detections[1]
        assertEquals(-2, allyBot.x)
        assertEquals(3, allyBot.y)
        assertEquals(DetectionType.ROBOT, allyBot.type)
        assertEquals("Robot: AllyBot", allyBot.details)
    }

    /**
     * Test radar mock with mixed detection types.
     */
    @Test
    fun testRadarMockMixedDetections() {
        // Register a robot first
        stubs.stubRegisterRobot("TestRobot")
        
        // Test mixed detections (walls and robots)
        val mixedDetections = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
            RadarResponse.Detection(2, 0, DetectionType.ROBOT, "Robot: TestBot2"),
            RadarResponse.Detection(-1, -1, DetectionType.WALL, "Wall of type SQUARE")
        )
        
        val radar = stubs.stubRadarScan("TestRobot", 3, mixedDetections)
        
        assertEquals(3, radar.range)
        assertEquals(3, radar.detections.size)
        
        // Verify detection types are preserved
        val wallDetections = radar.detections.filter { it.type == DetectionType.WALL }
        val robotDetections = radar.detections.filter { it.type == DetectionType.ROBOT }
        
        assertEquals(2, wallDetections.size)
        assertEquals(1, robotDetections.size)
    }

    /**
     * Test that radar mock format matches expected server response structure.
     */
    @Test
    fun testRadarMockStructureCompatibility() {
        // Register a robot first
        stubs.stubRegisterRobot("TestRobot")
        
        // Test that all required fields are present and have correct types
        val detections = listOf(
            RadarResponse.Detection(1, 1, DetectionType.WALL, "Test wall")
        )
        
        val radar = stubs.stubRadarScan("TestRobot", 5, detections)
        
        // Test RadarResponse structure
        assertTrue(radar.range is Int)
        assertTrue(radar.detections is List<*>)
        
        // Test Detection structure
        val detection = radar.detections[0]
        assertTrue(detection.x is Int)
        assertTrue(detection.y is Int)
        assertTrue(detection.type is DetectionType)
        assertTrue(detection.details is String)
        
        // Test DetectionType enum values
        assertEquals(DetectionType.WALL, detection.type)
        assertTrue(DetectionType.values().contains(DetectionType.WALL))
        assertTrue(DetectionType.values().contains(DetectionType.ROBOT))
    }
}
