package za.co.sww.rwars.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.junit.jupiter.api.Assertions;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.service.BattleService;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@QuarkusTest
public class WebSocketSteps {

    private static final Logger LOGGER = Logger.getLogger(WebSocketSteps.class.getName());
    private static final String WEBSOCKET_URI = "ws://localhost:8081/battle-state/";

    @Inject
    private BattleService battleService;

    private Battle battle;
    private String battleId;
    private Map<String, Robot> robots = new ConcurrentHashMap<>();
    private TestWebSocketClient webSocketClient;
    private Session webSocketSession;
    private String lastReceivedMessage;
    private CountDownLatch messageLatch;

    @Before
    public void setup() {
        // Reset the battle service before each test
        if (battleService != null) {
            battleService.resetBattle();
        }
    }

    @After
    public void cleanup() {
        // Close the WebSocket session if it's open
        if (webSocketSession != null && webSocketSession.isOpen()) {
            try {
                webSocketSession.close();
                LOGGER.info("WebSocket session closed");
            } catch (IOException e) {
                LOGGER.severe("Error closing WebSocket session: " + e.getMessage());
            }
        }
    }

    @Given("a battle with name {string} and dimensions {int}x{int} exists")
    public void aBattleWithNameAndDimensionsExists(String battleName, int width, int height) {
        battle = battleService.createBattle(battleName, width, height);
        battleId = battle.getId();
        LOGGER.info("Created battle with ID: " + battleId);
    }

