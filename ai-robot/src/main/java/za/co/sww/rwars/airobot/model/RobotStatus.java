package za.co.sww.rwars.airobot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RobotStatus(
        String status,
        int hitPoints,
        int maxHitPoints
) {
    public boolean isActive() {
        return hitPoints > 0 && !"CRASHED".equals(status) && !"DESTROYED".equals(status);
    }
}
