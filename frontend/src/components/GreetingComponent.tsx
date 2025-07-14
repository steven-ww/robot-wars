import React from 'react';
import './GreetingComponent.css';

// Declare global configuration type
declare global {
  interface Window {
    AppConfig?: {
      backendUrl: string;
      environment: string;
    };
  }
}

// Function to get the backend URL
const getBackendUrl = (): string => {
  // Check if there's a global configuration (set during build)
  if (window.AppConfig?.backendUrl) {
    return window.AppConfig.backendUrl;
  }

  // Fallback to development default
  return 'http://localhost:8080';
};

const GreetingComponent: React.FC = () => {
  const openSwaggerUI = () => {
    const backendUrl = getBackendUrl();
    const swaggerUrl = `${backendUrl}/swagger-ui`;
    window.open(swaggerUrl, '_blank');
  };

  return (
    <div className="greeting-container">
      <h2>Robot Wars API Documentation</h2>

      <div className="game-description">
        <h3>About Robot Wars</h3>
        <p className="description">
          Robot Wars is a multiplayer battle arena game where robots compete
          against each other in a grid-based arena. Players control robots that
          can move, scan their surroundings, and engage in combat until only one
          robot remains standing.
        </p>
      </div>

      <div className="game-mechanics">
        <h3>How It Works</h3>
        <div className="mechanics-list">
          <div className="mechanic-item">
            <h4>Arena</h4>
            <p>
              Grid-based battlefield with configurable dimensions (default
              50x50). Contains walls and obstacles that block movement and laser
              fire.
            </p>
          </div>
          <div className="mechanic-item">
            <h4>Movement</h4>
            <p>
              Robots can move in 8 directions (N, S, E, W, NE, NW, SE, SW) for a
              specified number of blocks.
            </p>
          </div>
          <div className="mechanic-item">
            <h4>Combat</h4>
            <p>
              Robots can fire lasers to damage other robots. Each hit deals 20
              damage points.
            </p>
          </div>
          <div className="mechanic-item">
            <h4>Radar</h4>
            <p>
              Scan the battlefield to detect walls and other robots within a
              configurable range.
            </p>
          </div>
          <div className="mechanic-item">
            <h4>Health</h4>
            <p>
              Robots start with 100 hit points and are destroyed when reaching 0
              HP.
            </p>
          </div>
        </div>
      </div>

      <div className="getting-started">
        <h3>Getting Started</h3>
        <div className="steps">
          <div className="step">
            <span className="step-number">1</span>
            <div className="step-content">
              <h4>Create a Battle</h4>
              <p>
                Use the <code>/api/battles</code> endpoint to create a new
                battle arena with optional dimensions.
              </p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">2</span>
            <div className="step-content">
              <h4>Register Robots</h4>
              <p>
                Register your robot using <code>/api/robots/register</code> to
                join a battle.
              </p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">3</span>
            <div className="step-content">
              <h4>Start the Battle</h4>
              <p>
                Once enough robots are registered, start the battle with{' '}
                <code>/api/battles/{'{battleId}'}/start</code>.
              </p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">4</span>
            <div className="step-content">
              <h4>Control Your Robot</h4>
              <p>
                Use the robot control endpoints to move, scan with radar, and
                fire lasers.
              </p>
            </div>
          </div>
          <div className="step">
            <span className="step-number">5</span>
            <div className="step-content">
              <h4>Win the Battle</h4>
              <p>Be the last robot standing to win the battle!</p>
            </div>
          </div>
        </div>
      </div>

      <div className="api-documentation">
        <h3>Interactive API Documentation</h3>
        <p className="description">
          Explore the full Robot Wars API with interactive documentation powered
          by Swagger UI. Try out the endpoints, see request/response examples,
          and understand the complete API specification.
        </p>
        <button onClick={openSwaggerUI} className="swagger-button">
          Open Swagger UI Documentation
        </button>
      </div>
    </div>
  );
};

export default GreetingComponent;
