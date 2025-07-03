package za.co.sww.rwars.backend.model;

import java.util.UUID;

/**
 * Represents a robot in the battle.
 */
public class Robot {
    private String id;
    private String name;
    private String battleId;

    public Robot() {
        this.id = UUID.randomUUID().toString();
    }

    public Robot(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public Robot(String name, String battleId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.battleId = battleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBattleId() {
        return battleId;
    }

    public void setBattleId(String battleId) {
        this.battleId = battleId;
    }
}
