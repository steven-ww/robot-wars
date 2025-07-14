package za.co.sww.rwars.robodemo.wiremock

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.slf4j.LoggerFactory
import za.co.sww.rwars.robodemo.model.Battle
import za.co.sww.rwars.robodemo.model.LaserResponse
import za.co.sww.rwars.robodemo.model.RadarResponse
import za.co.sww.rwars.robodemo.model.Robot
import java.util.UUID

/**
 * Class to configure WireMock stubs for the API endpoints.
 */
class WireMockStubs {
    private val logger = LoggerFactory.getLogger(WireMockStubs::class.java)
    private val mapper: ObjectMapper = jacksonObjectMapper()

    // Store generated IDs for use across different stubs
    private val battleId = UUID.randomUUID().toString()
    private val robotIds = mutableMapOf<String, String>()

    /**
     * Sets up a stub for creating a battle.
     *
     * @param battleName The name of the battle
     * @return The created battle
     */
    fun stubCreateBattle(battleName: String): Battle {
        val battle = Battle(
            id = battleId,
            name = battleName,
            arenaWidth = 50,
            arenaHeight = 50,
            state = "READY",
            robots = emptyList(),
            robotMovementTimeSeconds = 1.0,
        )

        WireMock.stubFor(
            post(urlEqualTo("/api/battles"))
                .withRequestBody(
                    equalToJson(
                        mapper.writeValueAsString(mapOf("name" to battleName)),
                        true,
                        false,
                    ),
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(battle)),
                ),
        )

