package za.co.sww.rwars.robodemo

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import za.co.sww.rwars.robodemo.api.BattleApiClient
import za.co.sww.rwars.robodemo.api.RobotApiClient
import za.co.sww.rwars.robodemo.model.Robot
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("Main")

/**
 * Generates a unique battle name using timestamp and UUID to ensure uniqueness
 * even when multiple instances are run simultaneously.
 *
 * @return A unique battle name in the format "Demo Battle YYYY-MM-DD HH:mm:ss [UUID-suffix]"
 */
fun generateUniqueBattleName(): String {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    val uniqueId = UUID.randomUUID().toString().takeLast(8) // Use last 8 characters of UUID
    return "Demo Battle $timestamp [$uniqueId]"
}

/**
 * Generates unique robot names with creative prefixes and UUID suffixes.
 * Randomly selects from a pool of creative robot names to make each demo run more interesting.
 *
 * @return A unique robot name in the format "CreativeName-[UUID-suffix]"
 */
fun generateUniqueRobotName(): String {
    val creativeNames = listOf(
        "Restro", "ReqBot", "CyberWarrior", "MechFighter", "BattleBot",
        "IronGuardian", "SteelStorm", "TitanCrusher", "NeonNinja", "QuantumRanger",
        "PlasmaStrike", "VortexHunter", "ThunderBolt", "LaserLance", "RocketRider",
    )
    val baseName = creativeNames.random()
    val uniqueId = UUID.randomUUID().toString().takeLast(6) // Use last 6 characters of UUID
    return "$baseName-$uniqueId"
}

/**
 * Generates unique robot names with a specific base name and UUID suffixes.
 *
 * @param baseName The base name for the robot
 * @return A unique robot name in the format "BaseName-[UUID-suffix]"
 */
fun generateUniqueRobotName(baseName: String): String {
    val uniqueId = UUID.randomUUID().toString().takeLast(6) // Use last 6 characters of UUID
    return "$baseName-$uniqueId"
}

/**
 * Data class to hold application configuration parsed from command line arguments.
 */
data class AppConfig(
    val baseUrl: String = "http://localhost:8080",
    val timeLimit: Duration = Duration.ofMinutes(5),
)

/**
 * Parses command line arguments into an AppConfig object.
 * Supports named parameters: --url, --time
 */
