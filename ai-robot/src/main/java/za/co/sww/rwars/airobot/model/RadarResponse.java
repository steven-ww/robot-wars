package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RadarResponse(
        int range,
        List<Detection> detections
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detection(int x, int y, String type, String details) {}
}
