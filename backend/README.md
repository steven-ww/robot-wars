# Robot Wars Backend

This is the backend component of the Robot Wars application, built with Quarkus.

## Features

- REST API endpoints with virtual threads for improved scalability
- WebSocket support for real-time communication
- Native compilation support
- Kubernetes deployment configuration
- Docker containerization
- Virtual threads for high-throughput, low-latency applications
- Dev services for automated testing with containerized dependencies

## Tech Stack

- **Language**: Java 25
- **Framework**: Quarkus
- **Build Tool**: Gradle
- **Extensions**:
  - RESTEasy Reactive for REST endpoints with virtual threads
  - WebSockets for real-time communication
  - Virtual Threads for improved scalability
  - Kubernetes for deployment
  - Container Image Docker for containerization
  - SmallRye Health for health checks
  - Micrometer for metrics
  - Dev Services for testing
  - Cucumber for BDD testing

## Development

### Prerequisites

- JDK 25
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

The application uses Quarkus Dev Services for testing, which automatically provides containerized services (databases, message brokers, etc.) during test execution without manual setup. This ensures tests run in an environment similar to production with real dependencies.

#### BDD Testing with Cucumber

The application uses Cucumber for Behavior-Driven Development (BDD) testing. The BDD tests are written in Gherkin syntax and executed using Cucumber with RestAssured for API testing.

**Folder Structure:**
- `src/test/resources/features/`: Contains Gherkin feature files
- `src/test/java/za/co/sww/rwars/steps/`: Contains Java step definitions
- `src/test/resources/cucumber.properties`: Cucumber configuration

**Running BDD Tests:**
```bash
../gradlew test
```

This will run both unit tests and Cucumber BDD tests.

**Writing BDD Tests:**
1. Create a feature file in `src/test/resources/features/` using Gherkin syntax
2. Implement step definitions in `src/test/java/za/co/sww/rwars/steps/`
3. The test runner class `za.co.sww.rwars.CucumberTest` will automatically discover and run the tests

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

## Deployment

### AWS ECR + EC2 Deployment (via SSM)

The backend workflow (`.github/workflows/backend-ci.yml`) deploys on pushes to `main` (for backend/workflow path changes). Deployment is now done via **AWS Systems Manager (SSM)** instead of SSH.

The deployment uses:

- **OIDC Authentication**: GitHub Actions assumes an AWS IAM role via OpenID Connect (OIDC), without long-lived static credentials
- **AWS ECR**: Container images are pushed to Elastic Container Registry
- **AWS SSM Run Command**: The EC2 deploy step sends shell commands to the instance via SSM (no SSH key or SSH host required)
- **Route 53 update**: The API DNS A record is upserted to the EC2 instance public IP
- **Regional deployment**: AWS region `af-south-1`

#### Required GitHub Secrets

- `AWS_ROLE_ARN`: IAM role ARN assumed by GitHub Actions

#### Required GitHub Repository Variables

- `EC2_INSTANCE_ID`: Target EC2 instance for SSM deployment
- `ROUTE53_HOSTED_ZONE_ID`: Hosted zone containing `api.rwars.steven-webber.com`

#### Optional GitHub Repository Variables

- `CONTAINER_NAME`: Docker container name on EC2 (defaults to `robot-wars-backend`)
- `CONTAINER_PORT`: Host port for the backend container (defaults to `8080`)

#### ECR Repository

Docker images are pushed to ECR repository `robot-wars-backend`.

Images are tagged with:
- `latest`: most recent build on `main`
- `<commit-sha>`: exact build provenance

#### CI/CD Pipeline

The automated deployment process:
1. Builds and tests the application
2. Runs code quality checks (Checkstyle)
3. Builds the native binary and native Docker image
4. Authenticates to AWS using OIDC
5. Pushes `latest` and `<commit-sha>` tags to ECR
6. Uses SSM Run Command on the configured EC2 instance to:
   - log in to ECR
   - pull latest backend image
   - stop/remove existing container
   - start the updated container with restart policy
   - prune old images
