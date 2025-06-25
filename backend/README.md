# Robot Wars Backend

This is the backend component of the Robot Wars application, built with Quarkus.

## Features

- REST API endpoints
- WebSocket support for real-time communication
- Native compilation support
- Kubernetes deployment configuration
- Docker containerization

## Tech Stack

- **Language**: Java 17
- **Framework**: Quarkus
- **Build Tool**: Gradle
- **Extensions**:
  - RESTEasy Reactive for REST endpoints
  - WebSockets for real-time communication
  - Kubernetes for deployment
  - Container Image Docker for containerization
  - SmallRye Health for health checks
  - Micrometer for metrics

## Development

### Prerequisites

- JDK 17+
- Docker (optional, for containerized builds and deployment)

### Running the Application

Run the application in development mode:

```bash
../gradlew quarkusDev
```

This command starts the application in development mode with hot reload enabled.

The application will be available at http://localhost:8080

### Testing

Run the tests:

```bash
../gradlew test
```

## Building

### JVM Build

Build the application:

```bash
../gradlew build
```

This creates a runnable JAR in the `build` directory.

### Native Build

Build a native executable:

```bash
../gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

This creates a native executable in the `build` directory.

## Docker

### JVM Mode

Build a Docker image for JVM mode:

```bash
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/robot-wars-backend-jvm .
```

Run the container:

```bash
docker run -i --rm -p 8080:8080 quarkus/robot-wars-backend-jvm
```

### Native Mode

Build a Docker image for native mode:

```bash
docker build -f src/main/docker/Dockerfile.native -t quarkus/robot-wars-backend-native .
```

Run the container:

```bash
docker run -i --rm -p 8080:8080 quarkus/robot-wars-backend-native
```

## API Documentation

### REST Endpoints

- `GET /api/greeting`: Returns a plain text greeting
- `GET /api/greeting/json`: Returns a JSON greeting

### WebSocket Endpoints

- `/chat/{username}`: WebSocket endpoint for chat functionality

## Configuration

The application configuration is in `src/main/resources/application.properties`. Key configurations include:

- HTTP port: 8080
- CORS settings
- WebSocket configuration
- Native build settings
- Container image settings
- Kubernetes deployment configuration

## Kubernetes Deployment

The application includes Kubernetes deployment configuration. To generate the Kubernetes resources:

```bash
../gradlew build -Dquarkus.kubernetes.deploy=true
```

This will build the application and deploy it to your configured Kubernetes cluster.