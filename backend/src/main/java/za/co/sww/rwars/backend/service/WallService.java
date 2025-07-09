package za.co.sww.rwars.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import za.co.sww.rwars.backend.model.Wall;
import za.co.sww.rwars.backend.model.Battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for generating random walls in the arena.
 */
@ApplicationScoped
public class WallService {

    @ConfigProperty(name = "battle.walls.max-coverage-percentage", defaultValue = "2")
    private int maxCoveragePercentage;

    @ConfigProperty(name = "battle.walls.square-size", defaultValue = "4")
    private int squareSize;

    @ConfigProperty(name = "battle.walls.long-width", defaultValue = "1")
    private int longWidth;

    @ConfigProperty(name = "battle.walls.long-height", defaultValue = "10")
    private int longHeight;

    @ConfigProperty(name = "battle.walls.u-width", defaultValue = "4")
    private int uWidth;

    @ConfigProperty(name = "battle.walls.u-height", defaultValue = "10")
    private int uHeight;

    private final Random random = new Random();

    /**
     * Generate random walls for the battle arena.
     */
    public List<Wall> generateWalls(Battle battle) {
        List<Wall> walls = new ArrayList<>();
        int arenaWidth = battle.getArenaWidth();
        int arenaHeight = battle.getArenaHeight();
        int totalArenaSize = arenaWidth * arenaHeight;
        int maxWallCoverage = (totalArenaSize * maxCoveragePercentage) / 100;

        // Special case: if battle name contains "Empty", don't generate walls
        if (battle.getName() != null && battle.getName().contains("Empty")) {
            return walls; // Return empty list
        }

        // Ensure at least one wall is generated for arenas large enough
        if (maxWallCoverage < 10 && arenaWidth >= 10 && arenaHeight >= 10) {
            maxWallCoverage = 10; // Minimum wall coverage for small arenas
        }

        int currentCoverage = 0;
        int attempts = 0;
        int maxAttempts = 200; // Increased attempts

        while (currentCoverage < maxWallCoverage && attempts < maxAttempts) {
            Wall.WallType wallType = getRandomWallType();
            Wall wall = generateWall(wallType, arenaWidth, arenaHeight, walls);

            if (wall != null) {
                int wallSize = wall.getPositions().size();
                if (currentCoverage + wallSize <= maxWallCoverage) {
                    walls.add(wall);
                    currentCoverage += wallSize;
                }
            }
            attempts++;
        }

        // If no walls were generated and arena is large enough, force generate at least one simple wall
        if (walls.isEmpty() && arenaWidth >= 10 && arenaHeight >= 10) {
            Wall simpleWall = generateSimpleWall(arenaWidth, arenaHeight);
            if (simpleWall != null) {
                walls.add(simpleWall);
            }
        }

        return walls;
    }

    private Wall.WallType getRandomWallType() {
        Wall.WallType[] types = Wall.WallType.values();
        return types[random.nextInt(types.length)];
    }

    private Wall generateWall(Wall.WallType type, int arenaWidth, int arenaHeight, List<Wall> existingWalls) {
        switch (type) {
            case SQUARE:
                return generateSquareWall(arenaWidth, arenaHeight, existingWalls);
            case LONG:
                return generateLongWall(arenaWidth, arenaHeight, existingWalls);
            case U_SHAPE:
                return generateUShapeWall(arenaWidth, arenaHeight, existingWalls);
            default:
                return null;
        }
    }

    private Wall generateSquareWall(int arenaWidth, int arenaHeight, List<Wall> existingWalls) {
        if (arenaWidth < squareSize || arenaHeight < squareSize) {
            return null;
        }

        int attempts = 0;
        while (attempts < 20) {
            int startX = random.nextInt(arenaWidth - squareSize);
            int startY = random.nextInt(arenaHeight - squareSize);

            Wall wall = new Wall(Wall.WallType.SQUARE);
            boolean canPlace = true;

            for (int x = startX; x < startX + squareSize && canPlace; x++) {
                for (int y = startY; y < startY + squareSize && canPlace; y++) {
                    if (isPositionOccupied(x, y, existingWalls)) {
                        canPlace = false;
                    }
                }
            }

            if (canPlace) {
                for (int x = startX; x < startX + squareSize; x++) {
                    for (int y = startY; y < startY + squareSize; y++) {
                        wall.addPosition(x, y);
                    }
                }
                return wall;
            }
            attempts++;
        }
        return null;
    }

