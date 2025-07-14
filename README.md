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

## Robot Demo Application

The project includes a robot demonstration application (`robo-demo`) that showcases the Robot Wars API functionality by creating battles and moving robots around an arena.

### Running the Robot Demo

To run the robot demo, use the provided script:

```bash
cd robo-demo
./start-battle.sh [OPTIONS]
```

#### Available Options:

- `-u, --url URL`: Base URL for the Robot Wars API (default: `http://localhost:8080`)
- `-t, --time TIME`: Time limit for the battle (e.g., `5m`, `30s`) (default: `5m`)
- `-s, --stop-on-crash`: Stop the demo when the first robot crashes (default: `false`)
- `-h, --help`: Show help message

#### Examples:

```bash
# Run with default settings (5 minutes, continue after crashes)
./start-battle.sh

# Run for 2 minutes and stop on first crash
./start-battle.sh --time 2m --stop-on-crash

# Run against a different API server
./start-battle.sh --url http://remote-server:8080

# Combine multiple options
./start-battle.sh --url http://localhost:8080 --time 30s --stop-on-crash
```

### Running the Robot Demo Directly

You can also run the robot demo application directly using Gradle:

```bash
cd robo-demo
../gradlew run --args="--url http://localhost:8080 --time 5m --stop-on-crash"
```

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
- **Frontend CI**: Builds, tests, lints the frontend code, and deploys to S3 on main branch

### GitHub Variables Configuration

To enable the frontend deployment to S3, configure the following GitHub repository variables:

#### Required Secrets
- `AWS_ROLE_ARN`: The ARN of the AWS IAM role to assume for deployment

#### Required Variables
- `S3_BUCKET_ARN`: The ARN of the S3 bucket for deployment (e.g., `arn:aws:s3:::my-bucket-name`)
  - Alternatively, you can use `S3_BUCKET_NAME` with just the bucket name

#### Optional Variables
- `BACKEND_URL`: Backend URL for the production build (default: `http://localhost:8080`)
- `CLOUDFRONT_DISTRIBUTION_ID`: CloudFront distribution ID for cache invalidation (optional)

**Note**: The deployment uses `af-south-1` as the AWS region to match the backend deployment.

#### Setting Up GitHub Secrets and Variables

1. Go to your GitHub repository
2. Click on **Settings** > **Secrets and variables** > **Actions**
3. For secrets (like `AWS_ROLE_ARN`):
   - Click on the **Secrets** tab
   - Click **New repository secret**
   - Add the required secrets listed above
4. For variables (like `S3_BUCKET_ARN`):
   - Click on the **Variables** tab
   - Click **New repository variable**
   - Add the required variables listed above

#### AWS IAM Role Configuration

The `AWS_ROLE_ARN` should have the following permissions:
- `s3:PutObject`
- `s3:PutObjectAcl`
- `s3:GetObject`
- `s3:DeleteObject`
- `s3:ListBucket`
- `cloudfront:CreateInvalidation` (if using CloudFront)

The role should also have a trust policy that allows GitHub Actions to assume it using OpenID Connect.

#### Sample AWS Policies

Sample IAM policy and trust policy files are provided in the `.github/` directory:
- `.github/aws-iam-policy.json`: Sample IAM policy for S3 deployment permissions
- `.github/aws-trust-policy.json`: Sample trust policy for GitHub Actions OIDC

Replace the placeholders (YOUR-BUCKET-NAME, YOUR-ACCOUNT-ID, YOUR-GITHUB-USERNAME, YOUR-DISTRIBUTION-ID) with your actual values.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
