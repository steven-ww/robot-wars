# Robot Wars Frontend

This is the frontend component of the Robot Wars application, built with React and TypeScript.

## Features

- Modern React application with TypeScript
- REST API client to communicate with the backend
- WebSocket client for real-time communication
- Responsive design

## Tech Stack

- **Language**: TypeScript
- **Framework**: React
- **Build Tool**: npm
- **Key Dependencies**:
  - React for UI components
  - TypeScript for type safety
  - WebSocket API for real-time communication
  - ESLint for code quality

## Development

### Prerequisites

- Node.js 18+
- npm 8+

### Getting Started

1. Install dependencies:

```bash
npm install
```

2. Start the development server:

```bash
npm start
```

This will start the development server at http://localhost:3000.

The application is configured to proxy API requests to the backend at http://localhost:8080.

### Project Structure

- `src/components/`: React components
  - `GreetingComponent.tsx`: Component for REST API interaction
  - `ArenaComponent.tsx`: Component for battle arena visualization
  - `BattleManagement.tsx`: Component for battle management
- `src/App.tsx`: Main application component
- `src/index.tsx`: Application entry point

## Testing

Run the tests:

```bash
npm test
```

Run tests in CI mode (non-interactive):

```bash
npm test -- --watchAll=false
```

## Linting

Run the linter:

```bash
npm run lint
```

## Code Formatting

This project uses [Prettier](https://prettier.io/) for consistent code formatting.

### Format code automatically:

```bash
npm run format
```

### Check if code is properly formatted:

```bash
npm run format:check
```

The CI pipeline will automatically check that all code is properly formatted. You can configure your editor to format on save, or run the format command before committing.

## Building

Build the application for production:

```bash
npm run build
```

This creates a production-ready build in the `build` directory.


## Configuration

### Build-time Configuration

The frontend uses build-time configuration to set the backend URL. This approach works well for static deployments (like S3) where environment variables are not available at runtime.

#### Local Development

For local development, you can set the backend URL via environment variables:

```bash
# Development with custom backend URL
REACT_APP_BACKEND_URL=http://localhost:9080 npm start

# Or create a .env.local file
echo "REACT_APP_BACKEND_URL=http://localhost:8080" > .env.local
```

#### Production Deployment

For production deployments, the backend URL is configured via GitHub repository variables:

1. In your GitHub repository, go to Settings → Secrets and variables → Actions
2. Under "Repository variables", add:
   - **Name**: `BACKEND_URL`
   - **Value**: `https://your-backend-domain.com` (or your backend URL)

#### How it works

1. During the build process, `scripts/generate-config.js` reads the `REACT_APP_BACKEND_URL` environment variable
2. It generates a `public/config.js` file with the configuration
3. The `config.js` file is loaded before the React app starts
4. The frontend reads the configuration from `window.AppConfig`

#### Configuration Files

- `scripts/generate-config.js`: Generates the configuration file during build
- `public/config.js`: Auto-generated configuration file (not committed to git)
- `.env.example`: Example environment variables

## Connecting to the Backend

The frontend is configured to connect to the backend using:

- REST API endpoints at `/api/*`
- WebSocket endpoint at `ws://{host}/battle-state/{battleId}` (automatically converted from HTTP to WebSocket protocol)
- Swagger UI documentation at `/swagger-ui`

Both REST API and WebSocket connections use the same backend URL configuration:
- **Development**: Uses `http://localhost:8080` by default (proxy in `package.json` forwards REST requests)
- **Production**: Uses the configured `BACKEND_URL` from GitHub repository variables
- **WebSocket Protocol**: Automatically converts `http://` to `ws://` and `https://` to `wss://`

## Component Documentation

### GreetingComponent

This component demonstrates communication with the backend using REST API calls:

- Fetches plain text greeting from `/api/greeting`
- Fetches JSON greeting from `/api/greeting/json`
- Displays the responses and provides refresh buttons

### ArenaComponent

This component provides real-time battle arena visualization:

- Connects to the WebSocket endpoint at `/battle-state/{battleId}`
- Displays the battle arena with robots, walls, and laser fire
- Updates automatically as the battle progresses
- Shows battle results when completed
