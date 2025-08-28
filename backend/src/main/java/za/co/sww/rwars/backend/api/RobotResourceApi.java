package za.co.sww.rwars.backend.api;

import jakarta.ws.rs.Consumes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.RobotStatus;
import za.co.sww.rwars.backend.model.RadarResponse;
import za.co.sww.rwars.backend.model.LaserResponse;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * API interface for robot registration and battle status operations.
 *
 * Contains all OpenAPI documentation and method signatures for robot-related endpoints.
 */
@Path("/api/robots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Robots", description = "Robot management and action operations")
public interface RobotResourceApi {

    /**
     * Registers a robot for the battle (uses first available battle).
     *
     * @param robot The robot to register
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register")
    @Operation(
        summary = "Register a robot",
        description = "Registers a new robot for the first available battle. The robot will be automatically assigned "
                + "to an existing battle or a new one will be created."
    )
    @APIResponse(responseCode = "200", description = "Robot registered successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class),
        examples = @ExampleObject(name = "RegisteredRobot",
            summary = "Robot information",
            description = "Example robot response with position and status",
            value = """
                {
                  "id": "robot-456def78-9abc-123d-e456-789012345678",
                  "name": "DestroyerBot",
                  "battleId": "battle-123e4567-e89b-12d3-a456-556642440000",
                  "positionX": 25,
                  "positionY": 30,
                  "direction": "NORTH",
                  "status": "IDLE",
                  "targetBlocks": 0,
                  "blocksRemaining": 0,
                  "hitPoints": 100,
                  "maxHitPoints": 100
                }
                """)))
@APIResponse(responseCode = "409", description = "Conflict in registering robot",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Robot name already exists in battle"
                }
                """)))
    Response registerRobot(@Parameter(description = "Robot registration details",
        content = @Content(examples = @ExampleObject(name = "RegisterRobotRequest",
                summary = "Register a robot",
                description = "Example request to register a new robot",
                value = """
                    {
                      "name": "DestroyerBot"
                    }
                    """))) Robot robot);

    /**
     * Registers a robot for a specific battle.
     *
     * @param robot The robot to register
     * @param battleId The ID of the battle to join
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register/{battleId}")
    @Operation(
        summary = "Register a robot for a specific battle",
        description = "Registers a new robot to a specified battle using the battle ID."
    )
    @APIResponse(responseCode = "200", description = "Robot registered for the battle successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class)))
@APIResponse(responseCode = "400", description = "Invalid battle ID",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
@APIResponse(responseCode = "409", description = "Conflict in registration",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response registerRobotForBattle(
        @Parameter(description = "Details of the robot to register") Robot robot,
        @Parameter(description = "ID of the battle to join") @PathParam("battleId") String battleId);

    /**
     * Gets the battle status.
     *
     * @param battleId The battle ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}")
    @Operation(
        summary = "Get the status of a battle",
        description = "Retrieves the status of a battle using the provided battle ID."
    )
    @APIResponse(responseCode = "200", description = "Battle status retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
@APIResponse(responseCode = "400", description = "Invalid battle ID provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response getBattleStatus(
        @Parameter(description = "ID of the battle to retrieve status for") @PathParam("battleId") String battleId);

    /**
     * Gets the battle status for a specific robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}")
    @Operation(
        summary = "Get battle status for a robot",
        description = "Retrieves the battle status for a specific robot using the provided battle and robot ID."
    )
    @APIResponse(responseCode = "200", description = "Battle status for robot retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
@APIResponse(responseCode = "400", description = "Invalid IDs provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response getBattleStatusForRobot(
        @Parameter(description = "ID of the battle to retrieve status for") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId);

    /**
     * Gets a specific robot's status without revealing its absolute position.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The robot status (without position information)
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}/status")
    @Operation(
        summary = "Get robot status",
        description = "Retrieves the status of a robot in the battle without revealing its position."
    )
    @APIResponse(responseCode = "200", description = "Robot status retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = RobotStatus.class)))
@APIResponse(responseCode = "400", description = "Invalid robot or battle ID provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response getRobotStatus(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId);

    /**
     * Moves a robot in the specified direction for the specified number of blocks.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param moveRequest The move request containing direction and blocks
     * @return The robot with updated position
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/move")
    @Operation(
        summary = "Move a robot",
        description = "Moves a robot the specified number of blocks in a given direction. The robot will move "
                + "asynchronously over time, with each block taking the configured movement time."
    )
    @APIResponse(responseCode = "200", description = "Robot movement initiated successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class),
        examples = @ExampleObject(name = "RobotMoving",
            summary = "Robot information",
            description = "Example robot response with position and status",
            value = """
                {
                  "id": "robot-456def78-9abc-123d-e456-789012345678",
                  "name": "DestroyerBot",
                  "battleId": "battle-123e4567-e89b-12d3-a456-556642440000",
                  "positionX": 25,
                  "positionY": 30,
                  "direction": "NORTH",
                  "status": "MOVING",
                  "targetBlocks": 3,
                  "blocksRemaining": 2,
                  "hitPoints": 100,
                  "maxHitPoints": 100
                }
                """)))
@APIResponse(responseCode = "400", description = "Invalid IDs or move parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ValidationError",
            summary = "Validation error",
            description = "Example validation error response",
            value = """
                {
                  "message": "Invalid direction. Must be one of: NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW"
                }
                """)))
@APIResponse(responseCode = "409", description = "Robot cannot move",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Robot cannot move - path is blocked by a wall"
                }
                """)))
Response moveRobot(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Valid
        @Parameter(description = "Movement request parameters",
        content = @Content(examples = @ExampleObject(name = "MoveRequest",
                summary = "Move robot request",
                description = "Example request to move a robot",
                value = """
                    {
                      "direction": "NORTH",
                      "blocks": 3
                    }
                    """))) MoveRequest moveRequest);

    /**
     * Performs a radar scan for a robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param radarRequest The radar request containing range
     * @return The radar response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/radar")
    @Operation(
        summary = "Perform a radar scan",
        description = "Executes a radar scan for a robot to detect nearby objects within the specified range. "
                + "Returns all detected walls and robots relative to the scanning robot's position."
    )
    @APIResponse(responseCode = "200", description = "Radar scan completed successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = RadarResponse.class),
        examples = @ExampleObject(name = "RadarResponse",
            summary = "Radar scan results",
            description = "Example radar scan response with detections",
            value = """
                {
                  "range": 8,
                  "detections": [
                    {
                      "x": 2,
                      "y": -3,
                      "type": "WALL",
                      "details": "Wall segment detected"
                    },
                    {
                      "x": 5,
                      "y": 5,
                      "type": "ROBOT",
                      "details": "Enemy robot: CrusherBot"
                    }
                  ]
                }
                """)))
@APIResponse(responseCode = "400", description = "Invalid IDs or radar parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ValidationError",
            summary = "Validation error",
            description = "Example validation error response",
            value = """
                {
                  "message": "Invalid radar range. Must be between 1 and 20."
                }
                """)))
@APIResponse(responseCode = "409", description = "Radar operation failed",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Robot is destroyed and cannot perform radar scan"
                }
                """)))
Response performRadarScan(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Valid
        @Parameter(description = "Radar scan parameters",
        content = @Content(examples = @ExampleObject(name = "RadarRequest",
                summary = "Radar scan request",
                description = "Example request to perform a radar scan",
                value = """
                    {
                      "range": 8
                    }
                    """))) RadarRequest radarRequest);

    /**
     * Fires a laser for a robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param laserRequest The laser request containing direction
     * @return The laser response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/laser")
    @Operation(
        summary = "Fire a laser",
        description = "Fires a laser from the robot in the specified direction using the configured "
                + "laser range. The laser travels until it hits a wall, robot, or reaches the "
                + "configured maximum range."
    )
    @APIResponse(responseCode = "200", description = "Laser fired successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = LaserResponse.class),
        examples = {
            @ExampleObject(name = "LaserHit",
                summary = "Laser hit response",
                description = "Example response when laser hits a robot",
                value = """
                    {
                      "hit": true,
                      "hitRobotId": "robot-789abc12-def3-456g-h789-123456789012",
                      "hitRobotName": "TargetBot",
                      "damageDealt": 20,
                      "range": 5,
                      "direction": "EAST",
                      "laserPath": [
                        {"x": 0, "y": 0},
                        {"x": 1, "y": 0},
                        {"x": 2, "y": 0},
                        {"x": 3, "y": 0}
                      ],
                      "hitPosition": {"x": 3, "y": 0},
                      "blockedBy": "ROBOT"
                    }
                    """),
            @ExampleObject(name = "LaserMiss",
                summary = "Laser miss response",
                description = "Example response when laser misses or is blocked",
                value = """
                    {
                      "hit": false,
                      "hitRobotId": null,
                      "hitRobotName": null,
                      "damageDealt": 0,
                      "range": 5,
                      "direction": "EAST",
                      "laserPath": [
                        {"x": 0, "y": 0},
                        {"x": 1, "y": 0},
                        {"x": 2, "y": 0},
                        {"x": 3, "y": 0},
                        {"x": 4, "y": 0}
                      ],
                      "hitPosition": null,
                      "blockedBy": "WALL"
                    }
                    """)
        }))
@APIResponse(responseCode = "400", description = "Invalid IDs or laser parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ValidationError",
            summary = "Validation error",
            description = "Example validation error response",
            value = """
                {
                  "message": "Invalid direction. Must be one of: NORTH, SOUTH, EAST, WEST, NE, NW, SE, SW"
                }
                """)))
@APIResponse(responseCode = "409", description = "Laser operation failed",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Robot is destroyed and cannot fire laser"
                }
                """)))
Response fireLaser(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Valid
        @Parameter(description = "Laser firing parameters",
        content = @Content(examples = @ExampleObject(name = "LaserRequest",
                summary = "Fire laser request",
                description = "Example request to fire a laser",
                value = """
                    {
                      "direction": "EAST"
                    }
                    """))) LaserRequest laserRequest);

    /**
     * Move request record.
     */
    @Schema(description = "Request for moving a robot")
    @RegisterForReflection
record MoveRequest(
        @Schema(description = "Direction to move the robot",
                example = "NORTH",
                enumeration = {"NORTH", "SOUTH", "EAST", "WEST", "NE", "NW", "SE", "SW"})
        @NotBlank String direction,

        @Schema(description = "Number of blocks to move",
                example = "3",
                minimum = "1",
                maximum = "10")
        @Min(1) @Max(10) int blocks
    ) {
        public MoveRequest() {
            this(null, 0);
        }
    }

    /**
     * Radar request record.
     */
    @Schema(description = "Request for performing a radar scan")
    @RegisterForReflection
record RadarRequest(
        @Schema(description = "Range of the radar scan in grid units",
                example = "5",
                minimum = "1",
                maximum = "20")
        @Min(1) @Max(20) int range
    ) {
        public RadarRequest() {
            this(5);
        }
    }

    /**
     * Laser request record.
     */
    @Schema(description = "Request for firing a laser")
    @RegisterForReflection
record LaserRequest(
        @Schema(description = "Direction to fire the laser",
                example = "NORTH",
                enumeration = {"NORTH", "SOUTH", "EAST", "WEST", "NE", "NW", "SE", "SW"})
        @NotBlank String direction
    ) {
        public LaserRequest() {
            this(null);
        }
    }
}