fun parseArgs(args: Array<String>): AppConfig {
    var baseUrl = "http://localhost:8080"
    var timeLimit = Duration.ofMinutes(5)

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
            args[i] == "--help" || args[i] == "-h" -> {
                println("Usage: robo-demo [OPTIONS]")
                println("Options:")
                println("  --url <baseUrl>     Base URL for the Robot Wars API (default: http://localhost:8080)")
                println("  --time <time>       Time limit for the battle (e.g., 5m, 30s) (default: 5m)")
                println("  --help, -h          Show this help message")
                println()
                println("The demo will run until one robot wins or the time limit is reached.")
                exitProcess(0)
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

    return AppConfig(baseUrl, timeLimit)
}

/**
 * Main entry point for the robo-demo application.
 * This application demonstrates the use of the Robot Wars API to:
 * 1. Create a new battle
 * 2. Register robots
 * 3. Start the battle
 * 4. Move robots around until one wins or time limit is reached
 *
 * Supported parameters:
 * --url <baseUrl> : Base URL for the Robot Wars API (default: http://localhost:8080)
 * --time <time>   : Time limit for the battle (e.g., 5m, 30s) (default: 5m)
 */
fun main(args: Array<String>) = runBlocking {
    logger.info("Starting Robot Wars Demo")

    val config = parseArgs(args)
    logger.info("Using base URL: ${config.baseUrl}")
    logger.info("Time limit set to: ${config.timeLimit}")

    val battleApiClient = BattleApiClient(config.baseUrl)
    val robotApiClient = RobotApiClient(config.baseUrl)

    try {
        // Create a new battle with a 20x20 arena and unique name
        val battleName = generateUniqueBattleName()
        logger.info("Creating a new battle with a 20x20 arena: $battleName")
        val battle = battleApiClient.createBattle(battleName, 20, 20)
        logger.info("Battle created: ${battle.id} - ${battle.name} - Arena size: ${battle.arenaWidth}x${battle.arenaHeight}")

        // Register robots with unique creative names for the specific battle
        val restroName = generateUniqueRobotName()
        logger.info("Registering robot '$restroName' for battle ${battle.id}")
        val restro = robotApiClient.registerRobotForBattle(restroName, battle.id)
        logger.info("Robot registered: ${restro.id} - ${restro.name}")

        val reqBotName = generateUniqueRobotName()
        logger.info("Registering robot '$reqBotName' for battle ${battle.id}")
        val reqBot = robotApiClient.registerRobotForBattle(reqBotName, battle.id)
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

        // Move robots until one wins or time limit is reached
        moveRobotsUntilWinnerOrTimeout(robotApiClient, battle.id, restro, reqBot, config.timeLimit)

        logger.info("Demo completed successfully")
    } catch (e: Exception) {
        logger.error("Error during demo execution", e)
        exitProcess(1)
    }
}

/**
 * Moves robots around the arena until one wins or the time limit is reached.
 * Robots move concurrently and independently.
 */
private suspend fun moveRobotsUntilWinnerOrTimeout(
    robotApiClient: RobotApiClient,
    battleId: String,
    robot1: Robot,
    robot2: Robot,
    timeLimit: Duration,
) = coroutineScope {
    val startTime = Instant.now()
    val directions = listOf("NORTH", "EAST", "SOUTH", "WEST", "NE", "SE", "SW", "NW")
    var robot1Crashed = false
    var robot2Crashed = false
    var battleCompleted = false

    logger.info("Moving robots around the arena concurrently")

    // Start concurrent robot movement coroutines
    val robot1Job = launch {
        moveRobotContinuously(robotApiClient, battleId, robot1, directions) { crashed ->
            robot1Crashed = crashed
        }
    }

    val robot2Job = launch {
        moveRobotContinuously(robotApiClient, battleId, robot2, directions) { crashed ->
            robot2Crashed = crashed
        }
    }

    // Monitor the battle state until completion or timeout
    while (
        Duration.between(startTime, Instant.now()) < timeLimit &&
        !battleCompleted
    ) {
        try {
            // Check battle status
            val battleStatus = robotApiClient.getBattleStatus(battleId)

            // Check if battle is completed
            if (battleStatus.state == "COMPLETED") {
                battleCompleted = true
                robot1Job.cancel()
                robot2Job.cancel()

                if (battleStatus.winnerName != null) {
                    logger.info("🎉 BATTLE WON! Winner: ${battleStatus.winnerName} (ID: ${battleStatus.winnerId})")
                } else {
                    logger.info("Battle completed with no winner")
                }
                break
            }

            // Get current robot status for logging
            val robot1Details = robotApiClient.getRobotDetails(battleId, robot1.id)
            val robot2Details = robotApiClient.getRobotDetails(battleId, robot2.id)

            // Check for crashes and log status changes
            if (robot1Details.status == "CRASHED" && !robot1Crashed) {
                logger.info("💥 ${robot1.name} crashed into a wall at position (${robot1Details.positionX}, ${robot1Details.positionY})!")
                robot1Crashed = true
                robot1Job.cancel()
            }

            if (robot2Details.status == "CRASHED" && !robot2Crashed) {
                logger.info("💥 ${robot2.name} crashed into a wall at position (${robot2Details.positionX}, ${robot2Details.positionY})!")
                robot2Crashed = true
                robot2Job.cancel()
            }

            // Log robot positions periodically
            val elapsed = Duration.between(startTime, Instant.now())
            if (elapsed.seconds % 10 == 0L) { // Log every 10 seconds
                logger.info("📍 ${robot1Details.name}: (${robot1Details.positionX}, ${robot1Details.positionY}) - Status: ${robot1Details.status}")
                logger.info("📍 ${robot2Details.name}: (${robot2Details.positionX}, ${robot2Details.positionY}) - Status: ${robot2Details.status}")
            }
        } catch (e: Exception) {
            logger.error("Error monitoring battle status", e)
        }

        // Wait before next status check
        delay(1000) // Check every second

        // Log time elapsed every 30 seconds
        val elapsed = Duration.between(startTime, Instant.now())
        if (elapsed.seconds % 30 == 0L && elapsed.seconds > 0) {
            logger.info("⏱️  Time elapsed: ${elapsed.toMinutes()} minutes ${elapsed.toSecondsPart()} seconds")
        }
    }

    // Cancel any remaining robot movement jobs
    robot1Job.cancel()
    robot2Job.cancel()

    // Final status
    val finalBattleStatus = robotApiClient.getBattleStatus(battleId)
    logger.info("🏁 Battle ended with state: ${finalBattleStatus.state}")

    if (finalBattleStatus.state == "COMPLETED" && finalBattleStatus.winnerName != null) {
        logger.info("🏆 Final Winner: ${finalBattleStatus.winnerName}")
    } else if (!battleCompleted) {
        logger.info("⏰ Time limit reached without a winner")
    }

    // Log final robot status
    try {
        val finalRobot1 = robotApiClient.getRobotDetails(battleId, robot1.id)
        val finalRobot2 = robotApiClient.getRobotDetails(battleId, robot2.id)

        logger.info("📊 Final Status:")
        logger.info("   ${finalRobot1.name}: ${finalRobot1.status} at (${finalRobot1.positionX}, ${finalRobot1.positionY})")
        logger.info("   ${finalRobot2.name}: ${finalRobot2.status} at (${finalRobot2.positionX}, ${finalRobot2.positionY})")
    } catch (e: Exception) {
        logger.error("Error getting final robot status", e)
    }
}

/**
 * Continuously moves a robot in random directions until it crashes or is stopped.
 * Uses radar to detect and avoid walls when possible.
 */
private suspend fun moveRobotContinuously(
    robotApiClient: RobotApiClient,
    battleId: String,
    robot: Robot,
    directions: List<String>,
    onCrashed: (Boolean) -> Unit,
) {
    try {
        while (true) {
            // Wait for robot to be idle before issuing next movement command
            var robotStatus = robotApiClient.getRobotDetails(battleId, robot.id)

            // If robot crashed, stop moving
            if (robotStatus.status == "CRASHED") {
                onCrashed(true)
                break
            }

            // Wait if robot is still moving
            while (robotStatus.status == "MOVING") {
                delay(500) // Check every 500ms
                robotStatus = robotApiClient.getRobotDetails(battleId, robot.id)

                if (robotStatus.status == "CRASHED") {
                    onCrashed(true)
                    return
                }
            }

            // Use radar to scan for nearby walls and choose a safe direction
            val safeDirection = chooseSafeDirection(robotApiClient, battleId, robot, directions)
            val blocks = (1..2).random() // Reduced range to be more cautious

            if (safeDirection != null) {
                val (destX, destY) = calculateDestination(robotStatus.positionX, robotStatus.positionY, safeDirection, blocks)
                logger.info("🚀 Moving ${robot.name} $blocks blocks $safeDirection (radar-guided from (${robotStatus.positionX}, ${robotStatus.positionY}) to (~$destX, ~$destY))")
            } else {
                // If no safe direction found, try a random direction with minimal movement
                val direction = directions.random()
                val (destX, destY) = calculateDestination(robotStatus.positionX, robotStatus.positionY, direction, 1)
                logger.info("🚀 Moving ${robot.name} 1 block $direction (random fallback from (${robotStatus.positionX}, ${robotStatus.positionY}) to (~$destX, ~$destY))")

                try {
                    robotApiClient.moveRobot(battleId, robot.id, direction, 1)
                } catch (e: IOException) {
                    if (e.message?.contains("409") == true) {
                        logger.info("🏁 Battle has ended, stopping movement for ${robot.name}")
                        break
                    } else {
                        throw e
                    }
                }

                delay((1000..2500).random().toLong())
                continue
            }

            try {
                robotApiClient.moveRobot(battleId, robot.id, safeDirection, blocks)
            } catch (e: IOException) {
                if (e.message?.contains("409") == true) {
                    // Battle has ended (409 Conflict), stop moving this robot
                    logger.info("🏁 Battle has ended, stopping movement for ${robot.name}")
                    break
                } else {
                    // Re-throw other IOExceptions
                    throw e
                }
            }

            // Add some randomness to movement timing
            delay((1000..2500).random().toLong())
        }
    } catch (e: Exception) {
        logger.error("Error in continuous movement for ${robot.name}", e)
        onCrashed(true)
    }
}

/**
 * Calculates the intended destination coordinates for a movement command.
 * Used for logging purposes to show where the robot intends to move.
 */
private fun calculateDestination(currentX: Int, currentY: Int, direction: String, blocks: Int): Pair<Int, Int> {
    val (deltaX, deltaY) = when (direction) {
        "NORTH" -> Pair(0, -blocks)
        "SOUTH" -> Pair(0, blocks)
        "EAST" -> Pair(blocks, 0)
        "WEST" -> Pair(-blocks, 0)
        "NE" -> Pair(blocks, -blocks)
        "NW" -> Pair(-blocks, -blocks)
        "SE" -> Pair(blocks, blocks)
        "SW" -> Pair(-blocks, blocks)
        else -> Pair(0, 0)
    }
    return Pair(currentX + deltaX, currentY + deltaY)
}

/**
 * Uses radar to choose a safe direction for robot movement.
 * Analyzes radar data to avoid walls and other robots.
 */
private suspend fun chooseSafeDirection(
    robotApiClient: RobotApiClient,
    battleId: String,
    robot: Robot,
    directions: List<String>,
): String? {
    try {
        // Perform radar scan with range of 3 to detect nearby obstacles
        val radarResponse = robotApiClient.performRadarScan(battleId, robot.id, 3)

        // Get current robot position
        val robotDetails = robotApiClient.getRobotDetails(battleId, robot.id)
        val currentX = robotDetails.positionX
        val currentY = robotDetails.positionY

        // Log detailed radar scan results
        logger.info("📡 ${robot.name} at ($currentX, $currentY) - Radar scan (range 3):")
        if (radarResponse.detections.isEmpty()) {
            logger.info("   No obstacles detected within range")
        } else {
            radarResponse.detections.forEach { detection ->
                val distance = kotlin.math.sqrt(
                    (
                        detection.x * detection.x +
                            detection.y * detection.y
                        ).toDouble(),
                ).toInt()
                logger.info("   ${detection.type.name} detected at relative position (${detection.x}, ${detection.y}) - distance: $distance - ${detection.details}")
            }
        }

        // Analyze each direction for safety
        val safeDirections = mutableListOf<String>()
        val unsafeDirections = mutableListOf<String>()

        for (direction in directions) {
            val isSafe = isDirectionSafe(direction, currentX, currentY, radarResponse.detections)
            if (isSafe) {
                safeDirections.add(direction)
            } else {
                unsafeDirections.add(direction)
            }
        }

        // Log decision analysis
        logger.info("📊 ${robot.name} direction analysis:")
        logger.info("   Safe directions: $safeDirections")
        logger.info("   Unsafe directions: $unsafeDirections")

        val chosenDirection = safeDirections.randomOrNull()
        if (chosenDirection != null) {
            logger.info("✅ ${robot.name} chose direction: $chosenDirection (radar-guided)")
        } else {
            logger.info("⚠️  ${robot.name} found no safe directions - will attempt random fallback")
        }

        // Return a random safe direction, or null if none found
        return chosenDirection
    } catch (e: Exception) {
        logger.warn("Failed to perform radar scan for ${robot.name}: ${e.message}")
        logger.info("🎲 ${robot.name} falling back to random direction due to radar failure")
        // Fallback to random direction if radar fails
        return directions.random()
    }
}

/**
 * Determines if a direction is safe based on radar detections.
 * Checks if moving in the given direction would lead towards a detected obstacle.
 * Now works with relative coordinates from radar (robot position is 0,0).
 */
private fun isDirectionSafe(
    direction: String,
    currentX: Int,
    currentY: Int,
    detections: List<za.co.sww.rwars.robodemo.model.RadarResponse.Detection>,
): Boolean {
    // Calculate the direction vector
    val (deltaX, deltaY) = when (direction) {
        "NORTH" -> Pair(0, -1)
        "SOUTH" -> Pair(0, 1)
        "EAST" -> Pair(1, 0)
        "WEST" -> Pair(-1, 0)
        "NE" -> Pair(1, -1)
        "NW" -> Pair(-1, -1)
        "SE" -> Pair(1, 1)
        "SW" -> Pair(-1, 1)
        else -> Pair(0, 0)
    }

    // Check if any detected walls are in the path of this direction
    for (detection in detections) {
        if (detection.type.name == "WALL") {
            // Detection coordinates are now relative to robot position (0,0)
            val relativeX = detection.x
            val relativeY = detection.y

            // Check if the wall is in the same direction as our intended movement
            // If we're moving in a direction and there's a wall in that direction within 2 blocks, it's unsafe
            if (deltaX != 0 && (relativeX * deltaX > 0) && Math.abs(relativeX) <= 2) {
                if (deltaY == 0 || (relativeY * deltaY >= 0 && Math.abs(relativeY) <= 2)) {
                    logger.debug("   ❌ $direction unsafe: Wall at relative position ($relativeX, $relativeY) blocks path")
                    return false
                }
            }

            if (deltaY != 0 && (relativeY * deltaY > 0) && Math.abs(relativeY) <= 2) {
                if (deltaX == 0 || (relativeX * deltaX >= 0 && Math.abs(relativeX) <= 2)) {
                    logger.debug("   ❌ $direction unsafe: Wall at relative position ($relativeX, $relativeY) blocks path")
                    return false
                }
            }
        }
    }

    logger.debug("   ✅ $direction safe: No walls detected in path")
    return true
}
