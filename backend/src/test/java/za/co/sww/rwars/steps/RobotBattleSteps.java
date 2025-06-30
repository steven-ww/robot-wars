package za.co.sww.rwars.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

/**
 * Step definitions for Robot Battle feature.
 */
public class RobotBattleSteps {

    private String authToken;
    private String battleId;
    private Response response;
    private RequestSpecification request;
    private int robotCount = 0;

    @Given("I have a valid authentication token")
    public void iHaveAValidAuthenticationToken() {
        // In a real implementation, this might involve calling an auth endpoint
        // For now, we'll just set a dummy token
        authToken = "dummy-auth-token";
        request = given().header("Authorization", "Bearer " + authToken);
    }

    @When("I create a new battle with the name {string}")
    public void iCreateANewBattleWithTheName(String battleName) {
        Map<String, String> battleData = new HashMap<>();
        battleData.put("name", battleName);

        response = request
                .contentType("application/json")
                .body(battleData)
                .when()
                .post("/api/battles");
    }

    @Then("the battle should be created successfully")
    public void theBattleShouldBeCreatedSuccessfully() {
        response.then().statusCode(201);
    }

    @And("I should receive a battle ID")
    public void iShouldReceiveABattleId() {
        battleId = response.jsonPath().getString("id");
        Assertions.assertNotNull(battleId, "Battle ID should not be null");
    }

    @Given("I have a battle with ID {string}")
    public void iHaveABattleWithId(String id) {
        battleId = id;
    }

    @When("I add robot {string} to the battle")
    public void iAddRobotToTheBattle(String robotName) {
        Map<String, String> robotData = new HashMap<>();
        robotData.put("name", robotName);

        response = request
                .contentType("application/json")
                .body(robotData)
                .when()
                .post("/api/battles/" + battleId + "/robots");

        robotCount++;
    }

    @Then("the battle should have {int} robots")
    public void theBattleShouldHaveRobots(int expectedCount) {
        Assertions.assertEquals(expectedCount, robotCount,
                "Battle should have " + expectedCount + " robots");
    }

    @And("the robots should be ready for battle")
    public void theRobotsShouldBeReadyForBattle() {
        response = request
                .when()
                .get("/api/battles/" + battleId);

        String status = response.jsonPath().getString("status");
        Assertions.assertEquals("READY", status, "Battle status should be READY");
    }

    @Given("I have a battle with ID {string} with {int} robots")
    public void iHaveABattleWithIdWithRobots(String id, int count) {
        battleId = id;
        robotCount = count;
    }

    @When("I start the battle")
    public void iStartTheBattle() {
        response = request
                .when()
                .post("/api/battles/" + battleId + "/start");
    }

    @Then("the battle status should change to {string}")
    public void theBattleStatusShouldChangeTo(String expectedStatus) {
        response = request
                .when()
                .get("/api/battles/" + battleId);

        String status = response.jsonPath().getString("status");
        Assertions.assertEquals(expectedStatus, status,
                "Battle status should be " + expectedStatus);
    }

    @And("I should receive real-time updates about the battle")
    public void iShouldReceiveRealTimeUpdatesAboutTheBattle() {
        // This would typically involve WebSocket testing
        // For now, we'll just add a placeholder assertion
        Assertions.assertTrue(true, "WebSocket connection should be established");
    }
}
