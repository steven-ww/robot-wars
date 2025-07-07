import { defineFeature, loadFeature } from 'jest-cucumber';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import React from 'react';
import ArenaComponent from '../../components/ArenaComponent';
import WS from 'jest-websocket-mock';

// Load the feature file
const feature = loadFeature('./src/features/arena_rendering.feature');

// Mock data
const mockBattleState = {
  battleId: 'test-battle-id',
  battleName: 'Test Battle',
  arenaWidth: 20,
  arenaHeight: 20,
  robotMovementTimeSeconds: 1.0,
  battleState: 'READY',
  robots: [
    {
      id: 'robot-1',
      name: 'Robot 1',
      battleId: 'test-battle-id',
      positionX: 5,
      positionY: 5,
      direction: 'NORTH',
      status: 'IDLE',
      targetBlocks: 0,
      blocksRemaining: 0
    },
    {
      id: 'robot-2',
      name: 'Robot 2',
      battleId: 'test-battle-id',
      positionX: 15,
      positionY: 15,
      direction: 'SOUTH',
      status: 'IDLE',
      targetBlocks: 0,
      blocksRemaining: 0
    }
  ]
};

// WebSocket server mock
let server: WS;

defineFeature(feature, (test) => {
  beforeEach(async () => {
    // Mock window.location.host for the component
    Object.defineProperty(window, 'location', {
      value: {
        host: 'localhost'
      },
      writable: true
    });
    
    // Create a mock WebSocket server
    server = new WS('ws://localhost/battle-state/test-battle-id');
  });

  afterEach(() => {
    // Clean up the mock server
    WS.clean();
  });

  test('Render the initial arena state', ({ given, when, then, and }) => {
    given('the battle state websocket is available', () => {
      // This is handled by the mock WebSocket server
    });

    and('a battle with ID "test-battle-id" exists on the server', () => {
      // This is handled by the mock data
    });

    and('the battle has an arena with dimensions 20x20', () => {
      // This is handled by the mock data
    });

    and('the battle has 2 robots registered', () => {
      // This is handled by the mock data
    });

    when('I navigate to the arena page', () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));
    });

    and('I connect to the battle state websocket', async () => {
      // Wait for the WebSocket connection to be established
      await server.connected;
      
      // Send the mock battle state to the client
      server.send(JSON.stringify(mockBattleState));
    });

    then('I should see the arena with dimensions 20x20', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
      });
      
      const arenaGrid = screen.getByTestId('arena-grid');
      expect(arenaGrid).toHaveAttribute('data-width', '20');
      expect(arenaGrid).toHaveAttribute('data-height', '20');
    });

    and('I should see 2 robots on the arena', async () => {
      await waitFor(() => {
        const robots = screen.getAllByTestId(/robot-/);
        expect(robots).toHaveLength(2);
      });
    });

    and('each robot should be displayed at its correct position', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('robot-robot-1')).toBeInTheDocument();
      });
      
      const robot1 = screen.getByTestId('robot-robot-1');
      const robot2 = screen.getByTestId('robot-robot-2');

      expect(robot1).toHaveAttribute('data-x', '5');
      expect(robot1).toHaveAttribute('data-y', '5');
      expect(robot2).toHaveAttribute('data-x', '15');
      expect(robot2).toHaveAttribute('data-y', '15');
    });
  });

  test('Update the arena when robot positions change', ({ given, and, when, then }) => {
    given('the battle state websocket is available', () => {
      // This is handled by the mock WebSocket server
    });

    and('a battle with ID "test-battle-id" exists on the server', () => {
      // This is handled by the mock data
    });

    and('the battle has an arena with dimensions 20x20', () => {
      // This is handled by the mock data
    });

    and('the battle has 2 robots registered', () => {
      // This is handled by the mock data
    });

    given('I am viewing the arena', async () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));

      // Wait for the WebSocket connection to be established
      await server.connected;
      
      // Send the initial battle state
      server.send(JSON.stringify(mockBattleState));

      // Wait for the arena to render
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
      });
    });

    when('a robot moves to a new position', () => {
      // Create updated battle state with new robot position
      const updatedBattleState = {
        ...mockBattleState,
        robots: [
          {
            ...mockBattleState.robots[0],
            positionX: 6,
            positionY: 6,
            status: 'MOVING'
          },
          mockBattleState.robots[1]
        ]
      };

      // Send the updated battle state
      server.send(JSON.stringify(updatedBattleState));
    });

    then('the robot\'s position on the arena should be updated', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('robot-robot-1')).toHaveAttribute('data-x', '6');
      });
      
      const robot1 = screen.getByTestId('robot-robot-1');
      expect(robot1).toHaveAttribute('data-y', '6');
    });
  });

  test('Display robot status', ({ given, and, when, then }) => {
    given('the battle state websocket is available', () => {
      // This is handled by the mock WebSocket server
    });

    and('a battle with ID "test-battle-id" exists on the server', () => {
      // This is handled by the mock data
    });

    and('the battle has an arena with dimensions 20x20', () => {
      // This is handled by the mock data
    });

    and('the battle has 2 robots registered', () => {
      // This is handled by the mock data
    });

    given('I am viewing the arena', async () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));

      // Wait for the WebSocket connection to be established
      await server.connected;
      
      // Send the initial battle state
      server.send(JSON.stringify(mockBattleState));

      // Wait for the arena to render
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
      });
    });

    when('a robot\'s status changes to "MOVING"', () => {
      // Create updated battle state with new robot status
      const updatedBattleState = {
        ...mockBattleState,
        robots: [
          {
            ...mockBattleState.robots[0],
            status: 'MOVING'
          },
          mockBattleState.robots[1]
        ]
      };

      // Send the updated battle state
      server.send(JSON.stringify(updatedBattleState));
    });

    then('the robot should be displayed with a "MOVING" indicator', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('robot-robot-1')).toHaveAttribute('data-status', 'MOVING');
      });
      
      expect(screen.getByText('MOVING')).toBeInTheDocument();
    });
  });

  test('Handle connection errors', ({ given, and, when, then }) => {
    given('the battle state websocket is available', () => {
      // This is handled by the mock WebSocket server
    });

    and('a battle with ID "test-battle-id" exists on the server', () => {
      // This is handled by the mock data
    });

    and('the battle has an arena with dimensions 20x20', () => {
      // This is handled by the mock data
    });

    and('the battle has 2 robots registered', () => {
      // This is handled by the mock data
    });

    when('the websocket connection fails', async () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));

      // Wait for the WebSocket connection to be established
      await server.connected;
      
      // Simulate connection error by closing the server
      server.error();
    });

    then('I should see an error message', async () => {
      await waitFor(() => {
        expect(screen.getByText(/connection error/i)).toBeInTheDocument();
      });
    });

    and('I should have an option to reconnect', async () => {
      await waitFor(() => {
        expect(screen.getByText(/reconnect/i)).toBeInTheDocument();
      });
    });
  });
});
