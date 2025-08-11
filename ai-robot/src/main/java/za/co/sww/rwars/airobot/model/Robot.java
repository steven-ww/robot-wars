package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Robot(
        String id,
        String name,
        String battleId,
        int positionX,
        int positionY,
        String direction,
        String status,
        int targetBlocks,
        int blocksRemaining,
        int hitPoints,
        int maxHitPoints
) {}
