# AI Robot

A simple Java 21 client that connects to the Robot Wars production API and plays the game.

Features
- Dev mode: creates a test battle, registers one robot, starts the battle, and plays.
- Self-play mode: creates a normal battle, registers two robots, starts the battle, and plays both until there is a winner.

Requirements
- JDK 21

Run
- Dev mode (test battle):
  ./gradlew :ai-robot:run --args="--mode=dev --name=AgentV"
  Options:
    --devTwoRobots=true|false   (default: true) Run dev mode with two robots in the same test battle
    --maxSteps=N               (default: 200)  Maximum AI steps before stopping
    --maxSeconds=M             (default: 120)  Maximum seconds before stopping
    --statusEvery=K            (default: 5)    Log robot status every K steps

- Self-play (two robots in one battle):
  ./gradlew :ai-robot:run --args="--mode=self --name=AgentV"
  You can also pass --maxSteps/--maxSeconds/--statusEvery to cap duration and add status logs.

- Override server base URL (defaults to https://api.rwars.steven-webber.com):
  ./gradlew :ai-robot:run --args="--mode=dev --baseUrl=https://api.rwars.steven-webber.com"

Notes
- The AI is intentionally simple: it scans with radar, moves toward detections, and fires when aligned. It respects the server’s movement timing by polling for movement completion.
- Uses Java virtual threads for lightweight polling and concurrent action scheduling.
