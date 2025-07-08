package za.co.sww.rwars.robodemo

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import za.co.sww.rwars.robodemo.api.BattleApiClient
import za.co.sww.rwars.robodemo.api.RobotApiClient
import za.co.sww.rwars.robodemo.model.Robot
import java.time.Duration
import java.time.Instant
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

/**
 * Renders the arena with the robots' positions.
 *
 * @param width The width of the arena
 * @param height The height of the arena
 * @param robots The list of robots to display
 */
private fun renderArena(width: Int, height: Int, robots: List<Robot>) {
    logger.info("Rendering arena ($width x $height):")

    // Create a 2D array to represent the arena
    val arena = Array(height) { Array(width) { "." } }

    // Place robots on the arena
    robots.forEachIndexed { _, robot ->
        // Ensure robot position is within arena bounds
        if (robot.positionX in 0 until width && robot.positionY in 0 until height) {
            // Use the first letter of the robot's name as its marker
            arena[robot.positionY][robot.positionX] = robot.name.first().toString()
        }
    }

    // Print the arena
    val horizontalBorder = "+${"-".repeat(width * 2 - 1)}+"
    logger.info(horizontalBorder)

    for (y in 0 until height) {
        val row = arena[y].joinToString(" ")
        logger.info("| $row |")
    }

    logger.info(horizontalBorder)

    // Print legend
    robots.forEach { robot ->
        logger.info("${robot.name.first()} = ${robot.name} at (${robot.positionX}, ${robot.positionY})")
    }
}

/**
 * Data class to hold application configuration parsed from command line arguments.
 */
data class AppConfig(
    val baseUrl: String = "http://localhost:8080",
    val timeLimit: Duration = Duration.ofMinutes(5),
    val stopOnCrash: Boolean = false,
)

/**
 * Parses command line arguments into an AppConfig object.
 * Supports named parameters: --url, --time, --stop-on-crash
 */
fun parseArgs(args: Array<String>): AppConfig {
    var baseUrl = "http://localhost:8080"
    var timeLimit = Duration.ofMinutes(5)
    var stopOnCrash = false

    var i = 0
    while (i < args.size) {
        when {
            args[i] == "--url" && i + 1 < args.size -> {
                baseUrl = args[i + 1]
                i += 2
            }
            args[i] == "--time" && i + 1 < args.size -> {
                val timeLimitArg = args[i + 1]
                timeLimit = if (timeLimitArg.endsWith("s")) {
                    Duration.ofSeconds(timeLimitArg.dropLast(1).toLong())
                } else if (timeLimitArg.endsWith("m")) {
                    Duration.ofMinutes(timeLimitArg.dropLast(1).toLong())
                } else {
                    Duration.ofMinutes(timeLimitArg.toLong())
                }
                i += 2
            }
            args[i] == "--stop-on-crash" -> {
                stopOnCrash = true
                i += 1
            }
            args[i].startsWith("--") -> {
                logger.warn("Unknown parameter: ${args[i]}")
                i += 1
            }
            else -> {
                i += 1
            }
        }
    }

    return AppConfig(baseUrl, timeLimit, stopOnCrash)
}

/**
 * Main entry point for the robo-demo application.
 * This application demonstrates the use of the Robot Wars API to:
 * 1. Create a new battle
 * 2. Register robots
 * 3. Start the battle
 * 4. Move robots around until one crashes or time limit is reached
 *
 * Supported parameters:
 * --url <baseUrl> : Base URL for the Robot Wars API (default: http://localhost:8080)
 * --time <time>   : Time limit for the battle (e.g., 5m, 30s) (default: 5m)
 * --stop-on-crash : Stop the demo when the first robot crashes (default: false)
 */
fun main(args: Array<String>) = runBlocking {
    logger.info("Starting Robot Wars Demo")

    val config = parseArgs(args)
    logger.info("Using base URL: ${config.baseUrl}")
    logger.info("Time limit set to: ${config.timeLimit}")
    logger.info("Stop on crash: ${config.stopOnCrash}")

    val battleApiClient = BattleApiClient(config.baseUrl)
    val robotApiClient = RobotApiClient(config.baseUrl)

    try {
        // Create a new battle with a 20x20 arena
        logger.info("Creating a new battle with a 20x20 arena")
        val battle = battleApiClient.createBattle("Demo Battle", 20, 20)
        logger.info("Battle created: ${battle.id} - ${battle.name} - Arena size: ${battle.arenaWidth}x${battle.arenaHeight}")

        // Register robots
        logger.info("Registering robot 'Restro'")
        val restro = robotApiClient.registerRobot("Restro")
        logger.info("Robot registered: ${restro.id} - ${restro.name}")

        logger.info("Registering robot 'ReqBot'")
        val reqBot = robotApiClient.registerRobot("ReqBot")
        logger.info("Robot registered: ${reqBot.id} - ${reqBot.name}")

        // Start the battle
        logger.info("Starting the battle")
        val startedBattle = robotApiClient.startBattle(battle.id)
        logger.info("Battle started: ${startedBattle.state}")

        // Get robot details to display initial positions
        val restroDetails = robotApiClient.getRobotDetails(battle.id, restro.id)
        val reqBotDetails = robotApiClient.getRobotDetails(battle.id, reqBot.id)

        logger.info("Initial position of ${restroDetails.name}: (${restroDetails.positionX}, ${restroDetails.positionY})")
        logger.info("Initial position of ${reqBotDetails.name}: (${reqBotDetails.positionX}, ${reqBotDetails.positionY})")

        // Render the arena showing the initial positions of the robots
        renderArena(battle.arenaWidth, battle.arenaHeight, listOf(restroDetails, reqBotDetails))

        // Move robots until one crashes or time limit is reached
        moveRobotsUntilCrashOrTimeout(robotApiClient, battle.id, restro, reqBot, config.timeLimit, config.stopOnCrash)

        logger.info("Demo completed successfully")
    } catch (e: Exception) {
        logger.error("Error during demo execution", e)
        exitProcess(1)
    }
}

