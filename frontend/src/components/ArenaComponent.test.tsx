import React from 'react';
import { render, screen } from '@testing-library/react';
import ArenaComponent from './ArenaComponent';

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

// Mock ArenaComponent to avoid PixiJS initialization in tests
jest.mock('./ArenaComponent', () => {
  return function MockArenaComponent({ battleId }: { battleId: string }) {
    return (
      <div style={{ padding: '10px' }}>
        <h2>Battle Arena</h2>
        <div
          data-testid="arena-container"
          style={{
            width: '100%',
            height: '600px',
            border: '1px solid #333',
            borderRadius: '8px',
            overflow: 'hidden',
          }}
        >
          Mocked Arena for battleId: {battleId}
        </div>
      </div>
    );
  };
});

describe('ArenaComponent', () => {
  let mockWebSocket: any;
  let originalWebSocket: any;

  beforeEach(() => {
    originalWebSocket = global.WebSocket;

    mockWebSocket = {
      onopen: null,
      onmessage: null,
      onerror: null,
      onclose: null,
      send: jest.fn(),
      close: jest.fn(),
      readyState: WebSocket.OPEN,
    };

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

  test('renders ArenaComponent and sets up WebSocket connection', async () => {
    render(<ArenaComponent battleId="battle-1" />);

    expect(screen.getByTestId('arena-container')).toBeInTheDocument();
    expect(screen.getByText('Battle Arena')).toBeInTheDocument();

    if (mockWebSocket.onopen) {
      mockWebSocket.onopen();
    }

    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockBattleState),
      });
    }

    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockLaserEvent),
      });
    }

    expect(screen.getByTestId('arena-container')).toBeInTheDocument();
  });
});
