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
    @RunOnVirtualThread
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
    @RunOnVirtualThread
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
     * Gets the battle status for a specific robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The battle status
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}")
    @RunOnVirtualThread
    public Response getBattleStatusForRobot(@PathParam("battleId") String battleId,
                                            @PathParam("robotId") String robotId) {
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
     * Gets a specific robot's details.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The robot details
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}/details")
    @RunOnVirtualThread
    public Response getRobotDetails(@PathParam("battleId") String battleId,
                                    @PathParam("robotId") String robotId) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            Robot robot = battleService.getRobotDetails(battleId, robotId);
            return Response.ok(robot).build();
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
    @RunOnVirtualThread
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
     * Moves a robot in the specified direction for the specified number of blocks.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param moveRequest The move request containing direction and blocks
     * @return The robot with updated position
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/move")
    @RunOnVirtualThread
    public Response moveRobot(@PathParam("battleId") String battleId,
                              @PathParam("robotId") String robotId,
                              MoveRequest moveRequest) {
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
     * Error response record.
     */
    public record ErrorResponse(String message) {
        public ErrorResponse() {
            this(null);
        }
    }

    /**
     * Move request record.
     */
    public record MoveRequest(String direction, int blocks) {
        public MoveRequest() {
            this(null, 0);
        }
    }

}
