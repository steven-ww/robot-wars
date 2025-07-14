package za.co.sww.rwars.robodemo.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Response object for laser firing operations.
 */
data class LaserResponse(
    @JsonProperty("hit") val hit: Boolean,
    @JsonProperty("hitRobotId") val hitRobotId: String? = null,
    @JsonProperty("hitRobotName") val hitRobotName: String? = null,
    @JsonProperty("damageDealt") val damageDealt: Int = 0,
    @JsonProperty("range") val range: Int,
    @JsonProperty("direction") val direction: String,
    @JsonProperty("laserPath") val laserPath: List<Position> = emptyList(),
    @JsonProperty("hitPosition") val hitPosition: Position? = null,
    @JsonProperty("blockedBy") val blockedBy: String? = null,
) {
    /**
     * Position coordinates.
     */
    data class Position(
        @JsonProperty("x") val x: Int,
        @JsonProperty("y") val y: Int,
    )
}