7. Updates the Route 53 A record for `api.rwars.steven-webber.com`

## API Documentation

### REST Endpoints

- `GET /api/greeting`: Returns a plain text greeting
- `GET /api/greeting/json`: Returns a JSON greeting

### WebSocket Endpoints

- `/battle-state/{battleId}`: WebSocket endpoint for real-time battle state updates

## Configuration

The application configuration is in `src/main/resources/application.properties`. Key configurations include:

- HTTP port: 8080
- CORS settings
- WebSocket configuration
- Virtual threads configuration for improved scalability
- Dev services configuration for testing
- Native build settings with Java 25 support
- Container image settings
- Kubernetes deployment configuration

## Kubernetes Deployment

The application includes Kubernetes deployment configuration. To generate the Kubernetes resources:

```bash
../gradlew build -Dquarkus.kubernetes.deploy=true
```

This will build the application and deploy it to your configured Kubernetes cluster.

## Production HTTPS on EC2 (nginx + Let's Encrypt)

In production, the backend API is exposed via nginx running on an EC2 instance. nginx terminates HTTPS for
`api.rwars.steven-webber.com` and proxies requests to the Quarkus application running on `localhost:8080`
(typically inside a Docker container).

The relevant nginx configuration looks like this:

```nginx path=null start=null
server {
    listen 80;
    server_name api.rwars.steven-webber.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 443 ssl;
    server_name api.rwars.steven-webber.com;

    ssl_certificate     /etc/letsencrypt/live/api.rwars.steven-webber.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.rwars.steven-webber.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Checking certificate details and expiry

To inspect the currently configured TLS certificate on the EC2 instance:

```bash path=null start=null
sudo openssl x509 \
  -in /etc/letsencrypt/live/api.rwars.steven-webber.com/fullchain.pem \
  -noout -text | grep -E "Issuer:|Subject:|Not Before:|Not After :"
```

This shows the issuer, subject (CN) and validity period (`Not Before` / `Not After`).

### Renewing the Let's Encrypt certificate

The EC2 host uses Certbot with the nginx plugin to obtain and renew the certificate stored under
`/etc/letsencrypt/live/api.rwars.steven-webber.com/`.

To renew certificates manually:

```bash path=null start=null
sudo certbot renew
```

After a successful renewal, always validate the nginx configuration and reload it:

```bash path=null start=null
sudo nginx -t
sudo systemctl reload nginx
```

If you ever need to reissue the certificate explicitly (for example when first setting up the host), you can run:

```bash path=null start=null
sudo certbot --nginx -d api.rwars.steven-webber.com
```

Certbot will talk to Let's Encrypt, solve the HTTP-01 challenge via nginx, and write the certificate and key to the
paths referenced in the nginx configuration above.

### Verifying HTTPS and CORS from a client

From a client machine you can verify HTTPS and basic CORS behaviour with:

```bash path=null start=null
curl -i https://api.rwars.steven-webber.com/api/battles

curl -i https://api.rwars.steven-webber.com/api/battles \
  -H "Origin: https://rwars.steven-webber.com"
```

A valid certificate and working reverse proxy should result in a successful HTTPS handshake and a `2xx` HTTP status.
When called with an `Origin` header, the response should include `access-control-allow-origin` and related CORS
headers as configured by Quarkus.

Note: an expired TLS certificate on `api.rwars.steven-webber.com` will often surface in the browser as a generic
"CORS request did not succeed" or network error from the frontend, because the HTTPS connection fails before
CORS processing can occur.

### Automatic certificate renewal

The EC2 host runs `certbot renew` automatically via a systemd timer, so certificates are renewed before expiry
without manual intervention. Certbot is configured with a deploy hook in
`/etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh`:

```bash path=null start=null
#!/bin/sh
/usr/bin/nginx -t && /usr/bin/systemctl reload nginx
```

This hook runs only when a certificate is actually renewed; it validates the nginx configuration and reloads
nginx so the updated certificate is picked up immediately.
