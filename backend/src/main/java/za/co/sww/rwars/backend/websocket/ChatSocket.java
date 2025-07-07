package za.co.sww.rwars.backend.websocket;

import jakarta.enterprise.context.ApplicationScoped;
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

/**
 * A WebSocket endpoint for chat functionality.
 * This demonstrates basic WebSocket communication between clients.
 */
@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatSocket {

    private static final Logger LOGGER = Logger.getLogger(ChatSocket.class.getName());

    // Store active sessions
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Called when a new WebSocket connection is established.
     *
     * @param session The WebSocket session
     * @param username The username from the path parameter
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
        broadcast("User " + username + " joined the chat");
        LOGGER.info("New WebSocket connection: " + username);
    }

    /**
     * Called when a WebSocket connection is closed.
     *
     * @param session The WebSocket session
     * @param username The username from the path parameter
     */
    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        broadcast("User " + username + " left the chat");
        LOGGER.info("WebSocket connection closed: " + username);
    }

    /**
     * Called when an error occurs in the WebSocket connection.
     *
     * @param session The WebSocket session
     * @param username The username from the path parameter
     * @param throwable The error that occurred
     */
    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        LOGGER.severe("WebSocket error for user " + username + ": " + throwable.getMessage());
    }

    /**
     * Called when a message is received from a client.
     *
     * @param message The message received
     * @param username The username from the path parameter
     */
    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        broadcast(username + ": " + message);
        LOGGER.info("Message from " + username + ": " + message);
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    private void broadcast(String message) {
        sessions.values().forEach(session -> {
            session.getAsyncRemote().sendText(message);
        });
    }
}
