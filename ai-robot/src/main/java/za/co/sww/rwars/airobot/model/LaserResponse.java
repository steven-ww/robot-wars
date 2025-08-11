package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LaserResponse(
        boolean hit,
        String hitRobotId,
        String hitRobotName,
        int damageDealt,
        int range,
        String direction,
        List<Position> laserPath,
        Position hitPosition,
        String blockedBy
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Position(int x, int y) {}
}
