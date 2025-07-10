package za.co.sww.rwars.steps;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared context for storing test data between step definitions.
 * This allows different step definition classes to share state.
 */
public class TestContext {

    private static final TestContext INSTANCE = new TestContext();

    private final Map<String, String> battlesByName = new HashMap<>(); // battleName -> battleId
    private final Map<String, String> robotsByName = new HashMap<>(); // robotName -> robotId
    private String currentBattleId;
    private int firstDeleteStatus;
    private int secondDeleteStatus;

    public static TestContext getInstance() {
        return INSTANCE;
    }

    public void clear() {
        battlesByName.clear();
        robotsByName.clear();
        currentBattleId = null;
        firstDeleteStatus = 0;
        secondDeleteStatus = 0;
    }

    public void storeBattle(String battleName, String battleId) {
        battlesByName.put(battleName, battleId);
        // Set as current battle if none is set
        if (currentBattleId == null) {
            currentBattleId = battleId;
        }
    }

    public String getBattleId(String battleName) {
        return battlesByName.get(battleName);
    }

    public Map<String, String> getAllBattles() {
        return new HashMap<>(battlesByName);
    }

    public String getLastBattleId() {
        return battlesByName.values().stream().reduce((first, second) -> second).orElse(null);
    }

    public String getCurrentBattleId() {
        return currentBattleId != null ? currentBattleId : getLastBattleId();
    }

    public void setCurrentBattleId(String battleId) {
        this.currentBattleId = battleId;
    }

    public void storeRobot(String robotName, String robotId) {
        robotsByName.put(robotName, robotId);
    }

    public String getRobotId(String robotName) {
        return robotsByName.get(robotName);
    }

    public Map<String, String> getAllRobots() {
        return new HashMap<>(robotsByName);
    }

    public void setFirstDeleteStatus(int status) {
        this.firstDeleteStatus = status;
    }

    public int getFirstDeleteStatus() {
        return firstDeleteStatus;
    }

    public void setSecondDeleteStatus(int status) {
        this.secondDeleteStatus = status;
    }

    public int getSecondDeleteStatus() {
        return secondDeleteStatus;
    }
}
