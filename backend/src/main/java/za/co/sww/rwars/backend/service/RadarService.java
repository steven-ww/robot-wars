package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import za.co.sww.rwars.backend.model.Battle;
import za.co.sww.rwars.backend.model.Robot;
import za.co.sww.rwars.backend.model.RadarResponse;
import za.co.sww.rwars.backend.model.Wall;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for robot radar functionality.
 */
@ApplicationScoped
public class RadarService {

    @ConfigProperty(name = "battle.radar.default-range", defaultValue = "5")
    private int defaultRange;

    @ConfigProperty(name = "battle.radar.max-range", defaultValue = "20")
    private int maxRange;

    /**
     * Scan the area around a robot for obstacles and other robots.
     * Returns coordinates relative to the robot's position.
     */
    public RadarResponse scanArea(Battle battle, Robot robot, int range) {
        List<RadarResponse.Detection> detections = new ArrayList<>();

        // Ensure range is within limits
        range = Math.min(range, maxRange);

        int robotX = robot.getPositionX();
        int robotY = robot.getPositionY();

        // Scan within the specified range
        for (int x = Math.max(0, robotX - range); x <= Math.min(battle.getArenaWidth() - 1, robotX + range); x++) {
            for (int y = Math.max(0, robotY - range); y <= Math.min(battle.getArenaHeight() - 1, robotY + range); y++) {
                // Skip the robot's own position
                if (x == robotX && y == robotY) {
                    continue;
                }

                // Check if position is within range (circular/diamond range)
                if (calculateDistance(robotX, robotY, x, y) <= range) {
                    boolean wallDetected = false;

                    // Check for arena edge walls (boundaries)
                    if (x == 0 || x == battle.getArenaWidth() - 1 || y == 0 || y == battle.getArenaHeight() - 1) {
                        // Convert absolute coordinates to relative coordinates
                        int relativeX = x - robotX;
                        int relativeY = y - robotY;
                        detections.add(new RadarResponse.Detection(relativeX, relativeY, RadarResponse.DetectionType.WALL,
                            "Arena boundary wall"));
                        wallDetected = true;
                    }

                    // Check for internal walls at this position (only if no boundary wall detected)
                    if (!wallDetected) {
                        for (Wall wall : battle.getWalls()) {
                            if (wall.containsPosition(x, y)) {
                                // Convert absolute coordinates to relative coordinates
                                int relativeX = x - robotX;
                                int relativeY = y - robotY;
                                detections.add(new RadarResponse.Detection(relativeX, relativeY, RadarResponse.DetectionType.WALL,
                                    "Wall of type " + wall.getType()));
                                wallDetected = true;
                                break;
                            }
                        }
                    }

                    // Check for other robots at this position (only if no wall detected)
                    if (!wallDetected) {
                        for (Robot otherRobot : battle.getRobots()) {
                            if (!otherRobot.getId().equals(robot.getId())
                                && otherRobot.getPositionX() == x && otherRobot.getPositionY() == y) {
                                // Convert absolute coordinates to relative coordinates
                                int relativeX = x - robotX;
                                int relativeY = y - robotY;
                                detections.add(new RadarResponse.Detection(relativeX, relativeY, RadarResponse.DetectionType.ROBOT,
                                    "Robot: " + otherRobot.getName()));
                            }
                        }
                    }
                }
            }
        }

        return new RadarResponse(range, detections);
    }

    /**
     * Calculate Manhattan distance between two points.
     */
    private int calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