    @And("a robot named {string} is registered for the battle")
    public void aRobotNamedIsRegisteredForTheBattle(String robotName) {
        Robot robot = battleService.registerRobotForBattle(robotName, battleId);
        robots.put(robotName, robot);
        LOGGER.info("Registered robot: " + robotName + " with ID: " + robot.getId());

        // Set deterministic positions for reliable testing
        // Position robots at known locations to avoid flaky tests due to random positioning
        try {
            if ("TestBot1".equals(robotName)) {
                // Position TestBot1 at (15, 15)
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 15, 15);
            } else if ("TestBot2".equals(robotName)) {
                // Position TestBot2 at (35, 35) - far enough from TestBot1 but within arena
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 35, 35);
            } else {
                // For any other robots, use a safe fallback position
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 25, 25);
            }
        } catch (Exception e) {
            // If positioning fails (e.g., due to walls), try alternative positions
            if ("TestBot1".equals(robotName)) {
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 10, 10);
            } else if ("TestBot2".equals(robotName)) {
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 40, 40);
            } else {
                battleService.setRobotPositionForTesting(battleId, robot.getId(), 30, 30);
            }
        }
    }

    @When("I connect to the battle state websocket")
    public void iConnectToTheBattleStateWebsocket() throws Exception {
        connectToWebSocket();
    }

    @Given("I am connected to the battle state websocket")
    public void iAmConnectedToTheBattleStateWebsocket() throws Exception {
        connectToWebSocket();
    }

    @When("I send an {string} message to the websocket")
    public void iSendAnMessageToTheWebsocket(String message) throws Exception {
        messageLatch = new CountDownLatch(1);
        webSocketSession.getBasicRemote().sendText(message);
        LOGGER.info("Sent message to WebSocket: " + message);
    }

    @When("robot {string} moves {string} for {int} blocks")
    public void robotMovesForBlocks(String robotName, String direction, int blocks) {
        Robot robot = robots.get(robotName);
        Assertions.assertNotNull(robot, "Robot not found: " + robotName);

        battleService.moveRobot(battleId, robot.getId(), direction, blocks);
        LOGGER.info("Robot " + robotName + " moved " + direction + " for " + blocks + " blocks");
    }

    @When("the battle is started")
    public void theBattleIsStarted() {
        battleService.startBattle(battleId);
        LOGGER.info("Battle started: " + battleId);
    }

    @Then("I should receive the battle state information")
    public void iShouldReceiveTheBattleStateInformation() throws Exception {
        Assertions.assertTrue(messageLatch.await(5, TimeUnit.SECONDS),
                "Did not receive WebSocket message within timeout");
        Assertions.assertNotNull(lastReceivedMessage, "No WebSocket message received");
        LOGGER.info("Received WebSocket message: " + lastReceivedMessage);
    }

    @Then("I should receive the updated battle state information")
    public void iShouldReceiveTheUpdatedBattleStateInformation() throws Exception {
        Assertions.assertTrue(messageLatch.await(5, TimeUnit.SECONDS),
                "Did not receive WebSocket message within timeout");
        Assertions.assertNotNull(lastReceivedMessage, "No WebSocket message received");
        LOGGER.info("Received updated WebSocket message: " + lastReceivedMessage);
    }

    @Then("I should automatically receive a battle state update")
    public void iShouldAutomaticallyReceiveABattleStateUpdate() throws Exception {
        // Set up a new latch to wait for automatic broadcast
        messageLatch = new CountDownLatch(1);

        // Wait for automatic broadcast (should happen when robot starts moving)
        Assertions.assertTrue(messageLatch.await(5, TimeUnit.SECONDS),
                "Did not receive automatic WebSocket broadcast within timeout");
        Assertions.assertNotNull(lastReceivedMessage, "No automatic WebSocket message received");
        LOGGER.info("Received automatic WebSocket broadcast: " + lastReceivedMessage);
    }

    @And("the battle state should include the arena dimensions {int}x{int}")
    public void theBattleStateShouldIncludeTheArenaDimensions(int width, int height) throws Exception {
        JsonNode jsonNode = parseJsonMessage(lastReceivedMessage);
        Assertions.assertEquals(width, jsonNode.get("arenaWidth").asInt(), "Arena width does not match");
        Assertions.assertEquals(height, jsonNode.get("arenaHeight").asInt(), "Arena height does not match");
    }

    @And("the battle state should include {int} registered robots")
    public void theBattleStateShouldIncludeRegisteredRobots(int count) throws Exception {
        JsonNode jsonNode = parseJsonMessage(lastReceivedMessage);
        Assertions.assertEquals(count, jsonNode.get("robots").size(), "Robot count does not match");
    }

    @And("the battle state should include the robot {string}")
    public void theBattleStateShouldIncludeTheRobot(String robotName) throws Exception {
        JsonNode jsonNode = parseJsonMessage(lastReceivedMessage);
        JsonNode robotsNode = jsonNode.get("robots");
        boolean found = false;
        for (JsonNode robotNode : robotsNode) {
            if (robotName.equals(robotNode.get("name").asText())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Robot not found in battle state: " + robotName);
    }

    @And("the battle state should include the robot {string} with status {string}")
    public void theBattleStateShouldIncludeTheRobotWithStatus(String robotName, String status) throws Exception {
        JsonNode jsonNode = parseJsonMessage(lastReceivedMessage);
        JsonNode robotsNode = jsonNode.get("robots");
        boolean found = false;
        for (JsonNode robotNode : robotsNode) {
            if (robotName.equals(robotNode.get("name").asText())) {
                Assertions.assertEquals(status, robotNode.get("status").asText(), "Robot status does not match");
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Robot not found in battle state: " + robotName);
    }

    @And("the battle state should include battle state {string}")
    public void theBattleStateShouldIncludeBattleState(String state) throws Exception {
        JsonNode jsonNode = parseJsonMessage(lastReceivedMessage);
        Assertions.assertEquals(state, jsonNode.get("battleState").asText(), "Battle state does not match");
    }

    private void connectToWebSocket() throws Exception {
        messageLatch = new CountDownLatch(1);
        webSocketClient = new TestWebSocketClient();
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String uri = WEBSOCKET_URI + battleId;
        webSocketSession = container.connectToServer(webSocketClient, URI.create(uri));
        LOGGER.info("Connected to WebSocket: " + uri);

        // Wait for the initial message
        Assertions.assertTrue(messageLatch.await(5, TimeUnit.SECONDS),
                "Did not receive initial WebSocket message within timeout");
    }

    private JsonNode parseJsonMessage(String message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(message);
    }

    @ClientEndpoint
    public class TestWebSocketClient {

        @OnOpen
        public void onOpen(Session session) {
            LOGGER.info("WebSocket session opened");
        }

        @OnMessage
        public void onMessage(String message) {
            LOGGER.info("Received WebSocket message: " + message);
            lastReceivedMessage = message;
            messageLatch.countDown();
        }
    }
}
