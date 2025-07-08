# WebSocket Connection Fix

## Problem

The WebSocket connections in the Robot Wars frontend were failing to connect to the backend when running locally in development mode.

## Root Cause

The issue was caused by a **proxy configuration mismatch**:

- **Frontend Development Server**: Runs on `http://localhost:3000`
- **Backend Server**: Runs on `http://localhost:8080`
- **Proxy Configuration**: Only handles HTTP requests, not WebSocket connections

### What Was Happening

1. Frontend tries to connect to WebSocket at `ws://localhost:3000/battle-state/{battleId}`
2. The development server (port 3000) doesn't have WebSocket endpoints
3. The proxy configuration in `package.json` only proxies HTTP requests, not WebSocket connections
4. WebSocket connection fails

## Solution

Updated both `ArenaComponent.tsx` and `ChatComponent.tsx` to use environment-aware WebSocket URLs:

```typescript
// In development, connect directly to backend (port 8080)
// In production, use the same host as the frontend
const wsHost = process.env.NODE_ENV === 'development' 
  ? 'localhost:8080' 
  : window.location.host;

// Use secure WebSocket (wss) if the page is served over HTTPS
const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const wsUrl = `${wsProtocol}//${wsHost}/battle-state/${battleId}`;

console.log(`Connecting to WebSocket: ${wsUrl}`);
const ws = new WebSocket(wsUrl);
```

## Benefits

1. **Development Mode**: Connects directly to `ws://localhost:8080/battle-state/{battleId}`
2. **Production Mode**: Uses the same host as the frontend (supports both HTTP and HTTPS)
3. **Security**: Automatically uses `wss://` for HTTPS sites
4. **Debugging**: Logs the WebSocket URL for easier troubleshooting

## Backend WebSocket Endpoints

The backend provides these WebSocket endpoints:

- `/battle-state/{battleId}` - Real-time battle state updates
- `/chat/{username}` - Chat functionality

Both are defined in the backend Java classes:
- `BattleStateSocket.java` - `@ServerEndpoint("/battle-state/{battleId}")`
- `ChatSocket.java` - `@ServerEndpoint("/chat/{username}")`

## Testing

All BDD tests continue to pass, including the new "Render the arena for a selected battle" scenario.

## Usage

When running the application locally:

1. Start the backend: `cd backend && ../gradlew quarkusDev` (runs on port 8080)
2. Start the frontend: `cd frontend && npm start` (runs on port 3000)
3. WebSocket connections will automatically connect to the backend on port 8080

The fix is transparent to users and maintains compatibility with production deployments.
