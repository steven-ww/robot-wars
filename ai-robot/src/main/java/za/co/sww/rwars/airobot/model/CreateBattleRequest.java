package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateBattleRequest(
        String name,
        Integer width,
        Integer height,
        @JsonProperty("robotMovementTimeSeconds") Double robotMovementTimeSeconds
) {}
