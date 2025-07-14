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

interface WallPosition {
  x: number;
  y: number;
}

interface Wall {
  type: string;
  positions: WallPosition[];
}

interface RobotAction {
  robotId: string;
  robotName: string;
  action: string;
  timestamp: string;
}

interface BattleState {
  battleId: string;
  battleName: string;
  arenaWidth: number;
  arenaHeight: number;
  robotMovementTimeSeconds: number;
  battleState: string;
  robots: Robot[];
  walls: Wall[];
  winnerId?: string;
  winnerName?: string;
  robotActions?: RobotAction[];
}

interface LaserPosition {
  x: number;
  y: number;
}

interface LaserEvent {
  hit: boolean;
  hitRobotId?: string;
  hitRobotName?: string;
  damageDealt?: number;
  range: number;
  direction: string;
  laserPath: LaserPosition[];
  hitPosition?: LaserPosition;
  blockedBy?: string;
}

interface ArenaComponentProps {
  battleId: string;
}

const ArenaComponent: React.FC<ArenaComponentProps> = ({ battleId }) => {
  const [battleState, setBattleState] = useState<BattleState | null>(null);
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [connectionAttempted, setConnectionAttempted] =
    useState<boolean>(false);
  const [webSocketError, setWebSocketError] = useState<string | null>(null);
  const [lastUpdateTime, setLastUpdateTime] = useState<Date | null>(null);
  const [updateCount, setUpdateCount] = useState<number>(0);
  const [activeLaser, setActiveLaser] = useState<LaserEvent | null>(null);
  const [laserEffects, setLaserEffects] = useState<LaserEvent[]>([]);

  // Reference to the WebSocket connection
  const webSocketRef = useRef<WebSocket | null>(null);

  // Handle laser event
  const handleLaserEvent = (data: LaserEvent) => {
    console.log('Laser event:', data);

    // Set the active laser for immediate rendering
    setActiveLaser(data);

    // Add to laser effects history
    setLaserEffects(prev => [...prev, data]);

    // Clear the active laser after animation duration
    setTimeout(() => {
      setActiveLaser(null);
    }, 1000); // 1 second animation

    // Clear from effects history after a longer duration
    setTimeout(() => {
      setLaserEffects(prev => prev.filter(effect => effect !== data));
    }, 3000); // 3 seconds total display
  };

  // Function to connect to the WebSocket
  const connectWebSocket = useCallback(() => {
    setWebSocketError(null);
    setConnectionAttempted(true);

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
        setWebSocketError(null);
        console.log('Connected to battle state WebSocket');
      };

      ws.onmessage = event => {
        try {
          const data = JSON.parse(event.data);
          console.log('Received battle state:', data);

          // Check if there's an error message
          if (data.error) {
            setWebSocketError(data.error);
            return;
          }

          // Update battle state and tracking info
          // Check for laser event
          if (data.laserPath) {
            handleLaserEvent(data);
            // Don't update battle state with laser event data
          } else {
            // Only update battle state with actual battle state data
            setBattleState(data);
          }
          setLastUpdateTime(new Date());
          setUpdateCount(prev => prev + 1);
        } catch (err) {
          console.error('Error parsing WebSocket message:', err);
          setWebSocketError('Error parsing WebSocket message');
        }
      };

      ws.onerror = event => {
        console.error('WebSocket error:', event);
        setWebSocketError('Unable to connect to real-time updates');
        setIsConnected(false);
      };

      ws.onclose = () => {
        setIsConnected(false);
        console.log('Disconnected from battle state WebSocket');
      };

      // Store the WebSocket reference
      webSocketRef.current = ws;
    } catch (err) {
      setWebSocketError(
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
      // If connected, request update via WebSocket
      webSocketRef.current.send('update');
    } else {
      // If not connected, try to reconnect
      connectWebSocket();
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

    const isWinner = battleState?.winnerId === robot.id;
    const robotClasses = [
      'robot',
      `robot-${robot.status.toLowerCase()}`,
      isWinner ? 'robot-winner' : '',
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <div
        key={robot.id}
        className={robotClasses}
        style={style}
        data-testid={`robot-${robot.id}`}
        data-x={robot.positionX}
        data-y={robot.positionY}
        data-status={robot.status}
        data-winner={isWinner}
      >
        <div className="robot-name">{robot.name}</div>
        <div className="robot-status">{robot.status}</div>
        {isWinner && <div className="robot-crown">👑</div>}
      </div>
    );
  };

  // Render walls on the arena
  const renderWalls = () => {
    if (!battleState?.walls) return null;

    return battleState.walls.map((wall, wallIndex) =>
      wall.positions.map((position, posIndex) => {
        const style = {
          left: `${(position.x / (battleState?.arenaWidth || 1)) * 100}%`,
          bottom: `${(position.y / (battleState?.arenaHeight || 1)) * 100}%`,
          width: `${(1 / (battleState?.arenaWidth || 1)) * 100}%`,
          height: `${(1 / (battleState?.arenaHeight || 1)) * 100}%`,
        };

        return (
          <div
            key={`wall-${wallIndex}-${posIndex}`}
            className={`wall wall-${wall.type.toLowerCase()}`}
            style={style}
            data-testid={`wall-${wallIndex}-${posIndex}`}
            data-x={position.x}
            data-y={position.y}
            data-type={wall.type}
          />
        );
      })
    );
  };

  // Render laser effects on the arena
  const renderLaser = (laser: LaserEvent, index: number) => {
    if (!battleState || !laser.laserPath || laser.laserPath.length === 0)
      return null;

    const startPosition = laser.laserPath[0];
    const endPosition = laser.laserPath[laser.laserPath.length - 1];

    // Calculate laser beam style
    const deltaX = endPosition.x - startPosition.x;
    const deltaY = endPosition.y - startPosition.y;
    const length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    const angle = Math.atan2(deltaY, deltaX) * (180 / Math.PI);

    const laserStyle = {
      position: 'absolute' as const,
      left: `${(startPosition.x / battleState.arenaWidth) * 100}%`,
      bottom: `${(startPosition.y / battleState.arenaHeight) * 100}%`,
      width: `${(length / battleState.arenaWidth) * 100}%`,
      height: '3px',
      backgroundColor: laser.hit ? '#ff0000' : '#00ff00',
      transform: `rotate(${angle}deg)`,
      transformOrigin: '0 50%',
      zIndex: 10,
      opacity: laser === activeLaser ? 1 : 0.5,
      boxShadow: `0 0 8px ${laser.hit ? '#ff0000' : '#00ff00'}`,
    };

    const hitEffect =
      laser.hit && laser.hitPosition ? (
        <div
          className="laser-hit-effect"
          style={{
            position: 'absolute',
            left: `${(laser.hitPosition.x / battleState.arenaWidth) * 100}%`,
            bottom: `${(laser.hitPosition.y / battleState.arenaHeight) * 100}%`,
            width: '20px',
            height: '20px',
            borderRadius: '50%',
            backgroundColor: '#ff6600',
            transform: 'translate(-50%, 50%)',
            zIndex: 11,
            animation: 'pulse 0.5s ease-in-out',
          }}
          data-testid={`laser-hit-${index}`}
        />
      ) : null;

    return (
      <div key={`laser-${index}`} data-testid={`laser-${index}`}>
        <div className="laser-beam" style={laserStyle} />
        {hitEffect}
      </div>
    );
  };

  // Render the action window showing robot actions
  const renderActionWindow = () => {
    if (!battleState || battleState.battleState !== 'IN_PROGRESS') {
      return null;
    }

    const actions = battleState.robotActions || [];

    return (
      <div
        className="action-window responsive-width"
        data-testid="action-window"
      >
        <div className="action-window-header">
          <h4>Robot Actions</h4>
        </div>
        <div className="action-list scrollable" data-testid="action-list">
          {actions.map((action, index) => (
            <div
              key={`${action.robotId}-${action.timestamp}-${index}`}
              className="action-item"
              data-testid={`action-item-${index}`}
            >
              <span className="robot-name">{action.robotName}</span>
              <span className="action-type">{action.action}</span>
              <span className="action-timestamp">
                {new Date(action.timestamp).toLocaleTimeString()}
              </span>
            </div>
          ))}
          {actions.length === 0 && (
            <div className="no-actions">
              <p>No actions yet...</p>
            </div>
          )}
        </div>
      </div>
    );
  };

  // Render battle results when battle is completed
  const renderBattleResults = () => {
    if (battleState?.battleState !== 'COMPLETED') {
      return null;
    }

    return (
      <div className="battle-results">
        <div className="battle-results-header">
          <h3>🏆 Battle Complete!</h3>
        </div>
        <div className="battle-results-content">
          {battleState.winnerName ? (
            <div className="winner-announcement">
              <h4>Winner: {battleState.winnerName}</h4>
              <p>
                Congratulations! {battleState.winnerName} has won the battle!
              </p>
            </div>
          ) : (
            <div className="no-winner-announcement">
              <h4>Battle Ended</h4>
              <p>The battle has completed with no clear winner.</p>
            </div>
          )}

          <div className="battle-summary">
            <h5>Battle Summary:</h5>
            <ul>
              <li>Battle: {battleState.battleName}</li>
              <li>
                Arena Size: {battleState.arenaWidth}x{battleState.arenaHeight}
              </li>
              <li>Total Robots: {battleState.robots.length}</li>
              <li>
                Active Robots:{' '}
                {
                  battleState.robots.filter(
                    robot =>
                      robot.status === 'IDLE' || robot.status === 'MOVING'
                  ).length
                }
              </li>
              <li>
                Crashed/Destroyed Robots:{' '}
                {
                  battleState.robots.filter(
                    robot =>
                      robot.status === 'CRASHED' || robot.status === 'DESTROYED'
                  ).length
                }
              </li>
            </ul>
          </div>

          <div className="robot-final-status">
            <h5>Final Robot Status:</h5>
            <div className="robot-status-list">
              {battleState.robots.map(robot => (
                <div
                  key={robot.id}
                  className={`robot-status-item ${robot.id === battleState.winnerId ? 'winner' : ''}`}
                >
                  <span className="robot-name">{robot.name}</span>
                  <span
                    className={`robot-status-badge status-${robot.status.toLowerCase()}`}
                  >
                    {robot.status}
                  </span>
                  {robot.id === battleState.winnerId && (
                    <span className="winner-badge">👑 WINNER</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div
      className={`arena-container ${
        battleState?.battleState === 'IN_PROGRESS'
          ? 'arena-with-action-window responsive-layout'
          : ''
      }`}
      data-testid="arena-container"
    >
      <h2>Battle Arena</h2>
      <p className="description">
        This component displays the battle arena and the robots within it.
      </p>

      {/* Only show WebSocket error if connection was attempted and failed, and we don't have battle data */}
      {webSocketError && !battleState && connectionAttempted && (
        <div
          className="warning-container"
          style={{
            backgroundColor: '#fff3cd',
            border: '1px solid #ffeaa7',
            padding: '10px',
            marginBottom: '10px',
            borderRadius: '4px',
          }}
        >
          <p style={{ color: '#856404', margin: '0 0 5px 0' }}>
            <strong>Real-time updates unavailable:</strong> {webSocketError}
          </p>
          <p
            style={{
              color: '#856404',
              margin: '0 0 10px 0',
              fontSize: '0.9em',
            }}
          >
            Make sure the backend server is running on port 8080.
          </p>
          <button
            onClick={connectWebSocket}
            style={{
              padding: '5px 10px',
              backgroundColor: '#ffc107',
              border: 'none',
              borderRadius: '3px',
              cursor: 'pointer',
            }}
          >
            Retry Connection
          </button>
        </div>
      )}

      {/* Show battle results if battle is completed */}
      {renderBattleResults()}

      {/* Show connection status when we have battle data */}
      {battleState && (
        <div className="battle-info">
          <h3>{battleState.battleName}</h3>
          <p>
            Battle State:
            <span
              className={`battle-state-badge state-${battleState.battleState.toLowerCase()}`}
            >
              {battleState.battleState}
            </span>
          </p>
          <p>
            Arena Size: {battleState.arenaWidth}x{battleState.arenaHeight}
          </p>
          <p>Robots: {battleState.robots.length}</p>
          <p>
            Connection Status:{' '}
            {isConnected ? (
              <span style={{ color: 'green' }}>🟢 Live Updates</span>
            ) : (
              <span style={{ color: 'orange' }}>🟡 Cached Data</span>
            )}
          </p>
          {/* Show update tracking info */}
          {isConnected && (
            <div
              style={{ fontSize: '0.9em', color: '#666', marginTop: '10px' }}
            >
              <p>Updates received: {updateCount}</p>
              {lastUpdateTime && (
                <p>Last update: {lastUpdateTime.toLocaleTimeString()}</p>
              )}
            </div>
          )}
          {/* Only show refresh button if WebSocket is not connected */}
          {!isConnected && (
            <button
              onClick={requestUpdate}
              style={{
                padding: '5px 10px',
                backgroundColor: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '3px',
                cursor: 'pointer',
              }}
            >
              Refresh Data
            </button>
          )}
        </div>
      )}

      {/* Main layout wrapper for arena and action window */}
      {battleState && (
        <div className="arena-main-layout">
          {/* Arena section */}
          <div className="arena-section">
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
                maxWidth:
                  battleState.battleState === 'IN_PROGRESS' ? '60vw' : '75vw',
                maxHeight: 'calc(100vh - 250px)',
                margin: '0 auto',
              }}
            >
              {renderWalls()}
              {battleState.robots.map(renderRobot)}
              {/* Render active laser */}
              {activeLaser && renderLaser(activeLaser, 0)}
              {/* Render laser effects */}
              {laserEffects.map((laser, index) =>
                renderLaser(laser, index + 1)
              )}
            </div>
          </div>

          {/* Action window section */}
          {battleState.battleState === 'IN_PROGRESS' && (
            <div className="action-section">{renderActionWindow()}</div>
          )}
        </div>
      )}

      {/* Show connecting message only when no error and no battle data */}
      {!isConnected &&
        !webSocketError &&
        !battleState &&
        connectionAttempted && (
          <div className="connecting">
            <p>Connecting to battle state...</p>
          </div>
        )}

      {/* Show initial loading state */}
      {!connectionAttempted && (
        <div className="connecting">
          <p>Initializing battle arena...</p>
        </div>
      )}
    </div>
  );
};

export default ArenaComponent;
