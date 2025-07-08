import React, { useState, useEffect, useRef, useCallback } from 'react';
import './ArenaComponent.css';

interface Robot {
  id: string;
  name: string;
  battleId: string;
  positionX: number;
  positionY: number;
  direction: string;
  status: string;
  targetBlocks: number;
  blocksRemaining: number;
}

interface BattleState {
  battleId: string;
  battleName: string;
  arenaWidth: number;
  arenaHeight: number;
  robotMovementTimeSeconds: number;
  battleState: string;
  robots: Robot[];
}

interface ArenaComponentProps {
  battleId: string;
}

const ArenaComponent: React.FC<ArenaComponentProps> = ({ battleId }) => {
  const [battleState, setBattleState] = useState<BattleState | null>(null);
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Reference to the WebSocket connection
  const webSocketRef = useRef<WebSocket | null>(null);

  // Function to connect to the WebSocket
  const connectWebSocket = useCallback(() => {
    setError(null);

    try {
      // Close existing connection if any
      if (webSocketRef.current) {
        webSocketRef.current.close();
      }

      // Create a new WebSocket connection
      // In development, connect directly to backend (port 8080)
      // In production, use the same host as the frontend
      const wsHost =
        process.env.NODE_ENV === 'development'
          ? 'localhost:8080'
          : window.location.host;

      // Use secure WebSocket (wss) if the page is served over HTTPS
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const wsUrl = `${wsProtocol}//${wsHost}/battle-state/${battleId}`;

      console.log(`Connecting to WebSocket: ${wsUrl}`);
      const ws = new WebSocket(wsUrl);

      // Set up event handlers
      ws.onopen = () => {
        setIsConnected(true);
        console.log('Connected to battle state WebSocket');
      };

      ws.onmessage = event => {
        try {
          const data = JSON.parse(event.data);
          console.log('Received battle state:', data);

          // Check if there's an error message
          if (data.error) {
            setError(data.error);
            return;
          }

          // Update battle state
          setBattleState(data);
        } catch (err) {
          console.error('Error parsing WebSocket message:', err);
          setError('Error parsing WebSocket message');
        }
      };

      ws.onerror = event => {
        console.error('WebSocket error:', event);
        setError('WebSocket connection error');
        setIsConnected(false);
      };

      ws.onclose = () => {
        setIsConnected(false);
        console.log('Disconnected from battle state WebSocket');
      };

      // Store the WebSocket reference
      webSocketRef.current = ws;
    } catch (err) {
      setError(
        `Failed to connect: ${err instanceof Error ? err.message : String(err)}`
      );
      console.error('Error connecting to WebSocket:', err);
    }
  }, [battleId]);

  // Function to disconnect from the WebSocket
  const disconnectWebSocket = () => {
    if (webSocketRef.current) {
      webSocketRef.current.close();
      webSocketRef.current = null;
    }
  };

  // Function to request an update
  const requestUpdate = () => {
    if (isConnected && webSocketRef.current) {
      webSocketRef.current.send('update');
    }
  };

  // Connect to WebSocket when component mounts or battleId changes
  useEffect(() => {
    connectWebSocket();

    // Clean up WebSocket connection when component unmounts or battleId changes
    return () => {
      disconnectWebSocket();
    };
  }, [battleId, connectWebSocket]);

  // Render a robot on the arena
  const renderRobot = (robot: Robot) => {
    const style = {
      left: `${(robot.positionX / (battleState?.arenaWidth || 1)) * 100}%`,
      bottom: `${(robot.positionY / (battleState?.arenaHeight || 1)) * 100}%`,
    };

    return (
      <div
        key={robot.id}
        className={`robot robot-${robot.status.toLowerCase()}`}
        style={style}
        data-testid={`robot-${robot.id}`}
        data-x={robot.positionX}
        data-y={robot.positionY}
        data-status={robot.status}
      >
        <div className="robot-name">{robot.name}</div>
        <div className="robot-status">{robot.status}</div>
      </div>
    );
  };

  return (
    <div className="arena-container">
      <h2>Battle Arena</h2>
      <p className="description">
        This component displays the battle arena and the robots within it.
      </p>

      {error && (
        <div className="error-container">
          <p className="error">{error}</p>
          <button onClick={connectWebSocket}>Reconnect</button>
        </div>
      )}

      {isConnected && battleState && (
        <div className="battle-info">
          <h3>{battleState.battleName}</h3>
          <p>Battle State: {battleState.battleState}</p>
          <p>
            Arena Size: {battleState.arenaWidth}x{battleState.arenaHeight}
          </p>
          <p>Robots: {battleState.robots.length}</p>
          <button onClick={requestUpdate}>Refresh</button>
        </div>
      )}

      {isConnected && battleState && (
        <div
          className="arena-grid"
          data-testid="arena-grid"
          data-width={battleState.arenaWidth}
          data-height={battleState.arenaHeight}
          style={{
            aspectRatio: `${battleState.arenaWidth} / ${battleState.arenaHeight}`,
            position: 'relative',
            border: '1px solid #333',
            backgroundColor: '#f0f0f0',
            width: '100%',
            maxWidth: '800px',
            margin: '0 auto',
          }}
        >
          {battleState.robots.map(renderRobot)}
        </div>
      )}

      {!isConnected && !error && (
        <div className="connecting">
          <p>Connecting to battle state...</p>
        </div>
      )}
    </div>
  );
};

export default ArenaComponent;
