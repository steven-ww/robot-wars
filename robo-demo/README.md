# Robo Demo

A Kotlin-based demo application that interacts with the Robot Wars backend API to create battles, register robots, and move them around the arena until one robot wins.

## Project Overview

The Robo Demo project demonstrates how to use the Robot Wars backend API to:

1. Create a new battle with a 20x20 arena
2. Register robots with names "Restro" and "ReqBot"
3. Start the battle
4. Move the robots around the arena concurrently until one robot wins or the time limit is reached

The project uses:
- Kotlin as the programming language
- OkHttp and Jackson for API interactions
- Coroutines for asynchronous operations
- Cucumber and JUnit for BDD testing
- WireMock for mocking the backend API during tests
- ktlint for code style enforcement

## Setup

### Prerequisites

- JDK 21 or higher
- Gradle 8.5 or higher (or use the included Gradle wrapper)

### Building the Project

To build the project, run:

```bash
./gradlew :robo-demo:build
```

## Usage

### Running the Demo

The project includes a script to start the backend service locally and run the demo:

```bash
cd robo-demo
./start-battle.sh [OPTIONS]
```

#### Available Options:

- `-u, --url URL`: Base URL for the Robot Wars API (default: `http://localhost:8080`)
- `-t, --time TIME`: Time limit for the battle (e.g., `5m`, `30s`) (default: `5m`)
- `-h, --help`: Show help message

#### Script Behavior:

The script intelligently handles server startup based on the provided URL:

1. **Server Validation**: First checks if the server is accessible at the specified URL
2. **Local Server Management**: Only starts a local server if:
   - The URL points to localhost (localhost, 127.0.0.1, or ::1)
   - The server is not already running
   - The port is not in use by another service
3. **Remote Server Support**: When using a remote URL, validates connectivity without starting local services
4. **Health Check**: Uses the Quarkus health endpoint (`/q/health`) to verify server status
5. **Timeout Protection**: Waits up to 30 seconds for local server startup

#### Examples:

```bash
# Run with default settings (5 minutes time limit)
# - Will start local server if not running
./start-battle.sh

# Run for 2 minutes
# - Will start local server if not running
./start-battle.sh --time 2m

# Run against a remote server
# - Will NOT start local server, only validates connectivity
./start-battle.sh --url http://remote-server:8080

# Run against localhost on different port
# - Will start local server on port 8080 if not running
./start-battle.sh --url http://localhost:9000

# Combine multiple options
./start-battle.sh --url http://localhost:8080 --time 30s
```

#### Error Handling:

The script will exit with an error if:
- The server is not accessible at the specified URL
- A local server fails to start within 30 seconds
- A port is in use by a different service
- Network connectivity issues prevent server access

The demo will run until:
- One robot wins (when the other robot crashes and only one remains active)
- The time limit is reached

### Manual Execution

You can also run the application manually:

```bash
./gradlew :robo-demo:run
```

Or with custom arguments:

```bash
./gradlew :robo-demo:run --args="--url http://your-backend-url --time 2m"
```

## How the Demo Works

1. **Battle Creation**: Creates a new battle with a unique name and 20x20 arena
2. **Robot Registration**: Registers two robots ("Restro" and "ReqBot") 
3. **Battle Start**: Starts the battle, which changes the state to "IN_PROGRESS"
4. **Concurrent Movement**: Both robots move independently in random directions
5. **Battle Monitoring**: The application monitors the battle state until:
   - The battle state becomes "COMPLETED" (indicating a winner)
   - The time limit is reached
6. **Winner Declaration**: When one robot crashes, the backend automatically declares the remaining robot as the winner

## Testing

The project uses Cucumber for BDD testing. The tests use WireMock to mock the backend API, so you don't need the actual backend service running to run the tests.

### Running Tests

To run the tests:

```bash
./gradlew :robo-demo:test
```

### Test Structure

- Feature files are located in `src/test/resources/features/`
- Step definitions are in `src/test/kotlin/za/co/sww/rwars/robodemo/steps/`
- WireMock configuration is in `src/test/kotlin/za/co/sww/rwars/robodemo/wiremock/`

## API Clients

The project includes two API clients:

- `BattleApiClient`: For creating battles
- `RobotApiClient`: For registering robots, starting battles, moving robots, and getting battle/robot status

These clients handle the HTTP requests and JSON serialization/deserialization.

## Model Classes

The project includes two model classes:

- `Battle`: Represents a battle in the Robot Wars game (includes winner information)
- `Robot`: Represents a robot in the Robot Wars game

These classes are used for JSON serialization/deserialization.