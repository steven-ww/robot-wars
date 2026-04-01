# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Overview

Robot Wars is a multi-project application that simulates a virtual robot battle arena. The system allows robots to register, move around, use radar, and fire lasers at each other in a 2D grid arena. The project consists of:

1. **Backend**: Quarkus-based Java service providing core game logic and API
2. **Frontend**: React/TypeScript application for battle visualization and management
3. **Robo Demo**: Kotlin-based demo client showcasing API usage
4. **AI Robot**: Java client that demonstrates AI-driven robot behavior

## Development Commands

### Starting the Development Environment

The easiest way to start the entire development environment is to use the JBang script:

```bash
# If jbang is already installed:
./start_dev.java

# If jbang is not installed:
./jbang start_dev.java
```

This script:
- Starts the backend in Quarkus dev mode
- Starts the frontend development server
- Opens a browser to http://localhost:3000
- Displays logs from both servers

### Running Components Separately

#### Backend

```bash
cd backend
../gradlew quarkusDev
```

The backend will be available at http://localhost:8080

#### Frontend

```bash
cd frontend
npm install
npm start
```

The frontend will be available at http://localhost:3000

#### Demo Robot

```bash
cd robo-demo
./start-battle.sh [OPTIONS]
```

Options:
- `-u, --url URL`: Base URL for the API (default: `http://localhost:8080`)
- `-t, --time TIME`: Time limit for the battle (e.g., `5m`, `30s`) (default: `5m`)
- `-s, --stop-on-crash`: Stop the demo when a robot crashes (default: `false`)

#### AI Robot

```bash
# Dev mode (test battle):
cd ai-robot
../gradlew :ai-robot:run --args="--mode=dev --name=AgentV"

# Self-play (two robots in one battle):
../gradlew :ai-robot:run --args="--mode=self --name=AgentV"
```

### Testing

#### Backend Tests

```bash
cd backend
../gradlew test
```

This runs both unit tests and Cucumber BDD tests.

#### Frontend Tests

```bash
cd frontend
npm test
```

For non-interactive CI mode:

```bash
npm test -- --watchAll=false
```

#### Demo Robot Tests

```bash
cd robo-demo
../gradlew :robo-demo:test
```

### Building

#### Backend

```bash
cd backend
../gradlew build

# For native build:
../gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

#### Frontend

```bash
cd frontend
npm run build
```

### Linting and Formatting

#### Frontend

```bash
# Run linter:
cd frontend
npm run lint

# Format code:
npm run format

# Check formatting:
npm run format:check
```

## Architecture Overview

### System Architecture

The Robot Wars system uses a client-server architecture:
- The Quarkus backend provides the REST API and WebSocket endpoints
- The React frontend visualizes battles and communicates with the backend
- Robot clients (like robo-demo and ai-robot) connect to the backend API

### Coordinate System and Movement

The arena uses a 2D grid with integer coordinates:
- NORTH (or N): increases Y by 1 per block
- SOUTH (or S): decreases Y by 1 per block
- EAST (or E): increases X by 1 per block
- WEST (or W): decreases X by 1 per block
- Diagonal movements are also supported (NE, NW, SE, SW)
- Robots crash if they move out of bounds or into a wall
- Movement is asynchronous: robots traverse one block per configured movement time

### Backend Architecture

The backend follows a layered architecture:
- REST controllers handle HTTP requests
- Service layer contains business logic
- Model classes represent domain entities
- WebSocket endpoints provide real-time updates

Key components:
- Battle management system for creating and running battles
- Robot registration and control system
- Real-time battle state updates via WebSocket
- Arena visualization with walls and obstacles

### Frontend Architecture

The frontend uses a component-based React architecture:
- Battle management components for listing, creating, and starting battles
- Arena visualization using PixiJS v7
- WebSocket client for real-time updates
- REST API client for battle and robot management

### Communication

- **REST API**: Used for battle creation, robot registration, and discrete actions
- **WebSocket**: Used for real-time battle state updates, robot movements, and laser firing events

## Key API Endpoints

### Battle Management

- `POST /api/battles`: Create a new battle
- `GET /api/battles`: List all battles
- `GET /api/battles/{battleId}`: Get battle details
- `POST /api/battles/{battleId}/start`: Start a battle
- `DELETE /api/battles/{battleId}`: Delete a battle

### Robot Control

- `POST /api/battles/{battleId}/robots`: Register a robot
- `GET /api/battles/{battleId}/robots/{robotId}`: Get robot details
- `POST /api/battles/{battleId}/robots/{robotId}/move`: Move a robot
- `POST /api/battles/{battleId}/robots/{robotId}/radar`: Use radar
- `POST /api/battles/{battleId}/robots/{robotId}/laser`: Fire laser

### WebSocket

- `/battle-state/{battleId}`: Real-time updates about battle state, robot movements, and actions

## Developer Test Mode

The system supports a Test Mode to help robot developers iterate quickly:
- A battle becomes READY when a single robot is registered (versus requiring two robots in normal mode)
- Can be started and controlled like a normal battle
- The battle ends when the robot is destroyed or crashes into a wall

To use Test Mode from the UI (Battle Management tab):
1. Create a Test Battle
2. Register one robot using a client
3. Start the Battle when it shows state READY
4. View the Arena to see the robot's actions

## Robot Client Development

When developing a robot client:

1. Create a battle or test battle using the API or UI
2. Register a robot with a unique name
3. Wait for battle state to become READY
4. Start the battle when ready
5. Implement robot control logic:
   - Move the robot with directional commands
   - Use radar to scan the environment
   - Fire lasers when targeting other robots
6. Handle battle state updates and react accordingly

The `robo-demo` and `ai-robot` projects provide examples of robot client implementation in Kotlin and Java respectively.

## Special Notes

1. **Java 21 Virtual Threads**: The backend uses Java 21 virtual threads for improved scalability
2. **WebSocket Real-time Updates**: Robot movements and actions stream in real-time via WebSocket
3. **PixiJS Visualization**: The frontend uses PixiJS v7 for graphical representation of battles
4. **Native Compilation**: The backend supports native compilation for improved startup time
5. **Automated Deployments**: CI/CD pipelines for both backend and frontend deployment