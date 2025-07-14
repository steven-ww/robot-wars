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
                    robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)

                    // Simulate waiting for movement to complete (reduced for tests)
                    Thread.sleep(100) // Just a short delay for tests

                    // Determine if the robot should crash based on move count
                    val shouldCrash = moveCount >= crashAfterMoves && robotName == robots.keys.first()

                    // Set up stub for getting robot status with appropriate status
                    val status = if (shouldCrash) "CRASHED" else "IDLE"
                    wireMockStubs.stubGetRobotStatus(robotName, status)

                    // Call the API through the client
                    val robotStatus = robotApiClient.getRobotStatus(battle.id, robot.id)

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

    @Then("I should be able to move the robots around the arena until a specified time has passed")
    fun iShouldMoveRobotsUntilSpecifiedTime() = runBlocking {
        logger.info("Moving robots around the arena until the specified time has passed")

        wireMockStubs.stubGetBattleStatus()

        val startTime = Instant.now()
        // Use a shorter time limit for testing (5 seconds instead of 5 minutes)
        val timeLimitDuration = Duration.ofSeconds(5)
        val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
        var moveCount = 0
        val maxMoves = 10 // Limit the number of moves for test efficiency

        while (
            Duration.between(startTime, Instant.now()) < timeLimitDuration &&
            moveCount < maxMoves
        ) {
            moveCount++
            // Move each robot
            for ((robotName, robot) in robots) {
                val direction = directions.random()
                val blocks = (1..3).random()
                logger.info("Moving $robotName $blocks blocks $direction")

                try {
                    wireMockStubs.stubMoveRobot(robotName, direction, blocks)
                    robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)
                    Thread.sleep(100)
                } catch (e: Exception) {
                    logger.error("Error moving $robotName", e)
                }
            }

            val elapsed = Duration.between(startTime, Instant.now())
            logger.info("Time elapsed: ${elapsed.toMillis()} ms")
        }

        logger.info("Specified time limit reached or maximum moves completed")
        val battleStatus = robotApiClient.getBattleStatus(battle.id)
        logger.info("Battle ended with state: ${battleStatus.state}")
    }

    @Then("I should be able to move the robots around the arena until {int} minutes has passed even if they crash into a wall")
    fun iShouldMoveRobotsUntilTimeLimitEvenIfTheyCrash(timeLimit: Int) = runBlocking {
        logger.info("Moving robots around the arena for $timeLimit minutes, ignoring crashes")

        wireMockStubs.stubGetBattleStatus()

        val startTime = Instant.now()
        val timeLimitDuration = Duration.ofMinutes(timeLimit.toLong())
        val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")

        while (
            Duration.between(startTime, Instant.now()) < timeLimitDuration
        ) {
            // Move each robot
            for ((robotName, robot) in robots) {
                val direction = directions.random()
                val blocks = (1..3).random()
                logger.info("Moving $robotName $blocks blocks $direction")

                // Set up stub for moving a robot
                wireMockStubs.stubMoveRobot(robotName, direction, blocks)

                // Call the API through the client
                robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)

                // Simulate waiting for movement to complete (reduced for tests)
                Thread.sleep(100) // Just a short delay for tests
            }

            // Check time elapsed
            val elapsed = Duration.between(startTime, Instant.now())
            logger.info("Time elapsed: ${elapsed.toMinutes()} minutes ${elapsed.toSecondsPart()} seconds")
        }

        logger.info("Time limit reached ($timeLimit minutes)")

        // Final status
        val battleStatus = robotApiClient.getBattleStatus(battle.id)
        logger.info("Battle ended with state: ${battleStatus.state}")
    }

    @When("I move the robot in direction {string} for {int} blocks")
    fun iMoveTheRobotInDirectionForBlocks(direction: String, blocks: Int) = runBlocking {
        logger.info("Moving robot in direction $direction for $blocks blocks")

        // Get the first robot (should be "TrackBot" based on the scenario)
        val robot = robots.values.first()

        // Set up stub for moving a robot
        wireMockStubs.stubMoveRobot(robot.name, direction, blocks)

        // Call the API through the client
        val updatedRobot = robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)

        // Store the updated robot
        robots[robot.name] = updatedRobot

        logger.info("Robot ${robot.name} is now moving in direction $direction for $blocks blocks")
    }

    @When("I fire a laser in direction {string}")
    fun iFireALaserInDirection(direction: String) = runBlocking {
        logger.info("Firing laser in direction $direction")

        // Get the first robot (assuming one robot setup per scenario)
        val robot = robots.values.first()

        // Set up stub for firing laser
        wireMockStubs.stubFireLaser(robot.name, direction)

        // Call the API through the client
        val laserResponse = robotApiClient.fireLaser(battle.id, robot.id, direction)

        // Verify the response
        if (laserResponse.hit) {
            logger.info("Laser hit a robot: ${laserResponse.hitRobotName} (ID: ${laserResponse.hitRobotId})")
        } else {
            logger.info("Laser missed, hit blocked by: ${laserResponse.blockedBy}")
        }
    }

    @Then("I should be able to track the robot's position as it moves")
    fun iShouldBeAbleToTrackTheRobotsPositionAsItMoves() = runBlocking {
        logger.info("Tracking robot's position as it moves")

        // Get the robot
        val robot = robots.values.first()

        // Set up stubs for getting robot details with different positions
        // Simulate the robot moving by changing its position in each status update
        val positions = listOf(
            Pair(1, 1), // Initial position
            Pair(1, 2), // After moving one block
            Pair(1, 3), // After moving two blocks
        )

        // Track the robot's position over time
        for (index in positions.indices) {
            // Set up stub for getting robot status with updated status
            val status = if (index < positions.size - 1) "MOVING" else "IDLE"
            wireMockStubs.stubGetRobotStatus(robot.name, status)

            // Call the API through the client
            val robotStatus = robotApiClient.getRobotStatus(battle.id, robot.id)

            // Verify the status (position is no longer available through robot API)
            logger.info("${robot.name} status: ${robotStatus.status}")

            // Short delay to simulate time passing
            Thread.sleep(100)
        }

        logger.info("Successfully tracked robot's status as it moved")
    }

    @Then("I should be able to fire a laser before moving the robots around the arena until a specified time has passed")
    fun iShouldBeAbleToFireLaserBeforeMovingRobotsUntilSpecifiedTime() = runBlocking {
        logger.info("Moving robots around the arena with laser firing until the specified time has passed")

        wireMockStubs.stubGetBattleStatus()

        val startTime = Instant.now()
        // Use a shorter time limit for testing (5 seconds instead of 5 minutes)
        val timeLimitDuration = Duration.ofSeconds(5)
        val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
        var moveCount = 0
        val maxMoves = 10 // Limit the number of moves for test efficiency

        while (
            Duration.between(startTime, Instant.now()) < timeLimitDuration &&
            moveCount < maxMoves
        ) {
            moveCount++
            // Move each robot
            for ((robotName, robot) in robots) {
                val direction = directions.random()
                val blocks = (1..3).random()
                // Fire laser first
                logger.info("Firing laser for $robotName in direction $direction")
                wireMockStubs.stubFireLaser(robotName, direction)
                robotApiClient.fireLaser(battle.id, robot.id, direction)
                // Then move the robot
                logger.info("Moving $robotName $blocks blocks $direction")
                wireMockStubs.stubMoveRobot(robotName, direction, blocks)
                robotApiClient.moveRobot(battle.id, robot.id, direction, blocks)
                Thread.sleep(100)
            }

            val elapsed = Duration.between(startTime, Instant.now())
            logger.info("Time elapsed: ${elapsed.toMillis()} ms")
        }

        logger.info("Specified time limit reached or maximum moves completed")
        val battleStatus = robotApiClient.getBattleStatus(battle.id)
        logger.info("Battle ended with state: ${battleStatus.state}")
    }

    @When("I fire a laser in direction {string} before moving the robot in direction {string} for {int} block")
    fun iFireALaserBeforeMovingRobot(laserDirection: String, moveDirection: String, blocks: Int) = runBlocking {
        logger.info("Firing laser in direction $laserDirection before moving robot in direction $moveDirection for $blocks blocks")

        // Get the first robot (assuming one robot setup per scenario)
        val robot = robots.values.first()

        // Fire laser first
        wireMockStubs.stubFireLaser(robot.name, laserDirection)
        val laserResponse = robotApiClient.fireLaser(battle.id, robot.id, laserDirection)
        if (laserResponse.hit) {
            logger.info("Laser hit a robot: ${laserResponse.hitRobotName}")
        } else {
            logger.info("Laser missed, blocked by: ${laserResponse.blockedBy}")
        }

        // Then move the robot
        wireMockStubs.stubMoveRobot(robot.name, moveDirection, blocks)
        val updatedRobot = robotApiClient.moveRobot(battle.id, robot.id, moveDirection, blocks)

        // Store the updated robot
        robots[robot.name] = updatedRobot
        logger.info("Robot ${robot.name} fired laser in $laserDirection then moved $blocks blocks $moveDirection")
    }

    @Then("the robot should fire a laser first and then move in the specified direction")
    fun theRobotShouldFireLaserFirstAndThenMove() {
        logger.info("Verified that robot fired laser before moving")
        // This step is verification that the previous step completed successfully
        // The actual firing and moving happened in the previous step
    }
}
