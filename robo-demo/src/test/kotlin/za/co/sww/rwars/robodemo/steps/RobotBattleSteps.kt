package za.co.sww.rwars.robodemo.steps

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.slf4j.LoggerFactory
import za.co.sww.rwars.robodemo.api.BattleApiClient
import za.co.sww.rwars.robodemo.api.RobotApiClient
import za.co.sww.rwars.robodemo.model.Battle
import za.co.sww.rwars.robodemo.model.Robot
import java.time.Duration
import java.time.Instant

/**
 * Step definitions for the Robot Battle feature.
 */
class RobotBattleSteps {
    private val logger = LoggerFactory.getLogger(RobotBattleSteps::class.java)
    private val baseUrl = "http://localhost:8080"
    private val battleApiClient = BattleApiClient(baseUrl)
    private val robotApiClient = RobotApiClient(baseUrl)

    private lateinit var battle: Battle
    private val robots = mutableMapOf<String, Robot>()

    @Given("the backend service is running")
    fun theBackendServiceIsRunning() {
        // This step is a placeholder for checking if the backend service is running
        // In a real implementation, we would check if the service is accessible
        logger.info("Checking if backend service is running at $baseUrl")
        // For now, we'll just assume it's running
    }

    @When("I create a new battle")
    fun iCreateANewBattle() = runBlocking {
        logger.info("Creating a new battle")
        battle = battleApiClient.createBattle("Test Battle")
        assertNotNull(battle.id, "Battle ID should not be null")
        assertEquals("Test Battle", battle.name, "Battle name should match")
        logger.info("Battle created with ID: ${battle.id}")
    }

    @When("I register a robot with the name {string}")
    fun iRegisterARobotWithTheName(robotName: String) = runBlocking {
        logger.info("Registering robot with name: $robotName")
        val robot = robotApiClient.registerRobot(robotName)
        assertNotNull(robot.id, "Robot ID should not be null")
        assertEquals(robotName, robot.name, "Robot name should match")
        assertEquals(battle.id, robot.battleId, "Robot should be registered to the current battle")
        robots[robotName] = robot
        logger.info("Robot registered with ID: ${robot.id}")
    }

    @When("I start the battle")
    fun iStartTheBattle() = runBlocking {
        logger.info("Starting the battle")
        val startedBattle = robotApiClient.startBattle(battle.id)
        assertEquals("IN_PROGRESS", startedBattle.state, "Battle state should be IN_PROGRESS")
        battle = startedBattle
        logger.info("Battle started with state: ${battle.state}")
    }

    @Then("I should be able to move the robots around the arena until one crashes into a wall or {int} minutes has passed")
    fun iShouldBeAbleToMoveTheRobotsAroundTheArena(timeLimit: Int) = runBlocking {
        logger.info("Moving robots around the arena for up to $timeLimit minutes")

        val startTime = Instant.now()
        val timeLimitDuration = Duration.ofMinutes(timeLimit.toLong())
        val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
        val robotCrashed = mutableMapOf<String, Boolean>()

        // Initialize crash status for each robot
        robots.keys.forEach { robotCrashed[it] = false }

        // Move robots until one crashes or time limit is reached
        while (
            robotCrashed.values.all { !it } &&
            Duration.between(startTime, Instant.now()) < timeLimitDuration
        ) {
            // Move each robot
            for ((robotName, robot) in robots) {
                if (robotCrashed[robotName] == true) continue

                val direction = directions.random()
                val blocks = (1..3).random()
                logger.info("Moving $robotName $blocks blocks $direction")

                try {
                    val updatedRobot = robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)
                    // Wait for movement to complete
                    Thread.sleep(blocks * 1000L)

                    // Check if robot crashed
                    val robotStatus = robotApiClient.getRobotDetails(battle.id, robot.id)
                    if (robotStatus.status == "CRASHED") {
                        logger.info("$robotName crashed into a wall!")
                        robotCrashed[robotName] = true
                    }
                } catch (e: Exception) {
                    logger.error("Error moving $robotName", e)
                    robotCrashed[robotName] = true
                }
            }

            // Check time elapsed
            val elapsed = Duration.between(startTime, Instant.now())
            logger.info("Time elapsed: ${elapsed.toMinutes()} minutes ${elapsed.toSecondsPart()} seconds")

            if (elapsed > timeLimitDuration) {
                logger.info("Time limit reached ($timeLimit minutes)")
                break
            }
        }

        // Final status
        val battleStatus = robotApiClient.getBattleStatus(battle.id)
        logger.info("Battle ended with state: ${battleStatus.state}")

        robotCrashed.forEach { (robotName, crashed) ->
            if (crashed) {
                logger.info("$robotName crashed into a wall")
            }
        }

        if (robotCrashed.values.none { it }) {
            logger.info("Time limit reached without any crashes")
        }
    }
}
