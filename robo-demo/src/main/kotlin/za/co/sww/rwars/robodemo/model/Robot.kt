package za.co.sww.rwars.robodemo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents a robot in the Robot Wars game.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Robot(
    val id: String,
    val name: String,
    val battleId: String,
    val positionX: Int = 0,
    val positionY: Int = 0,
    val direction: String = "NORTH",
    val status: String = "IDLE",
    val blocksRemaining: Int = 0,
    val targetBlocks: Int = 0,
)
