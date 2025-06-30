package za.co.sww.rwars.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import za.co.sww.rwars.backend.service.BattleService;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class RobotSteps {

    @Inject
    BattleService battleService;

    private Response response;
    private String battleId;
    private RequestSpecification request;

    @Before
    public void setup() {
        request = RestAssured.given();
        request.contentType(ContentType.JSON);

        // Reset the battle service before each test
        if (battleService != null) {
            battleService.resetBattle();
        }
    }

    @When("I register my Robot supplying it's name")
    public void iRegisterMyRobotSupplyingItsName() {
        Map<String, String> robot = new HashMap<>();
        robot.put("name", "TestRobot");

        response = request.body(robot).post("/api/robots/register");
    }

    @And("there is no battle currently in progress")
    public void thereIsNoBattleCurrentlyInProgress() {
        // This is handled by the service, which creates a new battle if none exists
        // No specific action needed here
    }

    @Then("a battle id should be generated for my robot")
    public void aBattleIdShouldBeGeneratedForMyRobot() {
        response.then().statusCode(200);
        battleId = response.jsonPath().getString("battleId");
        Assertions.assertNotNull(battleId);
        Assertions.assertFalse(battleId.isEmpty());
    }

    @And("I should receive the battle id")
    public void iShouldReceiveTheBattleId() {
        response.then().body("battleId", Matchers.notNullValue());
    }

    @And("there is a battle currently in progress")
    public void thereIsABattleCurrentlyInProgress() {
        // Reset the battle service to ensure a clean state
        if (battleService != null) {
            battleService.resetBattle();
        }
        
        // First register two robots to create a battle and make it ready
        Map<String, String> robot1 = new HashMap<>();
        robot1.put("name", "FirstRobot");
        Response registerResponse1 = request.body(robot1).post("/api/robots/register");
        String firstBattleId = registerResponse1.jsonPath().getString("battleId");

        // Register second robot to make battle ready
        Map<String, String> robot2 = new HashMap<>();
        robot2.put("name", "SecondRobot");
        Response registerResponse2 = request.body(robot2).post("/api/robots/register");
        registerResponse2.then().statusCode(200);

        // Then start the battle to make it IN_PROGRESS
        Response startResponse = request.post("/api/robots/battle/" + firstBattleId + "/start");
        startResponse.then().statusCode(200);
        
        // Verify the battle is actually started
        Response statusResponse = request.get("/api/robots/battle/" + firstBattleId);
        statusResponse.then().statusCode(200).body("state", Matchers.equalTo("IN_PROGRESS"));
        
        // Store the battle status response to ensure it's available for subsequent steps
        response = statusResponse;
    }

    @Then("I should receive an error code and description reflecting that I can't join an in progress battle")
    public void iShouldReceiveAnErrorCodeAndDescription() {
        // Now try to register a robot when a battle is already in progress
        Map<String, String> robot = new HashMap<>();
        robot.put("name", "TestRobot");
        
        Response registrationResponse = request.body(robot).post("/api/robots/register");
        registrationResponse.then().statusCode(409); // Conflict
        registrationResponse.then().body("message", Matchers.containsString("Cannot join a battle in progress"));
    }

    @Given("I have registered my robot")
    public void iHaveRegisteredMyRobot() {
        Map<String, String> robot = new HashMap<>();
        robot.put("name", "MyRobot");

        response = request.body(robot).post("/api/robots/register");
        response.then().statusCode(200);
        battleId = response.jsonPath().getString("battleId");
    }

    @And("the battle has not yet started")
    public void theBattleHasNotYetStarted() {
        // No specific action needed, the battle is not started by default
    }

    @When("I check the status of the battle supplying my battle id")
    public void iCheckTheStatusOfTheBattle() {
        response = request.get("/api/robots/battle/" + battleId);
    }

    @And("it's a valid battle id")
    public void itsAValidBattleId() {
        response.then().statusCode(200);
    }

    @Then("the battle should have {int} robots")
    public void theBattleShouldHaveRobots(int count) {
        response.then().body("robotCount", Matchers.equalTo(count));
    }

    @And("the battle state should {string}")
    public void theBattleStateShouldBe(String state) {
        response.then().body("state", Matchers.equalTo(state));
    }

    @And("at least one other robot has registered")
    public void atLeastOneOtherRobotHasRegistered() {
        Map<String, String> robot = new HashMap<>();
        robot.put("name", "OtherRobot");

        // Store the response to ensure it's available if needed
        Response otherRobotResponse = request.body(robot).post("/api/robots/register");
        // We don't overwrite the main response here as it might be needed by other steps
        // But we ensure the request completes successfully
        otherRobotResponse.then().statusCode(200);
    }

    @Then("the battle should have {int} or more robots")
    public void theBattleShouldHaveOrMoreRobots(int minCount) {
        int actualCount = response.jsonPath().getInt("robotCount");
        Assertions.assertTrue(actualCount >= minCount, 
                "Expected at least " + minCount + " robots, but found " + actualCount);
    }

    @Given("At least two robots have registered for the battle")
    public void atLeastTwoRobotsHaveRegisteredForTheBattle() {
        // Register first robot
        Map<String, String> robot1 = new HashMap<>();
        robot1.put("name", "Robot1");
        Response response1 = request.body(robot1).post("/api/robots/register");
        response1.then().statusCode(200);
        battleId = response1.jsonPath().getString("battleId");

        // Register second robot
        Map<String, String> robot2 = new HashMap<>();
        robot2.put("name", "Robot2");
        Response response2 = request.body(robot2).post("/api/robots/register");
        response2.then().statusCode(200);

        // We don't overwrite the main response here as it will be set in subsequent steps
    }

    @And("the battle administrator has started the battle")
    public void theBattleAdministratorHasStartedTheBattle() {
        // Store the response to ensure it's available for subsequent steps
        response = request.post("/api/robots/battle/" + battleId + "/start");
        response.then().statusCode(200);
    }

    @Then("the battle status should be to {string}")
    public void theBattleStatusShouldBeTo(String status) {
        response.then().body("state", Matchers.equalTo(status));
    }
}
