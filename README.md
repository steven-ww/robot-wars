# Robot Wars

A multi-project application with a Quarkus backend and React frontend.

## Project Structure

This project consists of two main components:

- **Backend**: A Quarkus-based Java application that provides REST API endpoints and WebSocket support.
- **Frontend**: A React application with TypeScript that consumes the backend APIs.

## Tech Stack

### Backend
- **Language**: Java 17
- **Framework**: Quarkus
- **Build Tool**: Gradle
- **Features**:
  - REST API endpoints
  - WebSocket support
  - Native compilation
  - Kubernetes deployment configuration
  - Docker containerization

### Frontend
- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: npm
- **Features**:
  - REST API client
  - WebSocket client
  - Docker containerization

## Running Locally

### Prerequisites
- JDK 17+
- Node.js 18+
- Docker (optional, for containerized deployment)

### Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Run the application in development mode:
   ```bash
   ../gradlew quarkusDev
   ```

   The backend will be available at http://localhost:8080

### Frontend

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Run the application in development mode:
   ```bash
   npm start
   ```

   The frontend will be available at http://localhost:3000

## Testing

### Backend

Run the backend tests:
```bash
cd backend
../gradlew test
```

### Frontend

Run the frontend tests:
```bash
cd frontend
npm test
```

## Building

### Backend

Build the backend:
```bash
cd backend
../gradlew build
```

For native build:
```bash
cd backend
../gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

### Frontend

Build the frontend:
```bash
cd frontend
npm run build
```

## Docker Deployment

### Backend

Build the JVM Docker image:
```bash
cd backend
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/robot-wars-backend-jvm .
```

Build the native Docker image:
```bash
cd backend
docker build -f src/main/docker/Dockerfile.native -t quarkus/robot-wars-backend-native .
```

### Frontend

Build the frontend Docker image:
```bash
cd frontend
docker build -t robot-wars-frontend .
```

## API Overview

### REST Endpoints

- `GET /api/greeting`: Returns a plain text greeting
- `GET /api/greeting/json`: Returns a JSON greeting

### WebSocket Endpoints

- `/chat/{username}`: WebSocket endpoint for chat functionality

## CI/CD

This project includes GitHub Actions workflows for continuous integration and deployment:

- **Backend CI**: Builds, tests, and creates a Docker image for the backend
- **Frontend CI**: Builds, tests, and creates a Docker image for the frontend

## License

This project is licensed under the MIT License - see the LICENSE file for details.