package za.co.sww.rwars.backend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.Wall;
import za.co.sww.rwars.backend.service.BattleService;

/**
 * A WebSocket endpoint for battle state information.
 * This provides real-time updates about battle state, arena attributes, and registered robots.
 */
@ServerEndpoint("/battle-state/{battleId}")
@ApplicationScoped
public class BattleStateSocket {

    private static final Logger LOGGER = Logger.getLogger(BattleStateSocket.class.getName());

    @Inject
    private BattleService battleService;

    // Store active sessions by battle ID
    private final Map<String, Map<String, Session>> sessionsByBattleId = new ConcurrentHashMap<>();

    /**
     * Called when a new WebSocket connection is established.
     *
     * @param session The WebSocket session
     * @param battleId The battle ID from the path parameter
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("battleId") String battleId) {
        // Add the session to the sessions map for this battle
        sessionsByBattleId.computeIfAbsent(battleId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);

        LOGGER.info("New WebSocket connection for battle: " + battleId + ", session: " + session.getId());

        // Send the initial battle state to the client
        sendBattleState(battleId, session);
    }

    /**
     * Called when a WebSocket connection is closed.
     *
     * @param session The WebSocket session
     * @param battleId The battle ID from the path parameter
     */
    @OnClose
    public void onClose(Session session, @PathParam("battleId") String battleId) {
        // Remove the session from the sessions map for this battle
        Map<String, Session> battleSessions = sessionsByBattleId.get(battleId);
        if (battleSessions != null) {
            battleSessions.remove(session.getId());
            if (battleSessions.isEmpty()) {
                sessionsByBattleId.remove(battleId);
            }
        }

        LOGGER.info("WebSocket connection closed for battle: " + battleId + ", session: " + session.getId());
    }

    /**
     * Called when an error occurs in the WebSocket connection.
     *
     * @param session The WebSocket session
     * @param battleId The battle ID from the path parameter
     * @param throwable The error that occurred
     */
    @OnError
    public void onError(Session session, @PathParam("battleId") String battleId, Throwable throwable) {
        // Remove the session from the sessions map for this battle
        Map<String, Session> battleSessions = sessionsByBattleId.get(battleId);
        if (battleSessions != null) {
            battleSessions.remove(session.getId());
            if (battleSessions.isEmpty()) {
                sessionsByBattleId.remove(battleId);
            }
        }

        LOGGER.severe("WebSocket error for battle " + battleId
                + ", session " + session.getId() + ": " + throwable.getMessage());
    }

    /**
     * Called when a message is received from a client.
     * The client can request updates by sending "update" as a message.
     *
     * @param message The message received
     * @param session The WebSocket session
     * @param battleId The battle ID from the path parameter
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("battleId") String battleId) {
        LOGGER.info("Message from session " + session.getId() + " for battle " + battleId + ": " + message);

        // If the client requests an update, send the current battle state
        if ("update".equalsIgnoreCase(message)) {
            sendBattleState(battleId, session);
        }
    }

    /**
     * Sends the current battle state to a specific session.
     *
     * @param battleId The battle ID
     * @param session The WebSocket session to send the state to
     */
    private void sendBattleState(String battleId, Session session) {
        try {
            if (battleService.isValidBattleId(battleId)) {
                Battle battle = battleService.getBattleStatus(battleId);

                // Create a response with battle state information
                BattleStateResponse response = new BattleStateResponse();
                response.setBattleId(battle.getId());
                response.setBattleName(battle.getName());
                response.setArenaWidth(battle.getArenaWidth());
                response.setArenaHeight(battle.getArenaHeight());
                response.setRobotMovementTimeSeconds(battle.getRobotMovementTimeSeconds());
                response.setBattleState(battle.getState().toString());
                response.setRobots(battle.getRobots());
                response.setWalls(battle.getWalls());
                response.setWinnerId(battle.getWinnerId());
                response.setWinnerName(battle.getWinnerName());

                // Convert to JSON and send
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String jsonResponse = mapper.writeValueAsString(response);
                    session.getAsyncRemote().sendText(jsonResponse);
                } catch (JsonProcessingException e) {
                    LOGGER.severe("Error serializing battle state to JSON: " + e.getMessage());
                }
            } else {
                // Send an error message if the battle ID is invalid
                ErrorResponse error = new ErrorResponse("Invalid battle ID: " + battleId);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String jsonError = mapper.writeValueAsString(error);
                    session.getAsyncRemote().sendText(jsonError);
                } catch (JsonProcessingException e) {
                    LOGGER.severe("Error serializing error response to JSON: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error sending battle state for battle " + battleId + ": " + e.getMessage());

            // Send an error message
            ErrorResponse error = new ErrorResponse("Error retrieving battle state: " + e.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonError = mapper.writeValueAsString(error);
                session.getAsyncRemote().sendText(jsonError);
            } catch (JsonProcessingException jsonException) {
                LOGGER.severe("Error sending error message: " + jsonException.getMessage());
            }
        }
    }

    /**
     * Broadcasts the current battle state to all connected clients for a specific battle.
     * This can be called by other components to push updates to clients.
     *
     * @param battleId The battle ID
     */
    public void broadcastBattleState(String battleId) {
        Map<String, Session> battleSessions = sessionsByBattleId.get(battleId);
        if (battleSessions != null && !battleSessions.isEmpty()) {
            for (Session session : battleSessions.values()) {
                sendBattleState(battleId, session);
            }
        }
    }

    /**
     * Response class for battle state information.
     */
    public static class BattleStateResponse {
        private String battleId;
        private String battleName;
        private int arenaWidth;
        private int arenaHeight;
        private double robotMovementTimeSeconds;
        private String battleState;
        private java.util.List<Robot> robots;
        private java.util.List<Wall> walls;
        private String winnerId;
        private String winnerName;

        public BattleStateResponse() {
        }

        public String getBattleId() {
            return battleId;
        }

        public void setBattleId(String battleId) {
            this.battleId = battleId;
        }

        public String getBattleName() {
            return battleName;
        }

        public void setBattleName(String battleName) {
            this.battleName = battleName;
        }

        public int getArenaWidth() {
            return arenaWidth;
        }

        public void setArenaWidth(int arenaWidth) {
            this.arenaWidth = arenaWidth;
        }

        public int getArenaHeight() {
            return arenaHeight;
        }

        public void setArenaHeight(int arenaHeight) {
            this.arenaHeight = arenaHeight;
        }

        public double getRobotMovementTimeSeconds() {
            return robotMovementTimeSeconds;
        }

        public void setRobotMovementTimeSeconds(double robotMovementTimeSeconds) {
            this.robotMovementTimeSeconds = robotMovementTimeSeconds;
        }

        public String getBattleState() {
            return battleState;
        }

        public void setBattleState(String battleState) {
            this.battleState = battleState;
        }

        public java.util.List<Robot> getRobots() {
            return robots;
        }

        public void setRobots(java.util.List<Robot> robots) {
            this.robots = robots;
        }

        public java.util.List<Wall> getWalls() {
            return walls;
        }

        public void setWalls(java.util.List<Wall> walls) {
            this.walls = walls;
        }

        public String getWinnerId() {
            return winnerId;
        }

        public void setWinnerId(String winnerId) {
            this.winnerId = winnerId;
        }

        public String getWinnerName() {
            return winnerName;
        }

        public void setWinnerName(String winnerName) {
            this.winnerName = winnerName;
        }
    }

    /**
     * Error response class.
     */
    public static class ErrorResponse {
        private String error;

        public ErrorResponse() {
        }

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
