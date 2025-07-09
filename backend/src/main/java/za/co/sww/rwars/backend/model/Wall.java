package za.co.sww.rwars.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a wall in the battle arena.
 */
public class Wall {

    public enum WallType {
        SQUARE,     // 4x4 square
        LONG,       // 1x10 line
        U_SHAPE     // 4x10x4 U-shape
    }

    private WallType type;
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

    public static class Position {
        private int x;
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
