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
  - `ChatComponent.tsx`: Component for WebSocket interaction
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

## Building

Build the application for production:

```bash
npm run build
```

This creates a production-ready build in the `build` directory.


## Connecting to the Backend

The frontend is configured to connect to the backend at the same host where it's running, using:

- REST API endpoints at `/api/*`
- WebSocket endpoint at `ws://{host}/chat/{username}`

In development mode, the proxy in `package.json` forwards requests to the backend at http://localhost:8080.

## Component Documentation

### GreetingComponent

This component demonstrates communication with the backend using REST API calls:

- Fetches plain text greeting from `/api/greeting`
- Fetches JSON greeting from `/api/greeting/json`
- Displays the responses and provides refresh buttons

### ChatComponent

This component demonstrates real-time communication with the backend using WebSockets:

- Connects to the WebSocket endpoint at `/chat/{username}`
- Allows sending and receiving messages in real-time
- Displays a chat interface with connection status
