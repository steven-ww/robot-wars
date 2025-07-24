package za.co.sww.rwars.backend.rest;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.service.BattleService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * REST API for battle creation and management.
 *
 * This resource provides endpoints for creating, starting, and managing battles in the Robot Wars game.
 * Battles are the core game sessions where robots compete against each other in a grid-based arena.
 */
@Path("/api/battles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Battles", description = "Battle management operations")
public class BattleResource {

    @Inject
    private BattleService battleService;

    /**
     * Gets all battles with their current state and robots (but not robot positions).
     *
     * Retrieves a list of all battles with their summary information including the current state and participating
     * robots.
     *
     * @return A list of battle summaries
     */
    @GET
    @RunOnVirtualThread
    @Operation(
        summary = "Retrieve all battles",
        description = "Gets a summary of all battles including the current state and participating robots."
    )
    @APIResponse(responseCode = "200", description = "List of battles retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(type = SchemaType.ARRAY, implementation = Battle.class)))
    @APIResponse(responseCode = "500", description = "Internal server error",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response getAllBattles() {
        try {
            var battleSummaries = battleService.getAllBattleSummaries();
            return Response.ok(battleSummaries).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving battles: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Creates a new battle with the given name and default arena dimensions.
     *
     * Allows the client to create a new battle with optional arena dimensions and robot movement time.
     *
     * @param request The battle creation request
     * @return The created battle
     */
    @POST
    @RunOnVirtualThread
    @Operation(
        summary = "Create a new battle",
        description = "Creates a new battle arena with a given name and optional dimensions. If dimensions are not "
                + "provided, default values from server configuration are used."
    )
    @APIResponse(responseCode = "200", description = "Battle created successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class),
        examples = @ExampleObject(name = "BattleCreated", 
            summary = "Battle information",
            description = "Example battle response with all details",
            value = """
                {
                  "id": "battle-123e4567-e89b-12d3-a456-556642440000",
                  "name": "Epic Robot Showdown",
                  "arenaWidth": 60,
                  "arenaHeight": 40,
                  "robotMovementTimeSeconds": 1.5,
                  "state": "WAITING_ON_ROBOTS",
                  "robots": [],
                  "walls": [],
                  "winnerId": null,
                  "winnerName": null
                }
                """)))
    @APIResponse(responseCode = "400", description = "Invalid input data",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ValidationError",
            summary = "Validation error",
            description = "Example validation error response",
            value = """
                {
                  "message": "Invalid arena dimensions. Width and height must be between 10 and 1000."
                }
                """)))
    @APIResponse(responseCode = "409", description = "Conflict in creating battle",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Battle with this name already exists"
                }
                """)))
    public Response createBattle(
        @Parameter(description = "Battle creation details",
        content = @Content(examples = @ExampleObject(name = "CreateBattleRequest",
                summary = "Create a new battle",
                description = "Example request to create a new battle with custom dimensions",
                value = """
                    {
                      "name": "Epic Robot Showdown",
                      "width": 60,
                      "height": 40,
                      "robotMovementTimeSeconds": 1.5
                    }
                    """)))
        CreateBattleRequest request) {
        try {
            Battle battle;
            if (request.width() != null && request.height() != null
                    && request.robotMovementTimeSeconds() != null) {
                battle = battleService.createBattle(request.name(), request.width(), request.height(),
                        request.robotMovementTimeSeconds());
            } else if (request.width() != null && request.height() != null) {
                battle = battleService.createBattle(request.name(), request.width(), request.height());
            } else if (request.robotMovementTimeSeconds() != null) {
                battle = battleService.createBattle(request.name(), request.robotMovementTimeSeconds());
            } else {
                battle = battleService.createBattle(request.name());
            }
            return Response.ok(battle).build();
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
     * Starts the battle.
     *
     * Initiates the specified battle, transitioning it from READY to IN_PROGRESS status.
     *
     * @param battleId The battle ID
     * @return The current battle status
     */
    @POST
    @Path("/{battleId}/start")
    @RunOnVirtualThread
    @Operation(
        summary = "Start a battle",
        description = "Begins a battle with the given battle ID."
    )
    @APIResponse(responseCode = "200", description = "Battle started successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
    @APIResponse(responseCode = "400", description = "Invalid battle ID",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "409", description = "Battle cannot be started",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response startBattle(
            @Parameter(description = "ID of the battle to start") @PathParam("battleId") String battleId) {
        try {
            Battle battle = battleService.startBattle(battleId);
            return Response.ok(battle).build();
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
     * Deletes a completed battle and all associated data.
     *
     * Removes a battle and its data once it has been completed, identified by the provided battle ID.
     *
     * @param battleId The battle ID to delete
     * @return Empty response with status 204 if successful
     */
    @DELETE
    @Path("/{battleId}")
    @RunOnVirtualThread
    @Operation(
        summary = "Delete a completed battle",
        description = "Deletes a battle identified by battle ID if it has been completed."
    )
    @APIResponse(responseCode = "204", description = "Battle deleted successfully")
    @APIResponse(responseCode = "404", description = "Battle not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    @APIResponse(responseCode = "400", description = "Bad request",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponse.class)))
    public Response deleteBattle(
            @Parameter(description = "ID of the battle to delete") @PathParam("battleId") String battleId) {
        try {
            battleService.deleteBattle(battleId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    /**
     * Battle creation request record.
     */
    @Schema(description = "Request for creating a new battle")
    @RegisterForReflection
    public record CreateBattleRequest(
        @Schema(description = "Name of the battle", example = "Epic Robot Battle", required = true)
        String name,

        @Schema(description = "Width of the arena in grid units", example = "50", minimum = "10", maximum = "100")
        Integer width,

        @Schema(description = "Height of the arena in grid units", example = "50", minimum = "10", maximum = "100")
        Integer height,

        @Schema(description = "Time allowed for robot movement in seconds", example = "1.0", minimum = "0.1",
                maximum = "10.0")
        Double robotMovementTimeSeconds
    ) {
        public CreateBattleRequest() {
            this(null, null, null, null);
        }
    }

    /**
     * Error response record.
     */
    @Schema(description = "Error response containing error message")
    @RegisterForReflection
    public record ErrorResponse(
        @Schema(description = "Error message describing what went wrong", example = "Battle not found")
        String message
    ) {
        public ErrorResponse() {
            this(null);
        }
    }
}
