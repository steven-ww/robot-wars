package za.co.sww.rwars.backend.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.service.BattleService;

/**
 * REST API for battle creation and management.
 */
@Path("/api/battles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BattleResource {

    @Inject
    private BattleService battleService;

    /**
     * Creates a new battle with the given name and default arena dimensions.
     *
     * @param request The battle creation request
     * @return The created battle
     */
    @POST
    public Response createBattle(CreateBattleRequest request) {
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
     * Battle creation request record.
     */
    public record CreateBattleRequest(String name, Integer width, Integer height, Double robotMovementTimeSeconds) {
        public CreateBattleRequest() {
            this(null, null, null, null);
        }
    }

    /**
     * Error response record.
     */
    public record ErrorResponse(String message) {
        public ErrorResponse() {
            this(null);
        }
    }
}
