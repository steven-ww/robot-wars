package za.co.sww.rwars.robodemo.steps

import io.cucumber.java.After
import io.cucumber.java.Before
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
import za.co.sww.rwars.robodemo.wiremock.WireMockExtension
import za.co.sww.rwars.robodemo.wiremock.WireMockStubs
import java.time.Duration
import java.time.Instant

/**
 * Step definitions for the Robot Battle feature.
 */
class RobotBattleSteps {
    private val logger = LoggerFactory.getLogger(RobotBattleSteps::class.java)

    // WireMock setup
    private val wireMockExtension = WireMockExtension()
    private val wireMockStubs = WireMockStubs()
    private lateinit var baseUrl: String
    private lateinit var battleApiClient: BattleApiClient
    private lateinit var robotApiClient: RobotApiClient

    private lateinit var battle: Battle
    private val robots = mutableMapOf<String, Robot>()

    @Before
    fun setup() {
        // Start WireMock server
        baseUrl = wireMockExtension.start()
        logger.info("Started WireMock server at $baseUrl")

        // Initialize API clients with WireMock URL
        battleApiClient = BattleApiClient(baseUrl)
        robotApiClient = RobotApiClient(baseUrl)
    }

    @After
    fun tearDown() {
        // Stop WireMock server
        wireMockExtension.stop()
        logger.info("Stopped WireMock server")
    }

    @Given("the backend service is running")
    fun theBackendServiceIsRunning() {
        // With WireMock, we don't need to check if the real backend is running
        logger.info("Using WireMock server at $baseUrl instead of real backend")
    }

    @When("I create a new battle")
    fun iCreateANewBattle() = runBlocking {
        logger.info("Creating a new battle")

        // Set up stub for creating a battle
        battle = wireMockStubs.stubCreateBattle("Test Battle")

        // Call the API through the client
        val createdBattle = battleApiClient.createBattle("Test Battle")

        // Verify the response
        assertNotNull(createdBattle.id, "Battle ID should not be null")
        assertEquals("Test Battle", createdBattle.name, "Battle name should match")
        assertEquals(battle.id, createdBattle.id, "Battle ID should match the stubbed ID")

        // Update the battle reference
        battle = createdBattle

        logger.info("Battle created with ID: ${battle.id}")
    }

    @When("I register a robot with the name {string}")
    fun iRegisterARobotWithTheName(robotName: String) = runBlocking {
        logger.info("Registering robot with name: $robotName")

        // Set up stub for registering a robot
        val stubbedRobot = wireMockStubs.stubRegisterRobot(robotName)

        // Call the API through the client
        val robot = robotApiClient.registerRobot(robotName)

        // Verify the response
        assertNotNull(robot.id, "Robot ID should not be null")
        assertEquals(robotName, robot.name, "Robot name should match")
        assertEquals(battle.id, robot.battleId, "Robot should be registered to the current battle")
        assertEquals(stubbedRobot.id, robot.id, "Robot ID should match the stubbed ID")

        // Store the robot for later use
        robots[robotName] = robot

        logger.info("Robot registered with ID: ${robot.id}")
    }

    @When("I start the battle")
    fun iStartTheBattle() = runBlocking {
        logger.info("Starting the battle")

        // Set up stub for starting a battle
        val stubbedBattle = wireMockStubs.stubStartBattle()

        // Call the API through the client
        val startedBattle = robotApiClient.startBattle(battle.id)

        // Verify the response
        assertEquals("IN_PROGRESS", startedBattle.state, "Battle state should be IN_PROGRESS")
        assertEquals(stubbedBattle.id, startedBattle.id, "Battle ID should match the stubbed ID")

        // Update the battle reference
        battle = startedBattle

        logger.info("Battle started with state: ${battle.state}")
    }

    @Then("I should be able to move the robots around the arena until one crashes into a wall or {int} minutes has passed")
    fun iShouldBeAbleToMoveTheRobotsAroundTheArena(timeLimit: Int) = runBlocking {
        logger.info("Moving robots around the arena for up to $timeLimit minutes")

        // Set up stub for getting battle status
        wireMockStubs.stubGetBattleStatus()

        val startTime = Instant.now()
        val timeLimitDuration = Duration.ofMinutes(timeLimit.toLong())
        val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
        val robotCrashed = mutableMapOf<String, Boolean>()

        // Initialize crash status for each robot
        robots.keys.forEach { robotCrashed[it] = false }

        // Simulate robot movement until one crashes or time limit is reached
        // We'll make one robot crash after a few moves to demonstrate the functionality
        var moveCount = 0
        val crashAfterMoves = 3 // Crash after 3 moves

        while (
            robotCrashed.values.all { !it } &&
            Duration.between(startTime, Instant.now()) < timeLimitDuration &&
            moveCount < 5 // Limit to 5 moves for test efficiency
        ) {
            moveCount++

            // Move each robot
            for ((robotName, robot) in robots) {
                if (robotCrashed[robotName] == true) continue

                val direction = directions.random()
                val blocks = (1..3).random()
                logger.info("Moving $robotName $blocks blocks $direction")

                try {
                    // Set up stub for moving a robot
                    wireMockStubs.stubMoveRobot(robotName, direction, blocks)

                    // Call the API through the client
                    val updatedRobot = robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)

                    // Simulate waiting for movement to complete (reduced for tests)
                    Thread.sleep(100) // Just a short delay for tests

                    // Determine if the robot should crash based on move count
                    val shouldCrash = moveCount >= crashAfterMoves && robotName == robots.keys.first()

                    // Set up stub for getting robot details with appropriate status
                    val status = if (shouldCrash) "CRASHED" else "IDLE"
                    wireMockStubs.stubGetRobotDetails(robotName, status)

                    // Call the API through the client
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
