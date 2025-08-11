package za.co.sww.rwars.airobot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CliOptionsTest {
    @Test
    void parsesDefaults() {
        var opts = Main.CliOptions.parse(new String[]{});
        assertEquals(Main.Mode.DEV, opts.mode());
        assertEquals("AgentV", opts.name());
        assertEquals("https://api.rwars.steven-webber.com", opts.baseUrl());
    }

    @Test
    void parsesProvidedValues() {
        var opts = Main.CliOptions.parse(new String[]{"--mode=self", "--name=Zeta", "--baseUrl=https://x"});
        assertEquals(Main.Mode.SELF, opts.mode());
        assertEquals("Zeta", opts.name());
        assertEquals("https://x", opts.baseUrl());
    }

    @Test
    void rejectsUnknownMode() {
        assertThrows(IllegalArgumentException.class, () -> Main.CliOptions.parse(new String[]{"--mode=zzz"}));
    }
}
