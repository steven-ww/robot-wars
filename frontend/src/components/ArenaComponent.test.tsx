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

describe('ArenaComponent', () => {
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

  test('renders laser effect when laser event occurs', async () => {
    const { rerender } = render(<ArenaComponent battleId="battle-1" />);

    // Simulate WebSocket connection opening and receiving battle state
    if (mockWebSocket.onopen) {
      mockWebSocket.onopen();
    }

    // Send mock battle state to establish the arena
    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockBattleState),
      });
    }

    // Re-render to reflect the state changes
    rerender(<ArenaComponent battleId="battle-1" />);

    // Now the arena-grid should be visible
    screen.getByTestId('arena-grid');

    // Send laser event
    if (mockWebSocket.onmessage) {
      mockWebSocket.onmessage({
        data: JSON.stringify(mockLaserEvent),
      });
    }

    // Re-render to reflect laser event
    rerender(<ArenaComponent battleId="battle-1" />);

    // Verify laser rendering - should find laser elements (active laser + laser effects)
    // The component renders both activeLaser and laserEffects, so we expect 2 laser elements
    expect(screen.getAllByTestId(/laser-/)).toHaveLength(2);
  });
});
