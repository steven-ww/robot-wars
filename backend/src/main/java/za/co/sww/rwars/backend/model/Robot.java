package za.co.sww.rwars.backend.model;

/**
 * Represents a robot in the battle.
 */
public class Robot {
    private String name;
    private String battleId;

    public Robot() {
    }

    public Robot(String name) {
        this.name = name;
    }

    public Robot(String name, String battleId) {
        this.name = name;
        this.battleId = battleId;
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
