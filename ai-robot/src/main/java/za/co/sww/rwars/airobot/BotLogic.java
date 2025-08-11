package za.co.sww.rwars.airobot;

import za.co.sww.rwars.airobot.model.RadarResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class BotLogic {
    private BotLogic() {}

    public static boolean isAlignedForLaser(int dx, int dy) {
        return dx == 0 || dy == 0 || Math.abs(dx) == Math.abs(dy);
    }

    public static String directionToward(int dx, int dy) {
        if (dx == 0 && dy == 0) return null;
        var sx = Integer.compare(dx, 0); // -1, 0, 1
        var sy = Integer.compare(dy, 0);
        return switch (sx + "," + sy) {
            case "0,1" -> "NORTH";
            case "0,-1" -> "SOUTH";
            case "1,0" -> "EAST";
            case "-1,0" -> "WEST";
            case "1,1" -> "NE";
            case "-1,1" -> "NW";
            case "1,-1" -> "SE";
            case "-1,-1" -> "SW";
            default -> null;
        };
    }

    public static boolean isStepBlocked(String dir, List<RadarResponse.Detection> detections) {
        int[] v = unit(dir);
        if (v == null) return true; // unknown dir -> treat as blocked
        int ux = v[0], uy = v[1];
        for (var d : detections) {
            if (!"WALL".equals(d.type())) continue;
            if (d.x() == ux && d.y() == uy) return true; // immediate obstacle
            var det = d.details();
            if (det != null && det.toLowerCase().contains("boundary")) {
                // Boundary lines may be reported along edges; be conservative if step matches boundary vector
                if (d.x() == ux && (d.y() == uy || uy == 0)) return true;
                if (d.y() == uy && (d.x() == ux || ux == 0)) return true;
            }
        }
        return false;
    }

    public static String chooseSafeDirectionToward(int dx, int dy, List<RadarResponse.Detection> detections) {
        // Build candidate directions sorted by how much they reduce distance to target
        record Cand(String dir, int nx, int ny, int score) {}
        List<Cand> cands = new ArrayList<>();
        for (var dir : ALL_DIRS) {
            int[] v = unit(dir);
            int nx = dx - v[0];
            int ny = dy - v[1];
            int score = Math.abs(nx) + Math.abs(ny); // lower is better (Manhattan distance after step)
            cands.add(new Cand(dir, nx, ny, score));
        }
        cands.sort(Comparator.comparingInt(c -> c.score));
        for (var c : cands) {
            if (!isStepBlocked(c.dir, detections)) return c.dir;
        }
        return null;
    }

    public static String chooseAnySafeDirection(List<RadarResponse.Detection> detections, Random rng) {
        List<String> dirs = new ArrayList<>(ALL_DIRS);
        Collections.shuffle(dirs, rng);
        for (var d : dirs) {
            if (!isStepBlocked(d, detections)) return d;
        }
        return null;
    }

    private static final List<String> ALL_DIRS = List.of("NORTH","SOUTH","EAST","WEST","NE","NW","SE","SW");

    private static int[] unit(String dir) {
        return switch (dir) {
            case "NORTH" -> new int[]{0, 1};
            case "SOUTH" -> new int[]{0, -1};
            case "EAST"  -> new int[]{1, 0};
            case "WEST"  -> new int[]{-1, 0};
            case "NE"    -> new int[]{1, 1};
            case "NW"    -> new int[]{-1, 1};
            case "SE"    -> new int[]{1, -1};
            case "SW"    -> new int[]{-1, -1};
            default -> null;
        };
    }
}
