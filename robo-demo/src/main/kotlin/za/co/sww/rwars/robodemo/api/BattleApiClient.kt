package za.co.sww.rwars.robodemo.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import za.co.sww.rwars.robodemo.model.Battle
import java.io.IOException

/**
 * Client for interacting with the Battle API.
 */
class BattleApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()
    private val mapper: ObjectMapper = jacksonObjectMapper()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Creates a new battle with the given name.
     *
     * @param name The name of the battle
     * @param width Optional width of the arena
     * @param height Optional height of the arena
     * @param robotMovementTimeSeconds Optional time in seconds for robot movement
     * @return The created battle
     * @throws IOException if the API call fails
     */
    @Throws(IOException::class)
    suspend fun createBattle(
        name: String,
        width: Int? = null,
        height: Int? = null,
        robotMovementTimeSeconds: Double? = null,
    ): Battle {
        val requestBody = mapper.writeValueAsString(
            mapOf(
                "name" to name,
                "width" to width,
                "height" to height,
                "robotMovementTimeSeconds" to robotMovementTimeSeconds,
            ).filterValues { it != null },
        ).toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/api/battles")
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
