package za.co.sww.rwars.robodemo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents a battle in the Robot Wars game.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Battle(
    val id: String,
    val name: String,
    val arenaWidth: Int = 50,
    val arenaHeight: Int = 50,
    val state: String = "READY",
    val robots: List<Robot> = emptyList(),
    val robotMovementTimeSeconds: Double = 1.0,
    val winnerId: String? = null,
    val winnerName: String? = null,
)
