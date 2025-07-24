package za.co.sww.rwars.backend.rest;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
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
import za.co.sww.rwars.backend.service.BattleService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API for robot registration and battle status checking.
 *
 * Provides functionality for adding robots to battles, checking their status, and performing in-game actions.
 */
@Path("/api/robots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Robots", description = "Robot management and action operations")
public class RobotResource {

    @Inject
    private BattleService battleService;

    /**
     * Registers a robot for the battle (uses first available battle).
     *
     * Registers a robot to the next available battle, returning the robot details along with the battle ID.
     *
     * @param robot The robot to register
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register")
    @RunOnVirtualThread
    @Operation(
        summary = "Register a robot",
        description = "Registers a new robot for the first available battle. The robot will be automatically assigned "
                + "to an existing battle or a new one will be created."
    )
    @APIResponse(responseCode = "200", description = "Robot registered successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class),
        examples = @ExampleObject(name = "RegisteredRobot", ref = "#/components/examples/RobotResponse")))
    @APIResponse(responseCode = "409", description = "Conflict in registering robot",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ConflictError", ref = "#/components/examples/ConflictErrorResponse")))
    public Response registerRobot(@Parameter(description = "Robot registration details",
        content = @Content(examples = @ExampleObject(name = "RegisterRobotRequest",
                ref = "#/components/examples/RegisterRobotRequest"))) Robot robot) {
        try {
            Robot registeredRobot = battleService.registerRobot(robot.getName());
            return Response.ok(registeredRobot).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Registers a robot for a specific battle.
     *
     * Adds a new robot to a specified battle identified by the battle ID.
     *
     * @param robot The robot to register
     * @param battleId The ID of the battle to join
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register/{battleId}")
    @RunOnVirtualThread
    @Operation(
        summary = "Register a robot for a specific battle",
        description = "Registers a new robot to a specified battle using the battle ID."
    )
    @APIResponse(responseCode = "200", description = "Robot registered for the battle successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class)))
    @APIResponse(responseCode = "400", description = "Invalid battle ID",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "409", description = "Conflict in registration",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response registerRobotForBattle(
        @Parameter(description = "Details of the robot to register") Robot robot,
        @Parameter(description = "ID of the battle to join") @PathParam("battleId") String battleId) {
        try {
            Robot registeredRobot = battleService.registerRobotForBattle(robot.getName(), battleId);
            return Response.ok(registeredRobot).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Gets the battle status.
     *
     * Provides the status of a specific battle, including its current state and robot participants.
     *
     * @param battleId The battle ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}")
    @RunOnVirtualThread
    @Operation(
        summary = "Get the status of a battle",
        description = "Retrieves the status of a battle using the provided battle ID."
    )
    @APIResponse(responseCode = "200", description = "Battle status retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
    @APIResponse(responseCode = "400", description = "Invalid battle ID provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response getBattleStatus(
        @Parameter(description = "ID of the battle to retrieve status for") @PathParam("battleId") String battleId) {
        try {
            if (!battleService.isValidBattleId(battleId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID"))
                        .build();
            }
            Battle battle = battleService.getBattleStatus(battleId);
            return Response.ok(battle).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Gets the battle status for a specific robot.
     *
     * Provides the status of a battle for a particular robot, including relevant position and health information.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}")
    @RunOnVirtualThread
    @Operation(
        summary = "Get battle status for a robot",
        description = "Retrieves the battle status for a specific robot using the provided battle and robot ID."
    )
    @APIResponse(responseCode = "200", description = "Battle status for robot retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
    @APIResponse(responseCode = "400", description = "Invalid IDs provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response getBattleStatusForRobot(
        @Parameter(description = "ID of the battle to retrieve status for") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            Battle battle = battleService.getBattleStatusForRobot(battleId, robotId);
            return Response.ok(battle).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Gets a specific robot's status without revealing its absolute position.
     * This is what robots should use to check their own status.
     *
     * Retrieves the status of a specific robot, providing information on its health and actions available.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The robot status (without position information)
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}/status")
    @RunOnVirtualThread
    @Operation(
        summary = "Get robot status",
        description = "Retrieves the status of a robot in the battle without revealing its position."
    )
    @APIResponse(responseCode = "200", description = "Robot status retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = RobotStatus.class)))
    @APIResponse(responseCode = "400", description = "Invalid robot or battle ID provided",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response getRobotStatus(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            Robot robot = battleService.getRobotDetails(battleId, robotId);
            RobotStatus robotStatus = new RobotStatus(robot);
            return Response.ok(robotStatus).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }




    /**
     * Moves a robot in the specified direction for the specified number of blocks.
     *
     * Executes a movement action for a robot within a battle, updating its position.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param moveRequest The move request containing direction and blocks
     * @return The robot with updated position
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/move")
    @RunOnVirtualThread
    @Operation(
        summary = "Move a robot",
        description = "Moves a robot the specified number of blocks in a given direction. The robot will move "
                + "asynchronously over time, with each block taking the configured movement time."
    )
    @APIResponse(responseCode = "200", description = "Robot movement initiated successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Robot.class),
        examples = @ExampleObject(name = "RobotMoving", ref = "#/components/examples/RobotResponse")))
    @APIResponse(responseCode = "400", description = "Invalid IDs or move parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ValidationError", ref = "#/components/examples/ValidationErrorResponse")))
    @APIResponse(responseCode = "409", description = "Robot cannot move",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ConflictError", ref = "#/components/examples/ConflictErrorResponse")))
    public Response moveRobot(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Parameter(description = "Movement request parameters",
        content = @Content(examples = @ExampleObject(name = "MoveRequest",
                ref = "#/components/examples/MoveRequest"))) MoveRequest moveRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            Robot robot = battleService.moveRobot(
                    battleId,
                    robotId,
                    moveRequest.direction(),
                    moveRequest.blocks());
            return Response.ok(robot).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Performs a radar scan for a robot.
     *
     * Executes a radar scan for a robot, providing detection information on nearby walls and robots.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param radarRequest The radar request containing range
     * @return The radar response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/radar")
    @RunOnVirtualThread
    @Operation(
        summary = "Perform a radar scan",
        description = "Executes a radar scan for a robot to detect nearby objects within the specified range. "
                + "Returns all detected walls and robots relative to the scanning robot's position."
    )
    @APIResponse(responseCode = "200", description = "Radar scan completed successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = RadarResponse.class),
        examples = @ExampleObject(name = "RadarResponse", ref = "#/components/examples/RadarResponse")))
    @APIResponse(responseCode = "400", description = "Invalid IDs or radar parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ValidationError", ref = "#/components/examples/ValidationErrorResponse")))
    @APIResponse(responseCode = "409", description = "Radar operation failed",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ConflictError", ref = "#/components/examples/ConflictErrorResponse")))
    public Response performRadarScan(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Parameter(description = "Radar scan parameters",
        content = @Content(examples = @ExampleObject(name = "RadarRequest",
                ref = "#/components/examples/RadarRequest"))) RadarRequest radarRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            RadarResponse radarResponse = battleService.performRadarScan(
                    battleId,
                    robotId,
                    radarRequest.range());
            return Response.ok(radarResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Fires a laser for a robot.
     *
     * Executes a laser action for a robot in a specified direction using the configured laser range.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param laserRequest The laser request containing direction
     * @return The laser response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/laser")
    @RunOnVirtualThread
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
            @ExampleObject(name = "LaserHit", ref = "#/components/examples/LaserHitResponse"),
            @ExampleObject(name = "LaserMiss", ref = "#/components/examples/LaserMissResponse")
        }))
    @APIResponse(responseCode = "400", description = "Invalid IDs or laser parameters",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ValidationError", ref = "#/components/examples/ValidationErrorResponse")))
    @APIResponse(responseCode = "409", description = "Laser operation failed",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ConflictError", ref = "#/components/examples/ConflictErrorResponse")))
    public Response fireLaser(
        @Parameter(description = "ID of the battle the robot is in") @PathParam("battleId") String battleId,
        @Parameter(description = "ID of the robot") @PathParam("robotId") String robotId,
        @Parameter(description = "Laser firing parameters",
        content = @Content(examples = @ExampleObject(name = "LaserRequest",
                ref = "#/components/examples/LaserRequest"))) LaserRequest laserRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            LaserResponse laserResponse = battleService.fireLaser(
                    battleId,
                    robotId,
                    laserRequest.direction());
            return Response.ok(laserResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Error response record.
     */
    @Schema(description = "Error response containing error message")
    public record ErrorResponse(
        @Schema(description = "Error message describing what went wrong", example = "Invalid robot ID")
        String message
    ) {
        public ErrorResponse() {
            this(null);
        }
    }

    /**
     * Move request record.
     */
    @Schema(description = "Request for moving a robot")
    public record MoveRequest(
        @Schema(description = "Direction to move the robot",
                example = "NORTH",
                enumeration = {"NORTH", "SOUTH", "EAST", "WEST", "NE", "NW", "SE", "SW"})
        String direction,

        @Schema(description = "Number of blocks to move",
                example = "3",
                minimum = "1",
                maximum = "10")
        int blocks
    ) {
        public MoveRequest() {
            this(null, 0);
        }
    }

    /**
     * Radar request record.
     */
    @Schema(description = "Request for performing a radar scan")
    public record RadarRequest(
        @Schema(description = "Range of the radar scan in grid units",
                example = "5",
                minimum = "1",
                maximum = "20")
        int range
    ) {
        public RadarRequest() {
            this(5);
        }
    }

    /**
     * Laser request record.
     */
    @Schema(description = "Request for firing a laser")
    public record LaserRequest(
        @Schema(description = "Direction to fire the laser",
                example = "NORTH",
                enumeration = {"NORTH", "SOUTH", "EAST", "WEST", "NE", "NW", "SE", "SW"})
        String direction
    ) {
        public LaserRequest() {
            this(null);
        }
    }

}
