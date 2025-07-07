package za.co.sww.rwars.robodemo.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.slf4j.LoggerFactory

/**
 * Extension class to manage WireMock server lifecycle for tests.
 */
class WireMockExtension {
    private val logger = LoggerFactory.getLogger(WireMockExtension::class.java)
    private val wireMockServer: WireMockServer

    init {
        wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    }

    /**
     * Starts the WireMock server.
     *
     * @return The base URL of the WireMock server
     */
    fun start(): String {
        if (!wireMockServer.isRunning) {
            wireMockServer.start()
            WireMock.configureFor("localhost", wireMockServer.port())
            logger.info("WireMock server started on port: ${wireMockServer.port()}")
        }
        return "http://localhost:${wireMockServer.port()}"
    }

    /**
     * Stops the WireMock server.
     */
    fun stop() {
        if (wireMockServer.isRunning) {
            wireMockServer.stop()
            logger.info("WireMock server stopped")
        }
    }

    /**
     * Resets the WireMock server.
     */
    fun reset() {
        wireMockServer.resetAll()
        logger.info("WireMock server reset")
    }

    /**
     * Gets the WireMock server instance.
     *
     * @return The WireMock server instance
     */
    fun getServer(): WireMockServer {
        return wireMockServer
    }
}
