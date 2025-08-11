package za.co.sww.rwars.airobot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BotLogicTest {
    @Test
    void alignmentChecks() {
        assertTrue(BotLogic.isAlignedForLaser(0, 5));
        assertTrue(BotLogic.isAlignedForLaser(5, 0));
        assertTrue(BotLogic.isAlignedForLaser(3, 3));
        assertTrue(BotLogic.isAlignedForLaser(-4, 4));
        assertFalse(BotLogic.isAlignedForLaser(2, 3));
    }

    @Test
    void directionTowardCardinalAndDiagonal() {
        assertEquals("NORTH", BotLogic.directionToward(0, 5));
        assertEquals("SOUTH", BotLogic.directionToward(0, -2));
        assertEquals("EAST", BotLogic.directionToward(7, 0));
        assertEquals("WEST", BotLogic.directionToward(-1, 0));
        assertEquals("NE", BotLogic.directionToward(5, 9));
        assertEquals("NW", BotLogic.directionToward(-2, 3));
        assertEquals("SE", BotLogic.directionToward(4, -6));
        assertEquals("SW", BotLogic.directionToward(-3, -1));
        assertNull(BotLogic.directionToward(0, 0));
    }
}
