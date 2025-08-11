package za.co.sww.rwars.airobot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import za.co.sww.rwars.airobot.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class RwApiClient {
    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    RwApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    Battle createBattle(CreateBattleRequest req) throws IOException, InterruptedException {
        return postJson("/api/battles", req, Battle.class);
    }

    Battle createTestBattle(CreateBattleRequest req) throws IOException, InterruptedException {
        return postJson("/api/battles/test", req, Battle.class);
    }

    Battle startBattle(String battleId) throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/battles/" + battleId + "/start"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(res);
        return mapper.readValue(res.body(), Battle.class);
    }

    Robot registerRobotForBattle(String battleId, RobotRegisterRequest req) throws IOException, InterruptedException {
        return postJson("/api/robots/register/" + battleId, req, Robot.class);
    }

    Battle getBattleStatus(String battleId) throws IOException, InterruptedException {
        return get("/api/robots/battle/" + battleId, Battle.class);
    }

    Battle getBattleStatusForRobot(String battleId, String robotId) throws IOException, InterruptedException {
        return get("/api/robots/battle/" + battleId + "/robot/" + robotId, Battle.class);
    }

    RobotStatus getRobotStatus(String battleId, String robotId) throws IOException, InterruptedException {
        return get("/api/robots/battle/" + battleId + "/robot/" + robotId + "/status", RobotStatus.class);
    }

    Robot move(String battleId, String robotId, MoveRequest req) throws IOException, InterruptedException {
        return postJson("/api/robots/battle/" + battleId + "/robot/" + robotId + "/move", req, Robot.class);
    }

    RadarResponse radar(String battleId, String robotId, RadarRequest req) throws IOException, InterruptedException {
        return postJson("/api/robots/battle/" + battleId + "/robot/" + robotId + "/radar", req, RadarResponse.class);
    }

    LaserResponse laser(String battleId, String robotId, LaserRequest req) throws IOException, InterruptedException {
        return postJson("/api/robots/battle/" + battleId + "/robot/" + robotId + "/laser", req, LaserResponse.class);
    }

    private <T> T get(String path, Class<T> type) throws IOException, InterruptedException {
        var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(res);
        return mapper.readValue(res.body(), type);
    }

    private <T> T postJson(String path, Object body, Class<T> type) throws IOException, InterruptedException {
        var json = mapper.writeValueAsString(body);
        var req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = http.send(req, HttpResponse.BodyHandlers.ofString());
        ensure2xx(res);
        return mapper.readValue(res.body(), type);
    }

    private void ensure2xx(HttpResponse<?> res) {
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + ": " + res.body());
        }
    }
}
