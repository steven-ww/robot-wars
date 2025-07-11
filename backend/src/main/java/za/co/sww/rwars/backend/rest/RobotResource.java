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
     * Registers a robot for the battle (uses first available battle).
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
     * Registers a robot for a specific battle.
     *
     * @param robot The robot to register
     * @param battleId The ID of the battle to join
     * @return The registered robot with battle ID
     */
    @POST
    @Path("/register/{battleId}")
    @RunOnVirtualThread
    public Response registerRobotForBattle(Robot robot, @PathParam("battleId") String battleId) {
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
     * Gets a specific robot's status without revealing its absolute position.
     * This is what robots should use to check their own status.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @return The robot status (without position information)
     */
    @GET
    @Path("/battle/{battleId}/robot/{robotId}/status")
    @RunOnVirtualThread
    public Response getRobotStatus(@PathParam("battleId") String battleId,
                                   @PathParam("robotId") String robotId) {
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
     * Performs a radar scan for a robot.
     *
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param radarRequest The radar request containing range
     * @return The radar response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/radar")
    @RunOnVirtualThread
    public Response performRadarScan(@PathParam("battleId") String battleId,
                                     @PathParam("robotId") String robotId,
                                     RadarRequest radarRequest) {
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
     * @param battleId The battle ID
     * @param robotId The robot ID
     * @param laserRequest The laser request containing direction and optional range
     * @return The laser response
     */
    @POST
    @Path("/battle/{battleId}/robot/{robotId}/laser")
    @RunOnVirtualThread
    public Response fireLaser(@PathParam("battleId") String battleId,
                              @PathParam("robotId") String robotId,
                              LaserRequest laserRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Invalid battle ID or robot ID"))
                        .build();
            }
            LaserResponse laserResponse = battleService.fireLaser(
                    battleId,
                    robotId,
                    laserRequest.direction(),
                    laserRequest.range());
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

    /**
     * Radar request record.
     */
    public record RadarRequest(int range) {
        public RadarRequest() {
            this(5);
        }
    }

    /**
     * Laser request record.
     */
    public record LaserRequest(String direction, int range) {
        public LaserRequest() {
            this(null, 10);
        }
    }

}
