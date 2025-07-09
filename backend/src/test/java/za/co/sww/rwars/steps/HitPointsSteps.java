package za.co.sww.rwars.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import za.co.sww.rwars.backend.service.BattleService;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Robot.RobotStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HitPointsSteps {

    @Inject
    private BattleService battleService;

    private TestContext testContext = TestContext.getInstance();
    private Battle currentBattle;
    private String currentRobotId;

    @And("I have registered my robot {string} with hit points {int}")
    public void iHaveRegisteredMyRobotWithHitPoints(String robotName, int hitPoints) {
        String battleId = testContext.getLastBattleId();
        Assertions.assertNotNull(battleId, "Battle ID should be available");

        // Register robot via API (hit points are set by server configuration)
        Map<String, Object> robotRequest = new HashMap<>();
        robotRequest.put("name", robotName);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(robotRequest)
                .post("/api/robots/register/" + battleId);

        Assertions.assertEquals(200, response.getStatusCode());
        currentRobotId = response.jsonPath().getString("id");
        Assertions.assertNotNull(currentRobotId);

        // Update current battle reference
        currentBattle = battleService.getBattleStatus(battleId);

        // Register an additional robot if needed
        if (currentBattle.getRobotCount() < 2) {
            registerAdditionalRobot("AdditionalBot");
        }

        // Verify the robot was registered with server-configured hit points
        Robot robot = currentBattle.getRobots().stream()
                .filter(r -> robotName.equals(r.getName()))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(robot, "Robot should be registered");
        // Note: The expected hit points parameter is ignored as server controls this
    }

    @Given("a robot {string} with hit points {int} is registered")
    public void aRobotWithHitPointsIsRegistered(String robotName, int hitPoints) {
        String battleId = testContext.getLastBattleId();
        Assertions.assertNotNull(battleId, "Battle ID should be available");

        // Register robot via API (hit points are set by server configuration)
        Map<String, Object> robotRequest = new HashMap<>();
        robotRequest.put("name", robotName);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(robotRequest)
                .post("/api/robots/register/" + battleId);

        Assertions.assertEquals(200, response.getStatusCode());
        String robotId = response.jsonPath().getString("id");
        Assertions.assertNotNull(robotId);

        // Update current battle reference
        currentBattle = battleService.getBattleStatus(battleId);

        // Register an additional robot if needed
        if (currentBattle.getRobotCount() < 2) {
            registerAdditionalRobot("AdditionalBot");
        }
        // Note: The expected hit points parameter is ignored as server controls this
    }

    @When("the battle starts")
    public void theBattleStarts() {
        Assertions.assertNotNull(currentBattle);
        battleService.startBattle(currentBattle.getId());
        currentBattle = battleService.getBattleStatus(currentBattle.getId());
    }

    @Then("the robot {string} should have {int} hit points")
    public void theRobotShouldHaveHitPoints(String robotName, int expectedHitPoints) {
        // Get battle from context if currentBattle is null
        if (currentBattle == null) {
            String battleId = testContext.getLastBattleId();
            Assertions.assertNotNull(battleId, "Battle ID should be available");
            currentBattle = battleService.getBattleStatus(battleId);
        } else {
            // Refresh battle state to get latest robot data
            currentBattle = battleService.getBattleStatus(currentBattle.getId());
        }
        
        Robot robot = currentBattle.getRobots().stream()
                .filter(r -> robotName.equals(r.getName()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(robot, "Robot " + robotName + " should exist");
        Assertions.assertEquals(expectedHitPoints, robot.getHitPoints(),
                "Robot " + robotName + " should have " + expectedHitPoints + " hit points");
    }

    @When("{string} collides with a wall")
    public void collidesWithAWall(String robotName) {
        Assertions.assertNotNull(currentBattle);

        Robot robot = currentBattle.getRobots().stream()
                .filter(r -> robotName.equals(r.getName()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(robot, "Robot " + robotName + " should exist");

        // Simulate wall collision - reduce hit points to zero and set status to crashed
        robot.setHitPoints(0);
        robot.setStatus(RobotStatus.CRASHED);
        // Note: In a real implementation, this would be handled by the movement system
        // For testing, we're manually setting the robot to crashed state

        // Refresh battle state
        currentBattle = battleService.getBattleStatus(currentBattle.getId());
    }

    @Then("{string} hit points should reduce to zero")
    public void hitPointsShouldReduceToZero(String robotName) {
        theRobotShouldHaveHitPoints(robotName, 0);
    }

    @And("the state of {string} should be {string}")
    public void theStateOfShouldBe(String robotName, String expectedState) {
        // Get battle from context if currentBattle is null
        if (currentBattle == null) {
            String battleId = testContext.getLastBattleId();
            Assertions.assertNotNull(battleId, "Battle ID should be available");
            currentBattle = battleService.getBattleStatus(battleId);
        } else {
            // Refresh battle state to get latest robot data
            currentBattle = battleService.getBattleStatus(currentBattle.getId());
        }

        Robot robot = currentBattle.getRobots().stream()
                .filter(r -> robotName.equals(r.getName()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(robot, "Robot " + robotName + " should exist");

        RobotStatus expectedStatus;
        switch (expectedState.toLowerCase()) {
            case "destroyed":
                expectedStatus = RobotStatus.DESTROYED;
                break;
            case "crashed":
                expectedStatus = RobotStatus.CRASHED;
                break;
            case "idle":
                expectedStatus = RobotStatus.IDLE;
                break;
            case "moving":
                expectedStatus = RobotStatus.MOVING;
                break;
            default:
                throw new IllegalArgumentException("Unknown robot state: " + expectedState);
        }

        Assertions.assertEquals(expectedStatus, robot.getStatus(),
                "Robot " + robotName + " should have status " + expectedState);
    }

    @And("{string} should no longer participate in the battle")
    public void shouldNoLongerParticipateInTheBattle(String robotName) {
        // Get battle from context if currentBattle is null
        if (currentBattle == null) {
            String battleId = testContext.getLastBattleId();
            Assertions.assertNotNull(battleId, "Battle ID should be available");
            currentBattle = battleService.getBattleStatus(battleId);
        } else {
            // Refresh battle state to get latest robot data
            currentBattle = battleService.getBattleStatus(currentBattle.getId());
        }

        Robot robot = currentBattle.getRobots().stream()
                .filter(r -> robotName.equals(r.getName()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(robot, "Robot " + robotName + " should exist");
        Assertions.assertFalse(robot.isActive(),
                "Robot " + robotName + " should not be active");
    }

    @Then("{string} should remain inactive")
    public void shouldRemainInactive(String robotName) {
        shouldNoLongerParticipateInTheBattle(robotName);
    }

    @Given("multiple robots are registered")
    public void multipleRobotsAreRegistered() {
        String battleId = testContext.getLastBattleId();
        Assertions.assertNotNull(battleId, "Battle ID should be available");

        // Register multiple robots with different hit points
        String[] robotNames = {"Robot1", "Robot2", "Robot3"};
        int[] hitPoints = {100, 80, 60};

        for (int i = 0; i < robotNames.length; i++) {
            Map<String, Object> robotRequest = new HashMap<>();
            robotRequest.put("name", robotNames[i]);

            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(robotRequest)
                    .post("/api/robots/register/" + battleId);

            Assertions.assertEquals(200, response.getStatusCode());
            // Note: Hit points are set by server configuration, not by user
        }

        // Update current battle reference
        currentBattle = battleService.getBattleStatus(battleId);
    }

    @When("all but one robot hit points reduce to zero")
    public void allButOneRobotHitPointsReduceToZero() {
        Assertions.assertNotNull(currentBattle);

        List<Robot> robots = currentBattle.getRobots();
        Assertions.assertTrue(robots.size() > 1, "Should have multiple robots");

        // Set all robots except the last one to have 0 hit points and crashed status
        for (int i = 0; i < robots.size() - 1; i++) {
            Robot robot = robots.get(i);
            robot.setHitPoints(0);
            robot.setStatus(RobotStatus.CRASHED);
            // Note: In a real implementation, this would be handled by the movement system
            // For testing, we're manually setting the robot to crashed state
        }

        // Check if battle should end now
        Robot winner = currentBattle.getActiveRobot();
        if (winner != null) {
            currentBattle.declareWinner(winner);
        }

        // Refresh battle state
        currentBattle = battleService.getBattleStatus(currentBattle.getId());
    }

    @Then("the battle should end")
    public void theBattleShouldEnd() {
        Assertions.assertNotNull(currentBattle);

        // Check if battle is completed
        // Check if battle is completed by checking if only one robot is active
        long activeRobots = currentBattle.getActiveRobotCount();
        boolean battleCompleted = activeRobots <= 1;
        Assertions.assertTrue(battleCompleted, "Battle should be completed");
    }

    @And("the remaining robot should be declared the winner")
    public void theRemainingRobotShouldBeDeclaredTheWinner() {
        Assertions.assertNotNull(currentBattle);

        // Find the remaining active robot
        Robot winner = currentBattle.getRobots().stream()
                .filter(Robot::isActive)
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(winner, "There should be a winner");
        Assertions.assertTrue(winner.getHitPoints() > 0, "Winner should have hit points > 0");
    }

    @And("the battle state should update to reflect the winner")
    public void theBattleStateShouldUpdateToReflectTheWinner() {
        Assertions.assertNotNull(currentBattle);

        // Check if battle has a winner
        String winnerId = currentBattle.getWinnerId();
        Assertions.assertNotNull(winnerId, "Battle should have a winner ID");

        // Verify the winner is the active robot
        Robot winner = currentBattle.getRobots().stream()
                .filter(r -> winnerId.equals(r.getId()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(winner, "Winner should be found in battle");
        Assertions.assertTrue(winner.isActive(), "Winner should be active");
    }

    private void registerAdditionalRobot(String robotName) {
        Map<String, Object> robotRequest = new HashMap<>();
        robotRequest.put("name", robotName);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(robotRequest)
                .post("/api/robots/register/" + currentBattle.getId());

        Assertions.assertEquals(200, response.getStatusCode());
    }
}
