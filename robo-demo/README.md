# Robo Demo

A Kotlin-based demo application that interacts with the Robot Wars backend API to create battles, register robots, and move them around the arena.

## Project Overview

The Robo Demo project demonstrates how to use the Robot Wars backend API to:

1. Create a new battle
2. Register robots with names "Restro" and "ReqBot"
3. Start the battle
4. Move the robots around the arena until one crashes into a wall or 5 minutes has passed

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
./start-battle.sh
```

This script will:
1. Start the backend service in dev mode
2. Wait for the service to start
3. Run the Robo Demo application
4. Clean up resources when done

### Manual Execution

You can also run the application manually:

```bash
./gradlew :robo-demo:run
```

By default, the application connects to the backend at `http://localhost:8080`. You can specify a different URL as a command-line argument:

```bash
./gradlew :robo-demo:run --args="http://your-backend-url"
```

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

- `Battle`: Represents a battle in the Robot Wars game
- `Robot`: Represents a robot in the Robot Wars game

These classes are used for JSON serialization/deserialization.