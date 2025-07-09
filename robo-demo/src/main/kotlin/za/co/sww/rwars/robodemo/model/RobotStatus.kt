package za.co.sww.rwars.robodemo.model

/**
 * Represents a robot's status information without revealing its absolute position.
 * This is what robots should know about themselves - their identity and state,
 * but not their absolute coordinates in the arena.
 */
data class RobotStatus(
    val id: String,
    val name: String,
    val battleId: String,
    val direction: String,
    val status: String,
    val targetBlocks: Int,
    val blocksRemaining: Int,
    val hitPoints: Int,
    val maxHitPoints: Int,
)