/**
 * Tracks robot movement by polling its position during movement and rerendering the arena.
 *
 * @param robotApiClient The API client for interacting with the robot API
 * @param battleId The ID of the battle
 * @param movingRobotId The ID of the robot that is moving
 * @param otherRobotId The ID of the other robot
 * @param blocks The number of blocks the robot is moving
 */
private suspend fun trackRobotMovement(
    robotApiClient: RobotApiClient,
    battleId: String,
    movingRobotId: String,
    otherRobotId: String,
    blocks: Int,
) {
    // Get battle details to know arena dimensions
    val battle = robotApiClient.getBattleStatus(battleId)

    // Poll robot position every 200ms during movement
    // Total movement time is blocks * 1000ms (1 second per block)
    val pollInterval = 200L // milliseconds
    val totalMovementTime = blocks * 1000L
    var elapsedTime = 0L

    while (elapsedTime < totalMovementTime) {
        // Get current position of both robots
        val movingRobot = robotApiClient.getRobotDetails(battleId, movingRobotId)
        val otherRobot = robotApiClient.getRobotDetails(battleId, otherRobotId)

        // Log current position of the moving robot
        logger.info("${movingRobot.name} current position: (${movingRobot.positionX}, ${movingRobot.positionY}), status: ${movingRobot.status}")

        // Rerender the arena with current positions
        renderArena(battle.arenaWidth, battle.arenaHeight, listOf(movingRobot, otherRobot))

        // Wait for the next poll
        Thread.sleep(pollInterval)
        elapsedTime += pollInterval
    }
}

/**
 * Moves robots around the arena until one crashes or the time limit is reached.
 * Periodically retrieves robot status and rerenders the arena.
 */
private suspend fun moveRobotsUntilCrashOrTimeout(
    robotApiClient: RobotApiClient,
    battleId: String,
    robot1: Robot,
    robot2: Robot,
    timeLimit: Duration,
    stopOnCrash: Boolean,
) {
    val startTime = Instant.now()
    val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
    var robot1Crashed = false
    var robot2Crashed = false

    logger.info("Moving robots around the arena")

    while (Duration.between(startTime, Instant.now()) < timeLimit && (!stopOnCrash || (!robot1Crashed && !robot2Crashed))) {
        // Move robot1
        if (!robot1Crashed) {
            val direction1 = directions.random()
            val blocks1 = (1..3).random()
            logger.info("Moving ${robot1.name} $blocks1 blocks $direction1")

            try {
                robotApiClient.moveRobot(battleId, robot1.id, direction1, blocks1)

                // Track robot movement by polling its position during movement
                trackRobotMovement(robotApiClient, battleId, robot1.id, robot2.id, blocks1)

                // Check if robot crashed
                val robotStatus1 = robotApiClient.getRobotDetails(battleId, robot1.id)
                if (robotStatus1.status == "CRASHED") {
                    logger.info("${robot1.name} crashed into a wall!")
                    robot1Crashed = true
                    if (stopOnCrash) return
                }
            } catch (e: Exception) {
                logger.error("Error moving ${robot1.name}", e)
                robot1Crashed = true
                if (stopOnCrash) return
            }
        }

        // Move robot2
        if (!robot2Crashed) {
            val direction2 = directions.random()
            val blocks2 = (1..3).random()
            logger.info("Moving ${robot2.name} $blocks2 blocks $direction2")

            try {
                robotApiClient.moveRobot(battleId, robot2.id, direction2, blocks2)

                // Track robot movement by polling its position during movement
                trackRobotMovement(robotApiClient, battleId, robot2.id, robot1.id, blocks2)

                // Check if robot crashed
                val robotStatus2 = robotApiClient.getRobotDetails(battleId, robot2.id)
                if (robotStatus2.status == "CRASHED") {
                    logger.info("${robot2.name} crashed into a wall!")
                    robot2Crashed = true
                    if (stopOnCrash) return
                }
            } catch (e: Exception) {
                logger.error("Error moving ${robot2.name}", e)
                robot2Crashed = true
                if (stopOnCrash) return
            }
        }

        // Check time elapsed
        val elapsed = Duration.between(startTime, Instant.now())
        logger.info("Time elapsed: ${elapsed.toMinutes()} minutes ${elapsed.toSecondsPart()} seconds")

        if (elapsed > timeLimit) {
            logger.info("Time limit reached (5 minutes)")
            break
        }
    }

    // Final status
    val battleStatus = robotApiClient.getBattleStatus(battleId)
    logger.info("Battle ended with state: ${battleStatus.state}")

    if (robot1Crashed) {
        logger.info("${robot1.name} crashed into a wall")
    }

    if (robot2Crashed) {
        logger.info("${robot2.name} crashed into a wall")
    }

    if (!robot1Crashed && !robot2Crashed) {
        logger.info("Time limit reached without any crashes")
    }
}
