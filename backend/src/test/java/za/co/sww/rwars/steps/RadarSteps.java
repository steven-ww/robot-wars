package za.co.sww.rwars.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import za.co.sww.rwars.backend.service.BattleService;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Wall;
import za.co.sww.rwars.steps.TestContext;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class RadarSteps {

    @Inject
    private BattleService battleService;

    private TestContext testContext = TestContext.getInstance();
    private Response radarResponse;
    private String currentRobotId;
    private String currentBattleId;


    @And("I have registered my robot {string}")
    public void iHaveRegisteredMyRobot(String robotName) {
        // Get the current battle ID from context
        currentBattleId = testContext.getLastBattleId();
        Assertions.assertNotNull(currentBattleId, "Battle ID should be available");

        // Register robot via API
        Map<String, Object> robotRequest = new HashMap<>();
        robotRequest.put("name", robotName);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(robotRequest)
                .post("/api/robots/register/" + currentBattleId);

        Assertions.assertEquals(200, response.getStatusCode());
        currentRobotId = response.jsonPath().getString("id");
        Assertions.assertNotNull(currentRobotId);

        // Register an additional robot if needed to start the battle
        Battle battle = battleService.getBattleStatus(currentBattleId);
        if (battle.getRobotCount() < 2) {
            Map<String, Object> robotRequest2 = new HashMap<>();
            robotRequest2.put("name", "SecondRobot");
            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(robotRequest2)
                    .post("/api/robots/register/" + currentBattleId);
        }
    }

    @And("I have registered another robot {string}")
    public void iHaveRegisteredAnotherRobot(String robotName) {
        // Get the current battle ID from context
        currentBattleId = testContext.getLastBattleId();
        Assertions.assertNotNull(currentBattleId, "Battle ID should be available");

        // Register robot via API
        Map<String, Object> robotRequest = new HashMap<>();
        robotRequest.put("name", robotName);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(robotRequest)
                .post("/api/robots/register/" + currentBattleId);

        Assertions.assertEquals(200, response.getStatusCode());
    }

    @And("there are walls placed in the arena")
    public void thereAreWallsPlacedInTheArena() {
        // Walls are automatically generated when battle is created
        // We just need to verify they exist
        Battle battle = battleService.getBattleStatus(currentBattleId);
        Assertions.assertNotNull(battle);
        Assertions.assertTrue(battle.getWalls().size() > 0, "Battle should have walls");
    }

    @And("there are walls in the arena")
    public void thereAreWallsInTheArena() {
        thereAreWallsPlacedInTheArena();
    }

    @When("I invoke the radar API with range {int}")
    public void iInvokeTheRadarApiWithRange(int range) {
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", range);

        radarResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(radarRequest)
                .post("/api/robots/battle/" + currentBattleId + "/robot/" + currentRobotId + "/radar");
    }

    @Then("I should receive a radar response showing detected walls within range")
    public void iShouldReceiveARadarResponseShowingDetectedWallsWithinRange() {
        Assertions.assertEquals(200, radarResponse.getStatusCode());

        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);


        // Verify at least one wall is detected
        boolean wallDetected = detections.stream()
                .anyMatch(detection -> "WALL".equals(detection.get("type")));
        Assertions.assertTrue(wallDetected, "At least one wall should be detected");
    }

    @And("the response should indicate {string} at the detected positions")
    public void theResponseShouldIndicateAtTheDetectedPositions(String expectedType) {
        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);

        boolean typeFound = detections.stream()
                .anyMatch(detection -> expectedType.equals(detection.get("type")));
        Assertions.assertTrue(typeFound, "Should find detection of type: " + expectedType);
    }

    @Then("I should receive a radar response showing detected robots within range")
    public void iShouldReceiveARadarResponseShowingDetectedRobotsWithinRange() {
        Assertions.assertEquals(200, radarResponse.getStatusCode());

        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);

        // Verify at least one robot is detected
        boolean robotDetected = detections.stream()
                .anyMatch(detection -> "ROBOT".equals(detection.get("type")));
        Assertions.assertTrue(robotDetected, "At least one robot should be detected");
    }

    @Then("I should receive a radar response for positions within {int} blocks")
    public void iShouldReceiveARadarResponseForPositionsWithinBlocks(int maxRange) {
        Assertions.assertEquals(200, radarResponse.getStatusCode());

        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);

        // Verify all detections are within range
        for (Map<String, Object> detection : detections) {
            int x = (Integer) detection.get("x");
            int y = (Integer) detection.get("y");

            // Get robot position to calculate distance
            Robot robot = battleService.getRobotDetails(currentBattleId, currentRobotId);
            int robotX = robot.getPositionX();
            int robotY = robot.getPositionY();

            double distance = Math.sqrt(Math.pow(x - robotX, 2) + Math.pow(y - robotY, 2));
            Assertions.assertTrue(distance <= maxRange,
                "Detection at (" + x + "," + y + ") should be within range " + maxRange);
        }
    }

    @And("positions beyond {int} blocks should not be included in the response")
    public void positionsBeyondBlocksShouldNotBeIncludedInTheResponse(int maxRange) {
        // This is already verified in the previous step
        // This step is just for readability
    }

    @Then("I should receive an empty radar response")
    public void iShouldReceiveAnEmptyRadarResponse() {
        Assertions.assertEquals(200, radarResponse.getStatusCode());

        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);
        Assertions.assertTrue(detections.isEmpty(), "Radar response should be empty");
    }

    @And("no obstacles should be detected within the scanned area")
    public void noObstaclesShouldBeDetectedWithinTheScannedArea() {
        // This is already verified in the previous step
        // This step is just for readability
    }
}
