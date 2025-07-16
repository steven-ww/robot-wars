package za.co.sww.rwars.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import za.co.sww.rwars.backend.service.BattleService;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class RobotActionsSteps {

    @Inject
    private BattleService battleService;

    private Response response;
    private RequestSpecification request;
    private TestContext testContext = TestContext.getInstance();

    @Before
    public void setup() {
        request = RestAssured.given();
        request.contentType(ContentType.JSON);

        // Reset the battle service before each test
        if (battleService != null) {
            battleService.resetBattle();
        }
    }

    // Move Robot Validation Steps
    @When("I attempt to move a robot with null battle ID")
    public void iAttemptToMoveARobotWithNullBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/null/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with empty battle ID")
    public void iAttemptToMoveARobotWithEmptyBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/ /robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with battle ID longer than 100 characters")
    public void iAttemptToMoveARobotWithBattleIdLongerThan100Characters() {
        String robotId = testContext.getFirstAvailableRobotId();
        String longBattleId = "a".repeat(101);
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + longBattleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with null robot ID")
    public void iAttemptToMoveARobotWithNullRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/null/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with empty robot ID")
    public void iAttemptToMoveARobotWithEmptyRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/ /move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with robot ID longer than 100 characters")
    public void iAttemptToMoveARobotWithRobotIdLongerThan100Characters() {
        String battleId = testContext.getCurrentBattleId();
        String longRobotId = "a".repeat(101);
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + longRobotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with null move request")
    public void iAttemptToMoveARobotWithNullMoveRequest() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();

        response = request.post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with null direction")
    public void iAttemptToMoveARobotWithNullDirection() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", null);
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with empty direction")
    public void iAttemptToMoveARobotWithEmptyDirection() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "");
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with direction {string}")
    public void iAttemptToMoveARobotWithDirection(String direction) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", direction);
        moveRequest.put("blocks", 1);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I attempt to move a robot with {int} blocks")
    public void iAttemptToMoveARobotWithBlocks(int blocks) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", "NORTH");
        moveRequest.put("blocks", blocks);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
        testContext.setResponse(response);
    }

    @When("I move a robot with direction {string} and {int} blocks")
    public void iMoveARobotWithDirectionAndBlocks(String direction, int blocks) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("direction", direction);
        moveRequest.put("blocks", blocks);

        response = request.body(moveRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move");
    }

    @Then("the robot should move successfully")
    public void theRobotShouldMoveSuccessfully() {
        response.then().statusCode(200);
    }

    // Radar Scan Validation Steps
    @When("I attempt to perform radar scan with null battle ID")
    public void iAttemptToPerformRadarScanWithNullBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", 5);

        response = request.body(radarRequest).post("/api/robots/battle/null/robot/" + robotId + "/radar");
        testContext.setResponse(response);
    }

    @When("I attempt to perform radar scan with empty battle ID")
    public void iAttemptToPerformRadarScanWithEmptyBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", 5);

        response = request.body(radarRequest).post("/api/robots/battle/ /robot/" + robotId + "/radar");
        testContext.setResponse(response);
    }

    @When("I attempt to perform radar scan with null robot ID")
    public void iAttemptToPerformRadarScanWithNullRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", 5);

        response = request.body(radarRequest).post("/api/robots/battle/" + battleId + "/robot/null/radar");
        testContext.setResponse(response);
    }

    @When("I attempt to perform radar scan with empty robot ID")
    public void iAttemptToPerformRadarScanWithEmptyRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", 5);

        response = request.body(radarRequest).post("/api/robots/battle/" + battleId + "/robot/ /radar");
        testContext.setResponse(response);
    }

    @When("I attempt to perform radar scan with null radar request")
    public void iAttemptToPerformRadarScanWithNullRadarRequest() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();

        response = request.post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/radar");
        testContext.setResponse(response);
    }

    @When("I attempt to perform radar scan with range {int}")
    public void iAttemptToPerformRadarScanWithRange(int range) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", range);

        response = request.body(radarRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/radar");
        testContext.setResponse(response);
    }

    @When("I perform radar scan with range {int}")
    public void iPerformRadarScanWithRange(int range) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> radarRequest = new HashMap<>();
        radarRequest.put("range", range);

        response = request.body(radarRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/radar");
    }

    @Then("the radar scan should complete successfully")
    public void theRadarScanShouldCompleteSuccessfully() {
        response.then().statusCode(200);
    }

    // Laser Fire Validation Steps
    @When("I attempt to fire laser with null battle ID")
    public void iAttemptToFireLaserWithNullBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "NORTH");
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/null/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with empty battle ID")
    public void iAttemptToFireLaserWithEmptyBattleId() {
        String robotId = testContext.getFirstAvailableRobotId();
        if (robotId == null) {
            robotId = "dummy-robot-id";
        }
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "NORTH");
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/ /robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with null robot ID")
    public void iAttemptToFireLaserWithNullRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "NORTH");
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/null/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with empty robot ID")
    public void iAttemptToFireLaserWithEmptyRobotId() {
        String battleId = testContext.getCurrentBattleId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "NORTH");
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/ /laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with null laser request")
    public void iAttemptToFireLaserWithNullLaserRequest() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();

        response = request.post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with null direction")
    public void iAttemptToFireLaserWithNullDirection() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", null);
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with empty direction")
    public void iAttemptToFireLaserWithEmptyDirection() {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "");
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with direction {string}")
    public void iAttemptToFireLaserWithDirection(String direction) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", direction);
        laserRequest.put("range", 10);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I attempt to fire laser with range {int}")
    public void iAttemptToFireLaserWithRange(int range) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", "NORTH");
        laserRequest.put("range", range);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
        testContext.setResponse(response);
    }

    @When("I fire laser with direction {string} and range {int}")
    public void iFireLaserWithDirectionAndRange(String direction, int range) {
        String battleId = testContext.getCurrentBattleId();
        String robotId = testContext.getFirstAvailableRobotId();
        Map<String, Object> laserRequest = new HashMap<>();
        laserRequest.put("direction", direction);
        laserRequest.put("range", range);

        response = request.body(laserRequest).post("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser");
    }

    @Then("the laser should fire successfully")
    public void theLaserShouldFireSuccessfully() {
        response.then().statusCode(200);
    }
}
