package za.co.sww.rwars.airobot;

import za.co.sww.rwars.airobot.model.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Modes {
    private static final Random RNG = new Random();

    public static void runDevMode(RwApiClient api, String name, boolean twoRobots, int maxSteps, int maxSeconds, int statusEvery) throws Exception {
        System.out.println("[DEV] Creating test battle...");
        var uniqueName = name + " Test " + java.time.Instant.now().toEpochMilli();
        var battle = api.createTestBattle(new CreateBattleRequest(uniqueName, null, null, 0.5));
        System.out.println("[DEV] Created battle " + battle.id());

        System.out.println("[DEV] Registering robot...");
        var robot = api.registerRobotForBattle(battle.id(), new RobotRegisterRequest(name));
        System.out.println("[DEV] Registered robot " + robot.id());

        String other = null;
        if (twoRobots) {
            var otherName = name + "_Opponent";
            var r2 = api.registerRobotForBattle(battle.id(), new RobotRegisterRequest(otherName));
            other = r2.id();
            System.out.println("[DEV] Registered second robot " + other);
        }

        maybeStartBattle(api, battle.id());

        if (twoRobots && other != null) {
            playLoopDuel(api, battle.id(), robot.id(), other, maxSteps, maxSeconds, statusEvery, true);
        } else {
            playLoopSingle(api, battle.id(), robot.id(), maxSteps, maxSeconds, statusEvery);
        }
    }

    public static void runSelfPlayMode(RwApiClient api, String baseName, int maxSteps, int maxSeconds, int statusEvery) throws Exception {
        System.out.println("[SELF] Creating battle...");
        var battle = api.createBattle(new CreateBattleRequest(baseName + " vs " + baseName + " (self)", 20, 20, 0.5));
        System.out.println("[SELF] Created battle " + battle.id());

        System.out.println("[SELF] Registering two robots...");
        var r1 = api.registerRobotForBattle(battle.id(), new RobotRegisterRequest(baseName + "_A"));
        var r2 = api.registerRobotForBattle(battle.id(), new RobotRegisterRequest(baseName + "_B"));

        maybeStartBattle(api, battle.id());

        playLoopDuel(api, battle.id(), r1.id(), r2.id(), maxSteps, maxSeconds, statusEvery, false);
    }

    private static void maybeStartBattle(RwApiClient api, String battleId) throws Exception {
        var status = api.getBattleStatus(battleId);
        if (status.state().equals("READY")) {
            System.out.println("[INFO] Starting battle..." );
            api.startBattle(battleId);
        }
    }

    private static void playLoopSingle(RwApiClient api, String battleId, String robotId, int maxSteps, int maxSeconds, int statusEvery) throws Exception {
        final long deadlineNanos = System.nanoTime() + maxSeconds * 1_000_000_000L;
        int step = 0;
        while (true) {
            if (step >= maxSteps || System.nanoTime() > deadlineNanos) {
                System.out.println("[INFO] Stopping dev loop: step/time limit reached");
                break;
            }
            var status = api.getBattleStatusForRobot(battleId, robotId);
            if (status.state().equals("COMPLETED")) {
                System.out.println("[DEV] Battle completed. Winner: " + status.winnerName());
                break;
            }
            var rs = api.getRobotStatus(battleId, robotId);
            if (!rs.isActive()) {
                System.out.println("[DEV] Robot inactive (" + rs.status() + "). Exiting.");
                break;
            }
            if (statusEvery > 0 && (step % statusEvery) == 0) {
                System.out.println("[STATUS] Robot=" + robotId + " HP=" + rs.hitPoints() + "/" + rs.maxHitPoints() + " State=" + rs.status());
            }
            stepAI(api, battleId, robotId, status);
            step++;
        }
    }

    private static void playLoopDuel(RwApiClient api, String battleId, String r1, String r2, int maxSteps, int maxSeconds, int statusEvery, boolean dev) throws Exception {
        final long deadlineNanos = System.nanoTime() + maxSeconds * 1_000_000_000L;
        int loop = 0;
        while (true) {
            if (loop >= maxSteps || System.nanoTime() > deadlineNanos) {
                System.out.println("[INFO] Stopping duel loop: step/time limit reached");
                break;
            }
            var status = api.getBattleStatus(battleId);
            if (status.state().equals("COMPLETED")) {
                System.out.println((dev ? "[DEV]" : "[SELF]") + " Battle completed. Winner: " + status.winnerName());
                break;
            }
            // Alternate turns lightly
            if ((loop % 2) == 0) {
                var rs = api.getRobotStatus(battleId, r1);
                if (statusEvery > 0 && (loop % statusEvery) == 0) {
                    System.out.println("[STATUS] R1=" + r1 + " HP=" + rs.hitPoints() + "/" + rs.maxHitPoints() + " State=" + rs.status());
                }
                if (rs.isActive()) stepAI(api, battleId, r1, status);
            } else {
                var rs = api.getRobotStatus(battleId, r2);
                if (statusEvery > 0 && (loop % statusEvery) == 0) {
                    System.out.println("[STATUS] R2=" + r2 + " HP=" + rs.hitPoints() + "/" + rs.maxHitPoints() + " State=" + rs.status());
                }
                if (rs.isActive()) stepAI(api, battleId, r2, status);
            }
            loop++;
        }
    }

    private static void stepAI(RwApiClient api, String battleId, String robotId, Battle status) throws Exception {
        // Simple AI: try radar; if an enemy is roughly aligned, fire; else move toward nearest detection or random
        var radar = api.radar(battleId, robotId, new RadarRequest(5));
        var nearestRobot = radar.detections().stream()
                .filter(d -> d.type().equals("ROBOT"))
                .min(Comparator.comparingInt(d -> Math.abs(d.x()) + Math.abs(d.y())));

        if (nearestRobot.isPresent()) {
            var d = nearestRobot.get();
            // If aligned perfectly on cardinal/diagonal, try to fire
            if (BotLogic.isAlignedForLaser(d.x(), d.y())) {
                var fireDir = BotLogic.directionToward(d.x(), d.y());
                System.out.println("[AI] Firing " + fireDir + " at target " + d);
                api.laser(battleId, robotId, new LaserRequest(fireDir));
                Thread.sleep(200); // small pacing
                return;
            }
            // Move toward target, but avoid walls/boundaries
            var safeDir = BotLogic.chooseSafeDirectionToward(d.x(), d.y(), radar.detections());
            if (safeDir != null) {
                System.out.println("[AI] Moving " + safeDir + " toward target " + d);
                api.move(battleId, robotId, new MoveRequest(safeDir, 1));
                waitUntilIdle(api, battleId, robotId);
                return;
            } else {
                var alt = BotLogic.chooseAnySafeDirection(radar.detections(), RNG);
                if (alt != null) {
                    System.out.println("[AI] Avoiding hazard; wandering " + alt);
                    api.move(battleId, robotId, new MoveRequest(alt, 1));
                    waitUntilIdle(api, battleId, robotId);
                    return;
                } else {
                    System.out.println("[AI] All directions blocked; skipping move");
                    return;
                }
            }
        }
        // No robot detected: wander, but avoid hazards
        var dir = BotLogic.chooseAnySafeDirection(radar.detections(), RNG);
        if (dir == null) {
            System.out.println("[AI] No safe direction to wander; skipping move");
            return;
        }
        System.out.println("[AI] Wandering " + dir);
        api.move(battleId, robotId, new MoveRequest(dir, 1));
        waitUntilIdle(api, battleId, robotId);
    }

    

    private static void waitUntilIdle(RwApiClient api, String battleId, String robotId) throws Exception {
        // Poll robot status until not MOVING, with small sleeps using virtual threads
        for (;;) {
            var rs = api.getRobotStatus(battleId, robotId);
            if (!"MOVING".equals(rs.status())) return;
            Thread.sleep(150);
        }
    }
}
