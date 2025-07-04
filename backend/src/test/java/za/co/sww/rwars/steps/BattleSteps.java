package za.co.sww.rwars.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import io.quarkus.test.junit.QuarkusTest;
import za.co.sww.rwars.backend.service.BattleService;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
public class BattleSteps {

    @Inject
    private BattleService battleService;

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

    @When("I create a new battle with name {string}")
    public void iCreateANewBattleWithName(String battleName) {
        Map<String, Object> battleRequest = new HashMap<>();
        battleRequest.put("name", battleName);

        response = request.body(battleRequest).post("/api/battles");
    }

    @When("I create a new battle with name {string} and dimensions {int}x{int}")
    public void iCreateANewBattleWithNameAndDimensions(String battleName, int width, int height) {
        Map<String, Object> battleRequest = new HashMap<>();
        battleRequest.put("name", battleName);
        battleRequest.put("width", width);
        battleRequest.put("height", height);

        response = request.body(battleRequest).post("/api/battles");
    }

    @When("I attempt to create a battle with name {string} and dimensions {int}x{int}")
    public void iAttemptToCreateABattleWithNameAndDimensions(String battleName, int width, int height) {
        Map<String, Object> battleRequest = new HashMap<>();
        battleRequest.put("name", battleName);
        battleRequest.put("width", width);
        battleRequest.put("height", height);

        response = request.body(battleRequest).post("/api/battles");
    }

    @Then("a battle with the name {string} should be created")
    public void aBattleWithTheNameShouldBeCreated(String battleName) {
        response.then().statusCode(200);
        response.then().body("name", Matchers.equalTo(battleName));

        // Store the battle ID for later use
        battleId = response.jsonPath().getString("id");
        Assertions.assertNotNull(battleId);
        Assertions.assertFalse(battleId.isEmpty());
    }

    @And("the battle should have an arena with the default size from server configuration")
    public void theBattleShouldHaveAnArenaWithTheDefaultSizeFromServerConfiguration() {
        // We don't hardcode the default values here, just check that they're present and positive
        response.then().body("arenaWidth", Matchers.greaterThan(0));
        response.then().body("arenaHeight", Matchers.greaterThan(0));
    }

    @And("I should receive the battle details including id, name, and arena dimensions")
    public void iShouldReceiveTheBattleDetailsIncludingIdNameAndArenaDimensions() {
        response.then().body("id", Matchers.notNullValue());
        response.then().body("name", Matchers.notNullValue());
        response.then().body("arenaWidth", Matchers.notNullValue());
        response.then().body("arenaHeight", Matchers.notNullValue());
    }

    @And("the battle should have an arena with dimensions {int}x{int}")
    public void theBattleShouldHaveAnArenaWithDimensions(int width, int height) {
        response.then().body("arenaWidth", Matchers.equalTo(width));
        response.then().body("arenaHeight", Matchers.equalTo(height));
    }

    @Then("I should receive an error indicating the arena size is too small")
    public void iShouldReceiveAnErrorIndicatingTheArenaSizeIsTooSmall() {
        response.then().statusCode(400); // Bad Request
        response.then().body("message", Matchers.containsString("Arena dimensions must be at least"));
    }

    @And("the minimum arena size should be {int}x{int}")
    public void theMinimumArenaSizeShouldBe(int minWidth, int minHeight) {
        response.then().body("message", Matchers.containsString(minWidth + "x" + minHeight));
    }

    @Then("I should receive an error indicating the arena size is too large")
    public void iShouldReceiveAnErrorIndicatingTheArenaSizeIsTooLarge() {
        response.then().statusCode(400); // Bad Request
        response.then().body("message", Matchers.containsString("Arena dimensions must be at most"));
    }

    @And("the maximum arena size should be {int}x{int}")
    public void theMaximumArenaSizeShouldBe(int maxWidth, int maxHeight) {
        response.then().body("message", Matchers.containsString(maxWidth + "x" + maxHeight));
    }
}
