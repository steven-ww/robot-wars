package za.co.sww.rwars.robodemo.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents the response from a radar scan.
 */
data class RadarResponse(
    @JsonProperty("range") val range: Int,
    @JsonProperty("detections") val detections: List<Detection>,
) {
    /**
     * Represents a detected object in the radar scan.
     */
    data class Detection(
        @JsonProperty("x") val x: Int,
        @JsonProperty("y") val y: Int,
        @JsonProperty("type") val type: DetectionType,
        @JsonProperty("details") val details: String,
    )

    /**
     * Types of objects that can be detected by radar.
     */
    enum class DetectionType {
        @JsonProperty("WALL")
        WALL,

        @JsonProperty("ROBOT")
        ROBOT,
    }
}
