package za.co.sww.rwars.backend.rest;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import za.co.sww.rwars.backend.api.BattleResourceApi;
import za.co.sww.rwars.backend.api.BattleResourceApi.CreateBattleRequest;
import za.co.sww.rwars.backend.api.BattleResourceApi.ErrorResponse;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.service.BattleService;

/**
 * REST API for battle creation and management.
 *
 * Implements the BattleResourceApi interface to keep OpenAPI documentation and
 * request/response types defined in a single place (the API interface),
 * reducing duplication and improving maintainability.
 */
public class BattleResource implements BattleResourceApi {

    @Inject
    private BattleService battleService;

    @RunOnVirtualThread
    @Override
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

    @RunOnVirtualThread
    @Override
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

    @RunOnVirtualThread
    @Override
    public Response createTestBattle(CreateBattleRequest request) {
        try {
            Battle battle;
            if (request.width() != null && request.height() != null
                    && request.robotMovementTimeSeconds() != null) {
                battle = battleService.createTestBattle(request.name(), request.width(), request.height(),
                        request.robotMovementTimeSeconds());
            } else if (request.width() != null && request.height() != null) {
                battle = battleService.createTestBattle(request.name(), request.width(), request.height());
            } else if (request.robotMovementTimeSeconds() != null) {
                battle = battleService.createTestBattle(request.name(), request.robotMovementTimeSeconds());
            } else {
                battle = battleService.createTestBattle(request.name());
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

    @RunOnVirtualThread
    @Override
    public Response startBattle(String battleId) {
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

    @RunOnVirtualThread
    @Override
    public Response deleteBattle(String battleId) {
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
}
