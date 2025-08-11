package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Battle(
        String id,
        String name,
        @JsonProperty("arenaWidth") int arenaWidth,
        @JsonProperty("arenaHeight") int arenaHeight,
        @JsonProperty("robotMovementTimeSeconds") double robotMovementTimeSeconds,
        String state,
        List<Robot> robots,
        List<Object> walls,
        String winnerId,
        String winnerName
) {}
