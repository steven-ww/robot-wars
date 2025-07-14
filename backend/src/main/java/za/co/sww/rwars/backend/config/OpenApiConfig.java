package za.co.sww.rwars.backend.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.core.Application;

/**
 * OpenAPI configuration for the Robot Wars API.
 */
@OpenAPIDefinition(
    info = @Info(
        title = "Robot Wars API",
        version = "1.0.0",
        description = """
            # Robot Wars Game API

            Welcome to the Robot Wars API! This is a multiplayer battle arena game where robots compete against each other in a grid-based arena.

            ## How to Play

            1. **Create a Battle**: Use the battles endpoint to create a new battle arena
            2. **Register Robots**: Register your robot to join a battle
            3. **Start the Battle**: Once enough robots are registered, start the battle
            4. **Control Your Robot**: Use the robot endpoints to move, scan with radar, and fire lasers
            5. **Win the Battle**: Be the last robot standing!

            ## Game Mechanics

            - **Arena**: Grid-based battlefield with configurable dimensions
            - **Movement**: Robots can move in 8 directions (N, S, E, W, NE, NW, SE, SW)
            - **Combat**: Robots can fire lasers to damage other robots
            - **Radar**: Scan the battlefield to detect walls and other robots
            - **Health**: Robots start with 100 hit points and are destroyed at 0 HP
            - **Walls**: Static obstacles that block movement and laser fire

            ## WebSocket Support

            The API also provides WebSocket endpoints for real-time battle updates and chat functionality.

            ## Getting Started

            Try the API using the interactive Swagger UI below, or integrate with your robot client application.
            """,
        contact = @Contact(
            name = "Robot Wars Support",
            email = "support@robotwars.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server"),
        @Server(url = "https://api.robotwars.com", description = "Production Server")
    },
    tags = {
        @Tag(name = "Battles", description = "Battle creation and management operations"),
        @Tag(name = "Robots", description = "Robot registration and control operations"),
        @Tag(name = "Game Actions", description = "In-game robot actions like movement, radar, and combat")
    }
)
public class OpenApiConfig extends Application {
}
