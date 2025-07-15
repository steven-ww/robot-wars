package za.co.sww.rwars.backend.model;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Response object for laser firing operations.
 */
@Schema(description = "Response from a laser firing operation, detailing results")
@RegisterForReflection
public class LaserResponse {
    @Schema(description = "Indicates if the laser hit a target")
    private boolean hit;

    @Schema(description = "ID of the robot that was hit", example = "robot-789")
    private String hitRobotId;

    @Schema(description = "Name of the robot that was hit", example = "TargetBot")
    private String hitRobotName;

    @Schema(description = "Amount of damage dealt by the laser", example = "20")
    private int damageDealt;

    @Schema(description = "Range of the laser fire", example = "10")
    private int range;

    @Schema(description = "Direction the laser was fired", example = "NORTH")
    private String direction;

    @Schema(description = "Path coordinates that the laser traveled")
    private List<Position> laserPath;

    @Schema(description = "Position where the laser hit")
    private Position hitPosition;

    @Schema(description = "Reason the laser was blocked (WALL, ROBOT, BOUNDARY)", example = "WALL")
    private String blockedBy;

    /**
     * Default constructor.
     */
    public LaserResponse() {
    }

    /**
     * Constructor for a miss.
     *
     * @param range The range of the laser
     * @param direction The direction the laser was fired
     * @param laserPath The path the laser traveled
     * @param blockedBy What blocked the laser (WALL, BOUNDARY, or null if max range reached)
     */
    public LaserResponse(int range, String direction, List<Position> laserPath, String blockedBy) {
        this.hit = false;
        this.range = range;
        this.direction = direction;
        this.laserPath = laserPath;
        this.blockedBy = blockedBy;
    }

    /**
     * Constructor for a hit.
     *
     * @param hitRobotId The ID of the robot that was hit
     * @param hitRobotName The name of the robot that was hit
     * @param damageDealt The amount of damage dealt
     * @param range The range of the laser
     * @param direction The direction the laser was fired
     * @param laserPath The path the laser traveled
     * @param hitPosition The position where the hit occurred
     */
    public LaserResponse(String hitRobotId, String hitRobotName, int damageDealt,
                        int range, String direction, List<Position> laserPath, Position hitPosition) {
        this.hit = true;
        this.hitRobotId = hitRobotId;
        this.hitRobotName = hitRobotName;
        this.damageDealt = damageDealt;
        this.range = range;
        this.direction = direction;
        this.laserPath = laserPath;
        this.hitPosition = hitPosition;
        this.blockedBy = "ROBOT";
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public String getHitRobotId() {
        return hitRobotId;
    }

    public void setHitRobotId(String hitRobotId) {
        this.hitRobotId = hitRobotId;
    }

    public String getHitRobotName() {
        return hitRobotName;
    }

    public void setHitRobotName(String hitRobotName) {
        this.hitRobotName = hitRobotName;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<Position> getLaserPath() {
        return laserPath;
    }

    public void setLaserPath(List<Position> laserPath) {
        this.laserPath = laserPath;
    }

    public Position getHitPosition() {
        return hitPosition;
    }

    public void setHitPosition(Position hitPosition) {
        this.hitPosition = hitPosition;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    /**
     * Represents a position in the arena.
     */
    @Schema(description = "Position coordinates")
    @RegisterForReflection
    public static class Position {
        @Schema(description = "X coordinate of the position", example = "5")
        private int x;

        @Schema(description = "Y coordinate of the position", example = "10")
        private int y;

        public Position() {
        }

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Position position = (Position) obj;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() {
            return "Position{x=" + x + ", y=" + y + '}';
        }
    }
}
