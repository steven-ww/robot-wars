package za.co.sww.rwars.airobot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import za.co.sww.rwars.airobot.model.Battle;
import za.co.sww.rwars.airobot.model.RadarResponse;

import static org.junit.jupiter.api.Assertions.*;

class DtoMappingTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void canDeserializeBattle() throws Exception {
        String json = "{\n" +
                "  \"id\": \"battle-123\",\n" +
                "  \"name\": \"Epic\",\n" +
                "  \"arenaWidth\": 60,\n" +
                "  \"arenaHeight\": 40,\n" +
                "  \"robotMovementTimeSeconds\": 1.5,\n" +
                "  \"state\": \"READY\",\n" +
                "  \"robots\": [],\n" +
                "  \"walls\": [],\n" +
                "  \"winnerId\": null,\n" +
                "  \"winnerName\": null\n" +
                "}";
        Battle b = mapper.readValue(json, Battle.class);
        assertEquals("battle-123", b.id());
        assertEquals(60, b.arenaWidth());
        assertEquals("READY", b.state());
    }

    @Test
    void canDeserializeRadarResponse() throws Exception {
        String json = "{\n" +
                "  \"range\": 5,\n" +
                "  \"detections\": [\n" +
                "    { \"x\": 2, \"y\": -3, \"type\": \"WALL\", \"details\": \"Wall segment\" },\n" +
                "    { \"x\": 1, \"y\": 1, \"type\": \"ROBOT\", \"details\": \"Enemy\" }\n" +
                "  ]\n" +
                "}";
        RadarResponse r = mapper.readValue(json, RadarResponse.class);
        assertEquals(5, r.range());
        assertEquals(2, r.detections().size());
        assertEquals("ROBOT", r.detections().get(1).type());
    }
}
