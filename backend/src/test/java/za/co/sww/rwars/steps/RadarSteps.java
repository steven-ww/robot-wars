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

        // Verify all detections are within range (using relative coordinates)
        for (Map<String, Object> detection : detections) {
            int relativeX = (Integer) detection.get("x");
            int relativeY = (Integer) detection.get("y");

            // Calculate Manhattan distance from robot position (0,0 in relative coordinates)
            int distance = Math.abs(relativeX) + Math.abs(relativeY);
            Assertions.assertTrue(distance <= maxRange,
                "Detection at relative position (" + relativeX + "," + relativeY + ") should be within range " + maxRange + 
                ", but distance is " + distance);
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

    @Then("the radar response should contain coordinates relative to the robot's position")
    public void theRadarResponseShouldContainCoordinatesRelativeToTheRobotsPosition() {
        Assertions.assertEquals(200, radarResponse.getStatusCode());

        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);

        // Get robot position
        Robot robot = battleService.getRobotDetails(currentBattleId, currentRobotId);
        int robotX = robot.getPositionX();
        int robotY = robot.getPositionY();

        // Verify that detections contain relative coordinates
        for (Map<String, Object> detection : detections) {
            int detectionX = (Integer) detection.get("x");
            int detectionY = (Integer) detection.get("y");

            // Relative coordinates should be within the range around (0,0)
            // and should not match absolute arena coordinates
            Assertions.assertTrue(Math.abs(detectionX) <= 5, 
                "Detection X coordinate should be relative (within range), got: " + detectionX);
            Assertions.assertTrue(Math.abs(detectionY) <= 5, 
                "Detection Y coordinate should be relative (within range), got: " + detectionY);
        }
    }

    @And("no detection should have the same coordinates as the robot's absolute position")
    public void noDetectionShouldHaveTheSameCoordinatesAsTheRobotsAbsolutePosition() {
        List<Map<String, Object>> detections = radarResponse.jsonPath().getList("detections");
        Assertions.assertNotNull(detections);

        // Get robot position
        Robot robot = battleService.getRobotDetails(currentBattleId, currentRobotId);
        int robotX = robot.getPositionX();
        int robotY = robot.getPositionY();

        // Verify no detection has the robot's absolute position
        // (since coordinates should be relative, robot position should be (0,0) which is excluded)
        for (Map<String, Object> detection : detections) {
            int detectionX = (Integer) detection.get("x");
            int detectionY = (Integer) detection.get("y");

            Assertions.assertFalse(detectionX == robotX && detectionY == robotY,
                "Detection should not have robot's absolute position (" + robotX + "," + robotY + ")");
        }
    }
}
