package za.co.sww.rwars.steps;

import io.cucumber.java.Before;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import za.co.sww.rwars.backend.service.BattleService;

/**
 * Quarkus test resource for Cucumber tests.
 * This class is responsible for starting the Quarkus application before running the tests.
 */
@QuarkusTest
public class CucumberQuarkusTestResource {

    @Inject
    BattleService battleService;

    @BeforeAll
    public static void setupClass() {
        // Set the base URI for RestAssured
        RestAssured.baseURI = "http://localhost:8081";
    }

    @Before
    public void setup() {
        // Reset the battle service before each test
        if (battleService != null) {
            battleService.resetBattle();
        }
    }

    @AfterAll
    public static void teardownClass() {
        // Clean up resources if needed
    }
}