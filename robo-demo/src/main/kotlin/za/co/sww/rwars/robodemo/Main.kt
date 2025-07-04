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
 * Main entry point for the robo-demo application.
 * This application demonstrates the use of the Robot Wars API to:
 * 1. Create a new battle
 * 2. Register robots
 * 3. Start the battle
 * 4. Move robots around until one crashes or time limit is reached
 */
fun main(args: Array<String>) = runBlocking {
    logger.info("Starting Robot Wars Demo")

    val baseUrl = args.getOrElse(0) { "http://localhost:8080" }
    logger.info("Using base URL: $baseUrl")

    val battleApiClient = BattleApiClient(baseUrl)
    val robotApiClient = RobotApiClient(baseUrl)

    try {
        // Create a new battle
        logger.info("Creating a new battle")
        val battle = battleApiClient.createBattle("Demo Battle")
        logger.info("Battle created: ${battle.id} - ${battle.name}")

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

        // Move robots until one crashes or time limit is reached
        moveRobotsUntilCrashOrTimeout(robotApiClient, battle.id, restro, reqBot)

        logger.info("Demo completed successfully")
    } catch (e: Exception) {
        logger.error("Error during demo execution", e)
        exitProcess(1)
    }
}

/**
 * Moves robots around the arena until one crashes or the time limit is reached.
 */
private suspend fun moveRobotsUntilCrashOrTimeout(
    robotApiClient: RobotApiClient,
    battleId: String,
    robot1: Robot,
    robot2: Robot,
) {
    val startTime = Instant.now()
    val timeLimit = Duration.ofMinutes(5)
    val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
    var robot1Crashed = false
    var robot2Crashed = false

    logger.info("Moving robots around the arena")

    while (!robot1Crashed && !robot2Crashed && Duration.between(startTime, Instant.now()) < timeLimit) {
        // Move robot1
        if (!robot1Crashed) {
            val direction1 = directions.random()
            val blocks1 = (1..3).random()
            logger.info("Moving ${robot1.name} $blocks1 blocks $direction1")

            try {
                robotApiClient.moveRobot(battleId, robot1.id, direction1, blocks1)
                // Wait for movement to complete
                Thread.sleep(blocks1 * 1000L)

                // Check if robot crashed
                val robotStatus1 = robotApiClient.getRobotDetails(battleId, robot1.id)
                if (robotStatus1.status == "CRASHED") {
                    logger.info("${robot1.name} crashed into a wall!")
                    robot1Crashed = true
                }
            } catch (e: Exception) {
                logger.error("Error moving ${robot1.name}", e)
                robot1Crashed = true
            }
        }

        // Move robot2
        if (!robot2Crashed) {
            val direction2 = directions.random()
            val blocks2 = (1..3).random()
            logger.info("Moving ${robot2.name} $blocks2 blocks $direction2")

            try {
                robotApiClient.moveRobot(battleId, robot2.id, direction2, blocks2)
                // Wait for movement to complete
                Thread.sleep(blocks2 * 1000L)

                // Check if robot crashed
                val robotStatus2 = robotApiClient.getRobotDetails(battleId, robot2.id)
                if (robotStatus2.status == "CRASHED") {
                    logger.info("${robot2.name} crashed into a wall!")
                    robot2Crashed = true
                }
            } catch (e: Exception) {
                logger.error("Error moving ${robot2.name}", e)
                robot2Crashed = true
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
