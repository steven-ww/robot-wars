package za.co.sww.rwars.robodemo.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import za.co.sww.rwars.robodemo.model.RadarResponse
import za.co.sww.rwars.robodemo.model.RadarResponse.DetectionType

/**
 * Unit tests for radar logic and direction validation.
 */
class RadarLogicTest {

    /**
     * Test that direction vectors are correctly calculated.
     */
    @Test
    fun testDirectionVectors() {
        // Test cardinal directions
        assertEquals(Pair(0, 1), getDirectionVector("NORTH"))
        assertEquals(Pair(0, -1), getDirectionVector("SOUTH"))
        assertEquals(Pair(1, 0), getDirectionVector("EAST"))
        assertEquals(Pair(-1, 0), getDirectionVector("WEST"))

        // Test diagonal directions
        assertEquals(Pair(1, 1), getDirectionVector("NE"))
        assertEquals(Pair(-1, 1), getDirectionVector("NW"))
        assertEquals(Pair(1, -1), getDirectionVector("SE"))
        assertEquals(Pair(-1, -1), getDirectionVector("SW"))
    }

    /**
     * Test that walls directly in front of the robot are detected as unsafe.
     */
    @Test
    fun testWallDirectlyInFront() {
        // Wall directly north of robot (at relative position 0, 1)
        val wallNorth = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
        )

        assertFalse(isDirectionSafeRadarOnly("NORTH", wallNorth))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", wallNorth))
        assertTrue(isDirectionSafeRadarOnly("EAST", wallNorth))
        assertTrue(isDirectionSafeRadarOnly("WEST", wallNorth))

        // Wall directly south of robot (at relative position 0, -1)
        val wallSouth = listOf(
            RadarResponse.Detection(0, -1, DetectionType.WALL, "Arena boundary wall"),
        )

        assertTrue(isDirectionSafeRadarOnly("NORTH", wallSouth))
        assertFalse(isDirectionSafeRadarOnly("SOUTH", wallSouth))
        assertTrue(isDirectionSafeRadarOnly("EAST", wallSouth))
        assertTrue(isDirectionSafeRadarOnly("WEST", wallSouth))

        // Wall directly east of robot (at relative position 1, 0)
        val wallEast = listOf(
            RadarResponse.Detection(1, 0, DetectionType.WALL, "Arena boundary wall"),
        )

        assertTrue(isDirectionSafeRadarOnly("NORTH", wallEast))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", wallEast))
        assertFalse(isDirectionSafeRadarOnly("EAST", wallEast))
        assertTrue(isDirectionSafeRadarOnly("WEST", wallEast))

        // Wall directly west of robot (at relative position -1, 0)
        val wallWest = listOf(
            RadarResponse.Detection(-1, 0, DetectionType.WALL, "Arena boundary wall"),
        )