        logger.info("Stubbed create battle endpoint for battle: $battleName")
        return battle
    }

    /**
     * Sets up a stub for registering a robot.
     *
     * @param robotName The name of the robot
     * @return The registered robot
     */
    fun stubRegisterRobot(robotName: String): Robot {
        val robotId = UUID.randomUUID().toString()
        robotIds[robotName] = robotId

        val robot = Robot(
            id = robotId,
            name = robotName,
            battleId = battleId,
            positionX = 0,
            positionY = 0,
            direction = "NORTH",
            status = "IDLE",
            blocksRemaining = 0,
            targetBlocks = 0,
        )

        WireMock.stubFor(
            post(urlEqualTo("/api/robots/register"))
                .withRequestBody(
                    equalToJson(
                        mapper.writeValueAsString(mapOf("name" to robotName)),
                        true,
                        false,
                    ),
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(robot)),
                ),
        )

        logger.info("Stubbed register robot endpoint for robot: $robotName")
        return robot
    }

    /**
     * Sets up a stub for starting a battle.
     *
     * @return The started battle
     */
    fun stubStartBattle(): Battle {
        val battle = Battle(
            id = battleId,
            name = "Test Battle",
            arenaWidth = 50,
            arenaHeight = 50,
            state = "IN_PROGRESS",
            robots = robotIds.map { (name, id) ->
                Robot(
                    id = id,
                    name = name,
                    battleId = battleId,
                    positionX = 0,
                    positionY = 0,
                    direction = "NORTH",
                    status = "IDLE",
                    blocksRemaining = 0,
                    targetBlocks = 0,
                )
            },
            robotMovementTimeSeconds = 1.0,
        )

        WireMock.stubFor(
            post(urlPathMatching("/api/battles/$battleId/start"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(battle)),
                ),
        )

        logger.info("Stubbed start battle endpoint for battle: $battleId")
        return battle
    }

    /**
     * Sets up a stub for getting the battle status.
     *
     * @return The battle status
     */
    fun stubGetBattleStatus(): Battle {
        val battle = Battle(
            id = battleId,
            name = "Test Battle",
            arenaWidth = 50,
            arenaHeight = 50,
            state = "IN_PROGRESS",
            robots = robotIds.map { (name, id) ->
                Robot(
                    id = id,
                    name = name,
                    battleId = battleId,
                    positionX = 0,
                    positionY = 0,
                    direction = "NORTH",
                    status = "IDLE",
                    blocksRemaining = 0,
                    targetBlocks = 0,
                )
            },
            robotMovementTimeSeconds = 1.0,
        )

        WireMock.stubFor(
            get(urlPathMatching("/api/robots/battle/$battleId"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(battle)),
                ),
        )

        logger.info("Stubbed get battle status endpoint for battle: $battleId")
        return battle
    }

    /**
     * Sets up a stub for getting robot status (position-blind).
     *
     * @param robotName The name of the robot
     * @param status The status of the robot (IDLE, MOVING, CRASHED)
     * @return The robot status
     */
    fun stubGetRobotStatus(robotName: String, status: String = "IDLE"): za.co.sww.rwars.robodemo.model.RobotStatus {
        val robotId = robotIds[robotName] ?: throw IllegalArgumentException("Robot not found: $robotName")

        val robotStatus = za.co.sww.rwars.robodemo.model.RobotStatus(
            id = robotId,
            name = robotName,
            battleId = battleId,
            direction = "NORTH",
            status = status,
            blocksRemaining = 0,
            targetBlocks = 0,
            hitPoints = 100,
            maxHitPoints = 100,
        )

        WireMock.stubFor(
            get(urlPathMatching("/api/robots/battle/$battleId/robot/$robotId/status"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(robotStatus)),
                ),
        )

        logger.info("Stubbed get robot status endpoint for robot: $robotName with status: $status")
        return robotStatus
    }

    /**
     * Sets up a stub for moving a robot.
     *
     * @param robotName The name of the robot
     * @param direction The direction to move
     * @param blocks The number of blocks to move
     * @return The updated robot
     */
    fun stubMoveRobot(robotName: String, direction: String, blocks: Int): Robot {
        val robotId = robotIds[robotName] ?: throw IllegalArgumentException("Robot not found: $robotName")

        val robot = Robot(
            id = robotId,
            name = robotName,
            battleId = battleId,
            positionX = 0,
            positionY = 0,
            direction = direction,
            status = "MOVING",
            blocksRemaining = blocks,
            targetBlocks = blocks,
        )

        WireMock.stubFor(
            post(urlPathMatching("/api/robots/battle/$battleId/robot/$robotId/move"))
                .withRequestBody(
                    equalToJson(
                        mapper.writeValueAsString(
                            mapOf(
                                "direction" to direction,
                                "blocks" to blocks,
                            ),
                        ),
                        true,
                        false,
                    ),
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(robot)),
                ),
        )

        logger.info("Stubbed move robot endpoint for robot: $robotName, direction: $direction, blocks: $blocks")
        return robot
    }

    /**
     * Gets the battle ID.
     *
     * @return The battle ID
     */
    fun getBattleId(): String {
        return battleId
    }

    /**
     * Gets the robot ID for the given robot name.
     *
     * @param robotName The name of the robot
     * @return The robot ID
     */
    fun getRobotId(robotName: String): String {
        return robotIds[robotName] ?: throw IllegalArgumentException("Robot not found: $robotName")
    }

    /**
     * Sets up a stub for radar scanning.
     *
     * @param robotName The name of the robot
     * @param range The radar range
     * @param detections List of detections to return
     * @return The radar response
     */
    fun stubRadarScan(robotName: String, range: Int = 5, detections: List<RadarResponse.Detection> = emptyList()): RadarResponse {
        val robotId = robotIds[robotName] ?: throw IllegalArgumentException("Robot not found: $robotName")

        val radarResponse = RadarResponse(
            range = range,
            detections = detections,
        )

        WireMock.stubFor(
            post(urlPathMatching("/api/robots/battle/$battleId/robot/$robotId/radar"))
                .withRequestBody(
                    equalToJson(
                        mapper.writeValueAsString(mapOf("range" to range)),
                        true,
                        false,
                    ),
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(radarResponse)),
                ),
        )

        logger.info("Stubbed radar scan endpoint for robot: $robotName with range: $range, detections: ${detections.size}")
        return radarResponse
    }

    /**
     * Sets up a stub for firing a laser.
     *
     * @param robotName The name of the robot
     * @param direction The direction to fire the laser
     * @param range The laser range
     * @param hit Whether the laser hit a target
     * @param hitRobotName The name of the robot that was hit (if any)
     * @return The laser response
     */
    fun stubFireLaser(
        robotName: String,
        direction: String,
        range: Int = 10,
        hit: Boolean = false,
        hitRobotName: String? = null,
    ): LaserResponse {
        val robotId = robotIds[robotName] ?: throw IllegalArgumentException("Robot not found: $robotName")

        val laserResponse = LaserResponse(
            hit = hit,
            hitRobotId = if (hit && hitRobotName != null) robotIds[hitRobotName] else null,
            hitRobotName = hitRobotName,
            damageDealt = if (hit) 20 else 0,
            range = range,
            direction = direction,
            laserPath = listOf(
                LaserResponse.Position(0, 0),
                LaserResponse.Position(1, 0),
                LaserResponse.Position(2, 0),
            ),
            hitPosition = if (hit) LaserResponse.Position(2, 0) else null,
            blockedBy = if (hit) "ROBOT" else "WALL",
        )

        WireMock.stubFor(
            post(urlPathMatching("/api/robots/battle/$battleId/robot/$robotId/laser"))
                .withRequestBody(
                    equalToJson(
                        mapper.writeValueAsString(
                            mapOf(
                                "direction" to direction,
                                "range" to range,
                            ),
                        ),
                        true,
                        false,
                    ),
                )
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(laserResponse)),
                ),
        )

        logger.info("Stubbed fire laser endpoint for robot: $robotName, direction: $direction, range: $range, hit: $hit")
        return laserResponse
    }
}
