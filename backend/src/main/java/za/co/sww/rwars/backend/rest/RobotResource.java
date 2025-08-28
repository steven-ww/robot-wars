package za.co.sww.rwars.backend.rest;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.api.HttpError;
import za.co.sww.rwars.backend.api.RobotResourceApi;
import za.co.sww.rwars.backend.api.RobotResourceApi.MoveRequest;
import za.co.sww.rwars.backend.api.RobotResourceApi.RadarRequest;
import za.co.sww.rwars.backend.api.RobotResourceApi.LaserRequest;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.RobotStatus;
import za.co.sww.rwars.backend.model.RadarResponse;
import za.co.sww.rwars.backend.model.LaserResponse;
import za.co.sww.rwars.backend.service.BattleService;

/**
 * REST API implementation for robot registration and battle status checking.
 *
 * Provides functionality for adding robots to battles, checking their status, and performing in-game actions.
 */
public class RobotResource implements RobotResourceApi {

    @Inject
    private BattleService battleService;

    @RunOnVirtualThread
    @Override
    public Response registerRobot(Robot robot) {
        try {
            Robot registeredRobot = battleService.registerRobot(robot.getName());
            return Response.ok(registeredRobot).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
    public Response registerRobotForBattle(Robot robot, String battleId) {
        try {
            Robot registeredRobot = battleService.registerRobotForBattle(robot.getName(), battleId);
            return Response.ok(registeredRobot).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
    public Response getBattleStatus(String battleId) {
        try {
            if (!battleService.isValidBattleId(battleId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID"))
                        .build();
            }
            Battle battle = battleService.getBattleStatus(battleId);
            return Response.ok(battle).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
    public Response getBattleStatusForRobot(String battleId, String robotId) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID or robot ID"))
                        .build();
            }
            Battle battle = battleService.getBattleStatusForRobot(battleId, robotId);
            return Response.ok(battle).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
    public Response getRobotStatus(String battleId, String robotId) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID or robot ID"))
                        .build();
            }
            Robot robot = battleService.getRobotDetails(battleId, robotId);
            RobotStatus robotStatus = new RobotStatus(robot);
            return Response.ok(robotStatus).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
public Response moveRobot(String battleId, String robotId, MoveRequest moveRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID or robot ID"))
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
                    .entity(new HttpError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
public Response performRadarScan(String battleId, String robotId, RadarRequest radarRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID or robot ID"))
                        .build();
            }
            RadarResponse radarResponse = battleService.performRadarScan(
                    battleId,
                    robotId,
                    radarRequest.range());
            return Response.ok(radarResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }

    @RunOnVirtualThread
    @Override
public Response fireLaser(String battleId, String robotId, LaserRequest laserRequest) {
        try {
            if (!battleService.isValidBattleAndRobotId(battleId, robotId)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new HttpError("Invalid battle ID or robot ID"))
                        .build();
            }
            LaserResponse laserResponse = battleService.fireLaser(
                    battleId,
                    robotId,
                    laserRequest.direction());
            return Response.ok(laserResponse).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new HttpError(e.getMessage()))
                    .build();
        }
    }
}
