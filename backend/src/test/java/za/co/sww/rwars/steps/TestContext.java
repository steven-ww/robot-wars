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

    public static TestContext getInstance() {
        return INSTANCE;
    }

    public void clear() {
        battlesByName.clear();
        robotsByName.clear();
    }

    public void storeBattle(String battleName, String battleId) {
        battlesByName.put(battleName, battleId);
    }

    public String getBattleId(String battleName) {
        return battlesByName.get(battleName);
    }

    public Map<String, String> getAllBattles() {
        return new HashMap<>(battlesByName);
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
}
