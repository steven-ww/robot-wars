package za.co.sww.rwars.airobot;

import java.time.Duration;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        var cli = CliOptions.parse(args);
        var baseUrl = cli.baseUrl();
        var robotName = cli.name();

        var client = new RwApiClient(baseUrl);
        switch (cli.mode()) {
            case DEV -> Modes.runDevMode(client, robotName, cli.devTwoRobots(), cli.maxSteps(), cli.maxSeconds(), cli.statusEvery());
            case SELF -> Modes.runSelfPlayMode(client, robotName, cli.maxSteps(), cli.maxSeconds(), cli.statusEvery());
        }
    }

    enum Mode { DEV, SELF }

    record CliOptions(Mode mode, String name, String baseUrl, boolean devTwoRobots, int maxSteps, int maxSeconds, int statusEvery) {
        static CliOptions parse(String[] args) {
            String mode = "dev";
            String name = "AgentV";
            String baseUrl = System.getProperty("airobot.baseUrl", "https://api.rwars.steven-webber.com");
            boolean devTwoRobots = true;
            int maxSteps = 200;
            int maxSeconds = 120;
            int statusEvery = 5;
            for (String a : args) {
                if (a.startsWith("--mode=")) mode = a.substring("--mode=".length());
                else if (a.startsWith("--name=")) name = a.substring("--name=".length());
                else if (a.startsWith("--baseUrl=")) baseUrl = a.substring("--baseUrl=".length());
                else if (a.startsWith("--devTwoRobots=")) devTwoRobots = Boolean.parseBoolean(a.substring("--devTwoRobots=".length()));
                else if (a.startsWith("--maxSteps=")) maxSteps = Integer.parseInt(a.substring("--maxSteps=".length()));
                else if (a.startsWith("--maxSeconds=")) maxSeconds = Integer.parseInt(a.substring("--maxSeconds=".length()));
                else if (a.startsWith("--statusEvery=")) statusEvery = Integer.parseInt(a.substring("--statusEvery=".length()));
            }
            var parsedMode = switch (mode.toLowerCase()) {
                case "dev", "test" -> Mode.DEV;
                case "self", "self-play", "selfplay" -> Mode.SELF;
                default -> throw new IllegalArgumentException("Unknown mode: " + mode);
            };
            return new CliOptions(parsedMode, name, baseUrl, devTwoRobots, maxSteps, maxSeconds, statusEvery);
        }
    }
}
