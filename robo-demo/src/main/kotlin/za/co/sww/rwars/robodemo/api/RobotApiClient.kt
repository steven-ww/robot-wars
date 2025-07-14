package za.co.sww.rwars.robodemo.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import za.co.sww.rwars.robodemo.model.Battle
import za.co.sww.rwars.robodemo.model.LaserResponse
import za.co.sww.rwars.robodemo.model.RadarResponse
import za.co.sww.rwars.robodemo.model.Robot
import za.co.sww.rwars.robodemo.model.RobotStatus
import java.io.IOException

/**
 * Client for interacting with the Robot API.
 */
class RobotApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()
    private val mapper: ObjectMapper = jacksonObjectMapper()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Registers a robot with the given name.
     *
     * @param name The name of the robot
     * @return The registered robot
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun registerRobot(name: String): Robot {
        val requestBody = mapper.writeValueAsString(
            mapOf("name" to name),
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/robots/register")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Registers a robot for a specific battle.
     *
     * @param name The name of the robot
     * @param battleId The ID of the battle to join
     * @return The registered robot
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun registerRobotForBattle(name: String, battleId: String): Robot {
        val requestBody = mapper.writeValueAsString(
            mapOf("name" to name),
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/robots/register/$battleId")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Starts a battle.
     *
     * @param battleId The ID of the battle to start
     * @return The started battle
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun startBattle(battleId: String): Battle {
        val request = Request.Builder()
            .url("$baseUrl/api/battles/$battleId/start")
            .post("".toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Gets the battle status.
     *
     * @param battleId The ID of the battle
     * @return The battle status
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun getBattleStatus(battleId: String): Battle {
        val request = Request.Builder()
            .url("$baseUrl/api/robots/battle/$battleId")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Gets a robot's status without revealing its absolute position.
     * This is what robots should use to check their own status.
     *
     * @param battleId The ID of the battle
     * @param robotId The ID of the robot
     * @return The robot status (without position information)
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun getRobotStatus(battleId: String, robotId: String): RobotStatus {
        val request = Request.Builder()
            .url("$baseUrl/api/robots/battle/$battleId/robot/$robotId/status")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Moves a robot in the specified direction for the specified number of blocks.
     *
     * @param battleId The ID of the battle
     * @param robotId The ID of the robot
     * @param direction The direction to move (NORTH, SOUTH, EAST, WEST, NE, SE, SW, NW)
     * @param blocks The number of blocks to move
     * @return The updated robot
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun moveRobot(battleId: String, robotId: String, direction: String, blocks: Int): Robot {
        val requestBody = mapper.writeValueAsString(
            mapOf(
                "direction" to direction,
                "blocks" to blocks,
            ),
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/robots/battle/$battleId/robot/$robotId/move")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Performs a radar scan for a robot to detect nearby walls and other robots.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param range The radar scan range (default: 5)
     * @return The radar response containing detected objects
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun performRadarScan(battleId: String, robotId: String, range: Int = 5): RadarResponse {
        val requestBody = mapper.writeValueAsString(
            mapOf("range" to range),
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/robots/battle/$battleId/robot/$robotId/radar")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }

    /**
     * Fires a laser for a robot in the specified direction.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param direction The direction to fire the laser (NORTH, SOUTH, EAST, WEST, NE, SE, SW, NW)
     * @param range The laser range (default: 10)
     * @return The laser response containing hit information
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun fireLaser(battleId: String, robotId: String, direction: String, range: Int = 10): LaserResponse {
        val requestBody = mapper.writeValueAsString(
            mapOf(
                "direction" to direction,
                "range" to range,
            ),
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/robots/battle/$battleId/robot/$robotId/laser")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return mapper.readValue(responseBody)
        }
    }
}
