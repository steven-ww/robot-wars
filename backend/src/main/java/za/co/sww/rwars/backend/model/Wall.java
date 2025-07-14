package za.co.sww.rwars.backend.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a wall in the battle arena.
 */
@Schema(description = "Wall obstacle in the battle arena")
public class Wall {

    @Schema(description = "Type of wall configuration")
    public enum WallType {
        SQUARE,     // 4x4 square
        LONG,       // 1x10 line
        U_SHAPE     // 4x10x4 U-shape
    }

    @Schema(description = "Type of the wall")
    private WallType type;
    
    @Schema(description = "List of positions occupied by this wall")
    private List<Position> positions;

    public Wall(WallType type) {
        this.type = type;
        this.positions = new ArrayList<>();
    }

    public Wall(WallType type, List<Position> positions) {
        this.type = type;
        this.positions = positions;
    }

    public WallType getType() {
        return type;
    }

    public void setType(WallType type) {
        this.type = type;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public void addPosition(int x, int y) {
        this.positions.add(new Position(x, y));
    }

    public boolean containsPosition(int x, int y) {
        return positions.stream().anyMatch(pos -> pos.getX() == x && pos.getY() == y);
    }

    @Schema(description = "Position coordinates of a wall segment")
    public static class Position {
        @Schema(description = "X coordinate of the wall position", example = "20")
        private int x;
        
        @Schema(description = "Y coordinate of the wall position", example = "25")
        private int y;

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
    }
}
