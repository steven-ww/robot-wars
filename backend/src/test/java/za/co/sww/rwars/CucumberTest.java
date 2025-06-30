package za.co.sww.rwars;

import io.quarkiverse.cucumber.CucumberQuarkusTest;

/**
 * Test runner for Cucumber tests.
 * Using CucumberQuarkusTest to bootstrap Cucumber in the Quarkus project.
 */
//@Suite
//@IncludeEngines("cucumber")
//@SelectClasspathResource("features")
//@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "za.co.sww.rwars.steps")
//@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class CucumberTest extends CucumberQuarkusTest {
}
