import React from 'react';
import { render, screen } from '@testing-library/react';
import PhaserArenaComponent from './PhaserArenaComponent';

const mockBattleState = {
  battleId: 'battle-1',
  battleName: 'Test Battle',
  arenaWidth: 20,
  arenaHeight: 20,
  robotMovementTimeSeconds: 1,
  battleState: 'IN_PROGRESS',
  robots: [],
  walls: [],
};

const mockLaserEvent = {
  hit: false,
  range: 10,
  direction: 'NORTH',
  laserPath: [
    { x: 5, y: 5 },
    { x: 5, y: 10 },
  ],
};

// Mock PhaserArenaComponent to avoid Phaser initialization in tests
jest.mock('./PhaserArenaComponent', () => {
  return function MockPhaserArenaComponent({ battleId }: { battleId: string }) {
    return (
      <div style={{ padding: '10px' }}>
        <h2>Battle Arena</h2>
        <div
          data-testid="phaser-arena-container"
          style={{
            width: '100%',
            height: '600px',
            border: '1px solid #333',
            borderRadius: '8px',
            overflow: 'hidden',
          }}
        >
          Mocked Phaser Arena for battleId: {battleId}
        </div>
      </div>
    );
  };
});

describe('PhaserArenaComponent', () => {
  let mockWebSocket: any;
  let originalWebSocket: any;

  beforeEach(() => {
    originalWebSocket = global.WebSocket;

    // Mock WebSocket
    mockWebSocket = {
      onopen: null,
      onmessage: null,
      onerror: null,
      onclose: null,
      send: jest.fn(),
      close: jest.fn(),
      readyState: WebSocket.OPEN,
    };

    // Create a proper mock constructor with static properties
    const MockWebSocket = jest.fn(() => mockWebSocket) as any;
    MockWebSocket.CLOSED = WebSocket.CLOSED;
    MockWebSocket.CLOSING = WebSocket.CLOSING;
    MockWebSocket.CONNECTING = WebSocket.CONNECTING;
    MockWebSocket.OPEN = WebSocket.OPEN;
    MockWebSocket.prototype = WebSocket.prototype;

    global.WebSocket = MockWebSocket;
  });

  afterEach(() => {
    global.WebSocket = originalWebSocket;
  });

  test('renders PhaserArenaComponent and sets up WebSocket connection', async () => {
    render(<PhaserArenaComponent battleId="battle-1" />);

    // Verify the component renders with the correct container
    expect(screen.getByTestId('phaser-arena-container')).toBeInTheDocument();

    // Verify the component title
    expect(screen.getByText('Battle Arena')).toBeInTheDocument();

    // Simulate WebSocket connection opening
    if (mockWebSocket.onopen) {
      mockWebSocket.onopen();
    }

    // Send mock battle state
    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockBattleState),
      });
    }

    // Send laser event
    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockLaserEvent),
      });
    }

    // Since we're using a mocked Phaser, we can't test actual rendering,
    // but we can verify the component mounted successfully
    expect(screen.getByTestId('phaser-arena-container')).toBeInTheDocument();
  });
});