    private Wall generateLongWall(int arenaWidth, int arenaHeight, List<Wall> existingWalls) {
        boolean horizontal = random.nextBoolean();

        if (horizontal) {
            if (arenaWidth < longHeight || arenaHeight < longWidth) {
                return null;
            }

            int attempts = 0;
            while (attempts < 20) {
                int startX = random.nextInt(arenaWidth - longHeight);
                int startY = random.nextInt(arenaHeight - longWidth);

                Wall wall = new Wall(Wall.WallType.LONG);
                boolean canPlace = true;

                for (int x = startX; x < startX + longHeight && canPlace; x++) {
                    for (int y = startY; y < startY + longWidth && canPlace; y++) {
                        if (isPositionOccupied(x, y, existingWalls)) {
                            canPlace = false;
                        }
                    }
                }

                if (canPlace) {
                    for (int x = startX; x < startX + longHeight; x++) {
                        for (int y = startY; y < startY + longWidth; y++) {
                            wall.addPosition(x, y);
                        }
                    }
                    return wall;
                }
                attempts++;
            }
        } else {
            if (arenaWidth < longWidth || arenaHeight < longHeight) {
                return null;
            }

            int attempts = 0;
            while (attempts < 20) {
                int startX = random.nextInt(arenaWidth - longWidth);
                int startY = random.nextInt(arenaHeight - longHeight);

                Wall wall = new Wall(Wall.WallType.LONG);
                boolean canPlace = true;

                for (int x = startX; x < startX + longWidth && canPlace; x++) {
                    for (int y = startY; y < startY + longHeight && canPlace; y++) {
                        if (isPositionOccupied(x, y, existingWalls)) {
                            canPlace = false;
                        }
                    }
                }

                if (canPlace) {
                    for (int x = startX; x < startX + longWidth; x++) {
                        for (int y = startY; y < startY + longHeight; y++) {
                            wall.addPosition(x, y);
                        }
                    }
                    return wall;
                }
                attempts++;
            }
        }
        return null;
    }

    private Wall generateUShapeWall(int arenaWidth, int arenaHeight, List<Wall> existingWalls) {
        if (arenaWidth < uWidth || arenaHeight < uHeight) {
            return null;
        }

        int attempts = 0;
        while (attempts < 20) {
            int startX = random.nextInt(arenaWidth - uWidth);
            int startY = random.nextInt(arenaHeight - uHeight);

            Wall wall = new Wall(Wall.WallType.U_SHAPE);
            boolean canPlace = true;

            // Check if U-shape can be placed (bottom horizontal bar and two vertical bars)
            // Bottom horizontal bar
            for (int x = startX; x < startX + uWidth && canPlace; x++) {
                if (isPositionOccupied(x, startY, existingWalls)) {
                    canPlace = false;
                }
            }

            // Left vertical bar
            for (int y = startY; y < startY + uHeight && canPlace; y++) {
                if (isPositionOccupied(startX, y, existingWalls)) {
                    canPlace = false;
                }
            }

            // Right vertical bar
            for (int y = startY; y < startY + uHeight && canPlace; y++) {
                if (isPositionOccupied(startX + uWidth - 1, y, existingWalls)) {
                    canPlace = false;
                }
            }

            if (canPlace) {
                // Add bottom horizontal bar
                for (int x = startX; x < startX + uWidth; x++) {
                    wall.addPosition(x, startY);
                }

                // Add left vertical bar
                for (int y = startY; y < startY + uHeight; y++) {
                    wall.addPosition(startX, y);
                }

                // Add right vertical bar
                for (int y = startY; y < startY + uHeight; y++) {
                    wall.addPosition(startX + uWidth - 1, y);
                }

                return wall;
            }
            attempts++;
        }
        return null;
    }

    private boolean isPositionOccupied(int x, int y, List<Wall> existingWalls) {
        return existingWalls.stream().anyMatch(wall -> wall.containsPosition(x, y));
    }

    /**
     * Generate a simple square wall when normal generation fails.
     */
    private Wall generateSimpleWall(int arenaWidth, int arenaHeight) {
        // Generate a simple 2x2 square wall at a safe position
        int size = Math.min(2, Math.min(arenaWidth / 3, arenaHeight / 3));
        if (size < 1) {
            return null;
        }

        // Place it in the center area
        int startX = arenaWidth / 2 - size / 2;
        int startY = arenaHeight / 2 - size / 2;

        Wall wall = new Wall(Wall.WallType.SQUARE);
        for (int x = startX; x < startX + size; x++) {
            for (int y = startY; y < startY + size; y++) {
                wall.addPosition(x, y);
            }
        }
        return wall;
    }
}