        assertTrue(isDirectionSafeRadarOnly("NORTH", wallWest))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", wallWest))
        assertTrue(isDirectionSafeRadarOnly("EAST", wallWest))
        assertFalse(isDirectionSafeRadarOnly("WEST", wallWest))
    }

    /**
     * Test diagonal wall detection.
     */
    @Test
    fun testDiagonalWallDetection() {
        // Wall at NE diagonal (relative position 1, 1)
        val wallNE = listOf(
            RadarResponse.Detection(1, 1, DetectionType.WALL, "Arena boundary wall"),
        )

        assertTrue(isDirectionSafeRadarOnly("NORTH", wallNE))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", wallNE))
        assertTrue(isDirectionSafeRadarOnly("EAST", wallNE))
        assertTrue(isDirectionSafeRadarOnly("WEST", wallNE))
        assertFalse(isDirectionSafeRadarOnly("NE", wallNE))
        assertTrue(isDirectionSafeRadarOnly("NW", wallNE))
        assertTrue(isDirectionSafeRadarOnly("SE", wallNE))
        assertTrue(isDirectionSafeRadarOnly("SW", wallNE))
    }

    /**
     * Test that walls within 2 blocks but not directly in path are handled correctly.
     */
    @Test
    fun testWallsWithinRange() {
        // Wall at position (2, 0) - 2 blocks east
        val wallFarEast = listOf(
            RadarResponse.Detection(2, 0, DetectionType.WALL, "Arena boundary wall"),
        )

        // Should be safe to move east even though there's a wall 2 blocks away
        assertTrue(isDirectionSafeRadarOnly("EAST", wallFarEast))

        // Wall at position (1, -1) - SE diagonal but close
        val wallSEClose = listOf(
            RadarResponse.Detection(1, -1, DetectionType.WALL, "Arena boundary wall"),
        )

        // Should block SE movement but not other directions
        assertFalse(isDirectionSafeRadarOnly("SE", wallSEClose))
        assertTrue(isDirectionSafeRadarOnly("NORTH", wallSEClose))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", wallSEClose))
        assertTrue(isDirectionSafeRadarOnly("EAST", wallSEClose))
        assertTrue(isDirectionSafeRadarOnly("WEST", wallSEClose))
    }

    /**
     * Test that robots are detected but don't block movement (they can move).
     */
    @Test
    fun testRobotDetection() {
        // Robot directly north of our robot
        val robotNorth = listOf(
            RadarResponse.Detection(0, 1, DetectionType.ROBOT, "Robot: TestBot"),
        )

        // Robots should not block movement (they can move away)
        assertTrue(isDirectionSafeRadarOnly("NORTH", robotNorth))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", robotNorth))
        assertTrue(isDirectionSafeRadarOnly("EAST", robotNorth))
        assertTrue(isDirectionSafeRadarOnly("WEST", robotNorth))
    }

    /**
     * Test multiple detections.
     */
    @Test
    fun testMultipleDetections() {
        // Walls on north and east sides
        val multipleWalls = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
            RadarResponse.Detection(1, 0, DetectionType.WALL, "Arena boundary wall"),
        )

        assertFalse(isDirectionSafeRadarOnly("NORTH", multipleWalls))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", multipleWalls))
        assertFalse(isDirectionSafeRadarOnly("EAST", multipleWalls))
        assertTrue(isDirectionSafeRadarOnly("WEST", multipleWalls))

        // NE should also be blocked as it combines north and east
        assertFalse(isDirectionSafeRadarOnly("NE", multipleWalls))
    }

    /**
     * Test empty radar response (no detections).
     */
    @Test
    fun testEmptyRadarResponse() {
        val emptyDetections = emptyList<RadarResponse.Detection>()

        // All directions should be safe with no detections
        assertTrue(isDirectionSafeRadarOnly("NORTH", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("EAST", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("WEST", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("NE", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("NW", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("SE", emptyDetections))
        assertTrue(isDirectionSafeRadarOnly("SW", emptyDetections))
    }

    /**
     * Test arena boundary scenarios.
     */
    @Test
    fun testArenaBoundaryScenarios() {
        // Robot near north boundary - should detect wall at (0, 1)
        val nearNorthBoundary = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
        )

        assertFalse(isDirectionSafeRadarOnly("NORTH", nearNorthBoundary))
        assertFalse(isDirectionSafeRadarOnly("NE", nearNorthBoundary))
        assertFalse(isDirectionSafeRadarOnly("NW", nearNorthBoundary))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", nearNorthBoundary))
        assertTrue(isDirectionSafeRadarOnly("EAST", nearNorthBoundary))
        assertTrue(isDirectionSafeRadarOnly("WEST", nearNorthBoundary))
        assertTrue(isDirectionSafeRadarOnly("SE", nearNorthBoundary))
        assertTrue(isDirectionSafeRadarOnly("SW", nearNorthBoundary))

        // Robot near corner - should detect walls on two sides
        val nearCorner = listOf(
            RadarResponse.Detection(0, 1, DetectionType.WALL, "Arena boundary wall"),
            RadarResponse.Detection(-1, 0, DetectionType.WALL, "Arena boundary wall"),
        )

        assertFalse(isDirectionSafeRadarOnly("NORTH", nearCorner))
        assertFalse(isDirectionSafeRadarOnly("WEST", nearCorner))
        assertFalse(isDirectionSafeRadarOnly("NW", nearCorner))
        assertTrue(isDirectionSafeRadarOnly("SOUTH", nearCorner))
        assertTrue(isDirectionSafeRadarOnly("EAST", nearCorner))
        assertTrue(isDirectionSafeRadarOnly("SE", nearCorner))
    }

    /**
     * Helper function to get direction vector (copied from Main.kt for testing)
     */
    private fun getDirectionVector(direction: String): Pair<Int, Int> {
        return when (direction) {
            "NORTH" -> Pair(0, 1)
            "SOUTH" -> Pair(0, -1)
            "EAST" -> Pair(1, 0)
            "WEST" -> Pair(-1, 0)
            "NE" -> Pair(1, 1)
            "NW" -> Pair(-1, 1)
            "SE" -> Pair(1, -1)
            "SW" -> Pair(-1, -1)
            else -> Pair(0, 0)
        }
    }

    /**
     * Helper function to check if a direction is safe (copied from Main.kt for testing)
     */
    private fun isDirectionSafeRadarOnly(
        direction: String,
        detections: List<RadarResponse.Detection>,
    ): Boolean {
        val (deltaX, deltaY) = getDirectionVector(direction)

        // Check if any detected walls are in the path of this direction
        for (detection in detections) {
            if (detection.type == DetectionType.WALL) {
                val relativeX = detection.x
                val relativeY = detection.y

                // Check if the wall is directly in the path of movement
                // For diagonal movements, both X and Y must be in the same direction
                // For cardinal movements, only the relevant axis matters
                val wallBlocksMovement = when {
                    // Cardinal directions - check only the relevant axis
                    deltaX == 0 && deltaY != 0 -> {
                        // Moving purely north/south - wall blocks if it's directly in line and within 1 block
                        relativeX == 0 && (relativeY * deltaY > 0) && Math.abs(relativeY) <= 1
                    }
                    deltaY == 0 && deltaX != 0 -> {
                        // Moving purely east/west - wall blocks if it's directly in line and within 1 block
                        relativeY == 0 && (relativeX * deltaX > 0) && Math.abs(relativeX) <= 1
                    }
                    // Diagonal directions - check if wall blocks the diagonal path
                    deltaX != 0 && deltaY != 0 -> {
                        val withinRange = Math.abs(relativeX) <= 1 && Math.abs(relativeY) <= 1
                        if (!withinRange) {
                            false
                        } else {
                            // Wall blocks diagonal movement if:
                            // 1. It's directly on the diagonal path (both components same direction)
                            // 2. It's on one of the cardinal axes that the diagonal crosses
                            val onDiagonalPath = (relativeX * deltaX > 0) && (relativeY * deltaY > 0)
                            val onCardinalInPath = (
                                (relativeX == 0 && relativeY * deltaY > 0) || // On Y axis in direction of movement
                                    (relativeY == 0 && relativeX * deltaX > 0) // On X axis in direction of movement
                                )
                            onDiagonalPath || onCardinalInPath
                        }
                    }
                    else -> false
                }

                if (wallBlocksMovement) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Test robot movements in each cardinal direction.
     */
    @Test
    fun testRobotMovements() {
        val startX = 0
        val startY = 0

        // Test moving North - y should increase, x should remain constant
        val northResult = calculateDestination(startX, startY, "NORTH", 1)
        assertEquals(0, northResult.first) // x remains constant
        assertEquals(1, northResult.second) // y increases

        // Test moving South - y should decrease, x should remain constant
        val southResult = calculateDestination(startX, startY, "SOUTH", 1)
        assertEquals(0, southResult.first) // x remains constant
        assertEquals(-1, southResult.second) // y decreases

        // Test moving East - x should increase, y should remain constant
        val eastResult = calculateDestination(startX, startY, "EAST", 1)
        assertEquals(1, eastResult.first) // x increases
        assertEquals(0, eastResult.second) // y remains constant

        // Test moving West - x should decrease, y should remain constant
        val westResult = calculateDestination(startX, startY, "WEST", 1)
        assertEquals(-1, westResult.first) // x decreases
        assertEquals(0, westResult.second) // y remains constant
    }

    /**
     * Helper function to calculate destination (copied from Main.kt for testing)
     */
    private fun calculateDestination(currentX: Int, currentY: Int, direction: String, blocks: Int): Pair<Int, Int> {
        val (deltaX, deltaY) = when (direction) {
            "NORTH" -> Pair(0, blocks)
            "SOUTH" -> Pair(0, -blocks)
            "EAST" -> Pair(blocks, 0)
            "WEST" -> Pair(-blocks, 0)
            "NE" -> Pair(blocks, blocks)
            "NW" -> Pair(-blocks, blocks)
            "SE" -> Pair(blocks, -blocks)
            "SW" -> Pair(-blocks, -blocks)
            else -> Pair(0, 0)
        }
        return Pair(currentX + deltaX, currentY + deltaY)
    }
}
