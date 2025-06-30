package za.co.sww.rwars.backend.rest;

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
import za.co.sww.rwars.backend.service.BattleService;

/**
 * REST API for robot registration and battle status checking.
 */
@Path("/api/robots")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RobotResource {

    @Inject
    private BattleService battleService;

    /**
     * Registers a robot for the battle.
     *
     * @param robot The robot to register
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register")
    public Response registerRobot(Robot robot) {
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
     * Gets the battle status.
     *
     * @param battleId The battle ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}")
    public Response getBattleStatus(@PathParam("battleId") String battleId) {
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
     * Starts the battle.
     *
     * @param battleId The battle ID
     * @return The battle status
     */
    @POST
    @Path("/battle/{battleId}/start")
    public Response startBattle(@PathParam("battleId") String battleId) {
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
