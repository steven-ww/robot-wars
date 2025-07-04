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
            if (request.getWidth() != null && request.getHeight() != null) {
                battle = battleService.createBattle(request.getName(), request.getWidth(), request.getHeight());
            } else {
                battle = battleService.createBattle(request.getName());
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
     * Battle creation request class.
     */
    public static class CreateBattleRequest {
        private String name;
        private Integer width;
        private Integer height;

        public CreateBattleRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }
    }

    /**
     * Error response class.
     */
    public static class ErrorResponse {
        private String message;

        public ErrorResponse() {
        }

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}