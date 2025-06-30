package za.co.sww.rwars.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
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
        // Use the configured Quarkus test URI
        RestAssured.port = 8081;
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
        // First register a robot to create a battle
        Map<String, String> robot = new HashMap<>();
        robot.put("name", "FirstRobot");
        Response registerResponse = request.body(robot).post("/api/robots/register");
        String firstBattleId = registerResponse.jsonPath().getString("battleId");

        // Then start the battle
        request.post("/api/robots/battle/" + firstBattleId + "/start");
    }

    @Then("I should receive an error code and description reflecting that I can't join an in progress battle")
    public void iShouldReceiveAnErrorCodeAndDescription() {
        response.then().statusCode(409); // Conflict
        response.then().body("message", Matchers.containsString("Cannot join a battle in progress"));
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

        request.body(robot).post("/api/robots/register");
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
        battleId = response1.jsonPath().getString("battleId");

        // Register second robot
        Map<String, String> robot2 = new HashMap<>();
        robot2.put("name", "Robot2");
        request.body(robot2).post("/api/robots/register");
    }

    @And("the battle administrator has started the battle")
    public void theBattleAdministratorHasStartedTheBattle() {
        request.post("/api/robots/battle/" + battleId + "/start");
    }

    @Then("the battle status should be to {string}")
    public void theBattleStatusShouldBeTo(String status) {
        response.then().body("state", Matchers.equalTo(status));
    }
}
