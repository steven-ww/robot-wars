# Robot Wars

A multi-project application with a Quarkus backend and React frontend.

## Project Structure

This project consists of two main components:

- **Backend**: A Quarkus-based Java application that provides REST API endpoints and WebSocket support.
- **Frontend**: A React application with TypeScript that consumes the backend APIs.

## Tech Stack

### Backend
- **Language**: Java 21
- **Framework**: Quarkus
- **Build Tool**: Gradle
- **Features**:
  - REST API endpoints with virtual threads
  - WebSocket support
  - Native compilation
  - Kubernetes deployment configuration
  - Docker containerization
  - Dev services for testing

### Frontend
- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: npm
- **Features**:
  - REST API client
  - WebSocket client

## Running Locally

### Prerequisites

#### For Running the Application
- No prerequisites required! The JBang wrapper will automatically install:
  - JBang itself (if not already installed)
  - Java 21
  - Node.js 18+

#### For Development (Making Changes to the Project)
- JDK 21
- Node.js 18+
- Docker (optional, for containerized deployment)

### Using the Development Script

The project includes a JBang script that starts both the backend and frontend in development mode and opens a browser automatically:

1. Run the development script from the project root:
   ```bash
   # If jbang is already installed:
   ./start_dev.java

   # If jbang is not installed:
   ./jbang start_dev.java
   ```

   The first time you run this, JBang will install itself and download all required dependencies (Java 21 and Node.js) if they're not already on your system.

   This will:
   - Start the backend in Quarkus dev mode
   - Start the frontend development server
   - Open a browser to http://localhost:3000
   - Display logs from both servers in the console

   Press Ctrl+C to stop both servers.

### Running Components Separately

If you prefer to run the components separately:

#### Backend

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Run the application in development mode:
   ```bash
   ../gradlew quarkusDev
   ```

   The backend will be available at http://localhost:8080

#### Frontend

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

The backend uses Quarkus Dev Services for testing, which automatically provides containerized services (databases, message brokers, etc.) during test execution without manual setup.

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


## API Overview

### REST Endpoints

- `GET /api/greeting`: Returns a plain text greeting
- `GET /api/greeting/json`: Returns a JSON greeting

### WebSocket Endpoints

- `/chat/{username}`: WebSocket endpoint for chat functionality

## CI/CD

This project includes GitHub Actions workflows for continuous integration and deployment:

- **Backend CI**: Builds, tests, and creates a Docker image for the backend
- **Frontend CI**: Builds, tests, and lints the frontend code

## License

This project is licensed under the MIT License - see the LICENSE file for details.
