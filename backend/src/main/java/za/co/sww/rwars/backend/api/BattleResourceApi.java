package za.co.sww.rwars.backend.api;

import jakarta.ws.rs.Consumes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.model.Battle;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * API interface for battle creation and management operations.
 *
 * Contains all OpenAPI documentation and method signatures for battle-related endpoints.
 */
@Path("/api/battles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Battles", description = "Battle management operations")
public interface BattleResourceApi {

    /**
     * Gets all battles with their current state and robots (but not robot positions).
     *
     * @return A list of battle summaries
     */
    @GET
    @Operation(
        summary = "Retrieve all battles",
        description = "Gets a summary of all battles including the current state and participating robots."
    )
    @APIResponse(responseCode = "200", description = "List of battles retrieved",
        content = @Content(mediaType = "application/json",
        schema = @Schema(type = SchemaType.ARRAY, implementation = Battle.class)))
@APIResponse(responseCode = "500", description = "Internal server error",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response getAllBattles();

    /**
     * Creates a new battle with the given name and optional arena dimensions.
     *
     * @param request The battle creation request
     * @return The created battle
     */
    @POST
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
        schema = @Schema(implementation = HttpError.class),
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
        schema = @Schema(implementation = HttpError.class),
        examples = @ExampleObject(name = "ConflictError",
            summary = "Conflict error",
            description = "Example conflict error response",
            value = """
                {
                  "message": "Battle with this name already exists"
                }
                """)))
Response createBattle(
        @Valid
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
        CreateBattleRequest request);

    /**
     * Creates a new test battle which allows starting with a single robot registered.
     *
     * @param request The battle creation request
     * @return The created battle marked as test mode
     */
    @POST
    @Path("/test")
    @Operation(
        summary = "Create a new test battle",
        description = "Creates a developer test battle that can be started with a single robot."
    )
    @APIResponse(responseCode = "200", description = "Test battle created successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class),
        examples = @ExampleObject(name = "TestBattleCreated",
            summary = "Test battle information",
            description = "Example test battle response",
            value = """
                {
                  "id": "battle-test-123e4567-e89b-12d3-a456-556642440000",
                  "name": "Dev Test Battle",
                  "arenaWidth": 40,
                  "arenaHeight": 30,
                  "robotMovementTimeSeconds": 0.5,
                  "state": "WAITING_ON_ROBOTS",
                  "robots": [],
                  "walls": [],
                  "winnerId": null,
                  "winnerName": null,
                  "testMode": true
                }
                """)))
Response createTestBattle(
        @Valid
        @Parameter(description = "Battle creation details",
        content = @Content(examples = @ExampleObject(name = "CreateTestBattleRequest",
                summary = "Create a new test battle",
                description = "Example request to create a test battle",
                value = """
                    {
                      "name": "Dev Test Battle",
                      "width": 40,
                      "height": 30,
                      "robotMovementTimeSeconds": 0.5
                    }
                    """)))
        CreateBattleRequest request);

    /**
     * Starts a battle.
     *
     * @param battleId The battle ID
     * @return The current battle status
     */
    @POST
    @Path("/{battleId}/start")
    @Operation(
        summary = "Start a battle",
        description = "Begins a battle with the given battle ID."
    )
    @APIResponse(responseCode = "200", description = "Battle started successfully",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = Battle.class)))
@APIResponse(responseCode = "400", description = "Invalid battle ID",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
@APIResponse(responseCode = "409", description = "Battle cannot be started",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response startBattle(
            @Parameter(description = "ID of the battle to start") @PathParam("battleId") String battleId);

    /**
     * Deletes a completed battle and all associated data.
     *
     * @param battleId The battle ID to delete
     * @return Empty response with status 204 if successful
     */
    @DELETE
    @Path("/{battleId}")
    @Operation(
        summary = "Delete a completed battle",
        description = "Deletes a battle identified by battle ID if it has been completed."
    )
    @APIResponse(responseCode = "204", description = "Battle deleted successfully")
@APIResponse(responseCode = "404", description = "Battle not found",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
@APIResponse(responseCode = "400", description = "Bad request",
        content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = HttpError.class)))
    Response deleteBattle(
            @Parameter(description = "ID of the battle to delete") @PathParam("battleId") String battleId);

    /**
     * Battle creation request record.
     */
    @Schema(description = "Request for creating a new battle")
record CreateBattleRequest(
        @Schema(description = "Name of the battle", example = "Epic Robot Battle", required = true)
        @NotBlank String name,

        @Schema(description = "Width of the arena in grid units", example = "50", minimum = "10", maximum = "1000")
        Integer width,

        @Schema(description = "Height of the arena in grid units", example = "50", minimum = "10", maximum = "1000")
        Integer height,

        @Schema(description = "Time allowed for robot movement in seconds", example = "1.0", minimum = "0.1",
                maximum = "10.0")
        @DecimalMin("0.1") @DecimalMax("10.0") Double robotMovementTimeSeconds
    ) {
        public CreateBattleRequest() {
            this(null, null, null, null);
        }
    }
}
