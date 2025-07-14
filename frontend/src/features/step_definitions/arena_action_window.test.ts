import { defineFeature, loadFeature } from 'jest-cucumber';
import { render, screen, waitFor, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import React from 'react';
import ArenaComponent from '../../components/ArenaComponent';
import WS from 'jest-websocket-mock';

// Load the feature file
const feature = loadFeature('./src/features/arena_action_window.feature');

// Mock data for battle in progress
const mockBattleStateInProgress = {
  battleId: 'test-battle-id',
  battleName: 'Test Battle',
  arenaWidth: 20,
  arenaHeight: 20,
  robotMovementTimeSeconds: 1.0,
  battleState: 'IN_PROGRESS',
  robots: [
    {
      id: 'robot-1',
      name: 'Robot Alpha',
      battleId: 'test-battle-id',
      positionX: 5,
      positionY: 5,
      direction: 'NORTH',
      status: 'IDLE',
      targetBlocks: 0,
      blocksRemaining: 0,
    },
    {
      id: 'robot-2',
      name: 'Robot Beta',
      battleId: 'test-battle-id',
      positionX: 15,
      positionY: 15,
      direction: 'SOUTH',
      status: 'MOVING',
      targetBlocks: 0,
      blocksRemaining: 0,
    },
  ],
  walls: [],
  robotActions: [
    {
      robotId: 'robot-1',
      robotName: 'Robot Alpha',
      action: 'radar',
      timestamp: '2023-01-01T12:00:00Z',
    },
    {
      robotId: 'robot-2',
      robotName: 'Robot Beta',
      action: 'move',
      timestamp: '2023-01-01T12:00:01Z',
    },
  ],
};

// WebSocket server mock
let server: WS;

defineFeature(feature, test => {
  beforeEach(async () => {
    // Mock window.location.host for the component
    Object.defineProperty(window, 'location', {
      value: {
        host: 'localhost',
        protocol: 'http:',
      },
      writable: true,
    });

    // Mock NODE_ENV to development so WebSocket connects to localhost:8080
    process.env.NODE_ENV = 'development';

    // Create a mock WebSocket server
    server = new WS('ws://localhost:8080/battle-state/test-battle-id');
  });

  afterEach(() => {
    // Clean up the mock server
    WS.clean();
  });

  test('Show action window on the right of the arena during battle', ({
    given,
    when,
    then,
    and,
  }) => {
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

    given('a battle is in progress', () => {
      // This is handled by the mock data showing battle state as IN_PROGRESS
    });

    when('I navigate to the arena page for the battle', async () => {
      render(
        React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
      );

      // Wait for the WebSocket connection to be established
      await server.connected;

      // Send the mock battle state to the client
      server.send(JSON.stringify(mockBattleStateInProgress));

      // Wait for the arena to render
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
      });
    });

    then('I should see a window on the right of the arena', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('action-window')).toBeInTheDocument();
      });

      const actionWindow = screen.getByTestId('action-window');
      const arenaContainer = screen.getByTestId('arena-container');

      // Check that the action window is positioned to the right of the arena
      expect(actionWindow).toHaveClass('action-window');
      expect(arenaContainer).toHaveClass('arena-with-action-window');
    });

    and('the window should show robot actions', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('action-list')).toBeInTheDocument();
      });

      const actionList = screen.getByTestId('action-list');
      expect(actionList).toBeInTheDocument();
    });

    and(
      'the window should display the name of the robot for each action',
      async () => {
        await waitFor(() => {
          const actionWindow = screen.getByTestId('action-window');
          expect(actionWindow).toHaveTextContent('Robot Alpha');
        });
        await waitFor(() => {
          const actionWindow = screen.getByTestId('action-window');
          expect(actionWindow).toHaveTextContent('Robot Beta');
        });
      }
    );

    and(
      'the window should display the action taken (e.g. radar, move)',
      async () => {
        await waitFor(() => {
          expect(screen.getByText('radar')).toBeInTheDocument();
        });
        await waitFor(() => {
          expect(screen.getByText('move')).toBeInTheDocument();
        });
      }
    );
  });

  test('Action window scrolls as battle continues', ({
    given,
    when,
    then,
    and,
  }) => {
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

    given('I am viewing the arena with the action window visible', async () => {
      render(
        React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
      );

      // Wait for the WebSocket connection to be established
      await server.connected;

      // Send the initial battle state
      server.send(JSON.stringify(mockBattleStateInProgress));

      // Wait for the arena and action window to render
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByTestId('action-window')).toBeInTheDocument();
      });
    });

    and('the action window is displaying robot actions', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('action-list')).toBeInTheDocument();
      });
    });

    when('multiple robot actions occur during the battle', async () => {
      // Send multiple action updates
      const actionsUpdate = {
        ...mockBattleStateInProgress,
        robotActions: [
          ...mockBattleStateInProgress.robotActions,
          {
            robotId: 'robot-1',
            robotName: 'Robot Alpha',
            action: 'fire_laser',
            timestamp: '2023-01-01T12:00:02Z',
          },
          {
            robotId: 'robot-2',
            robotName: 'Robot Beta',
            action: 'radar',
            timestamp: '2023-01-01T12:00:03Z',
          },
          {
            robotId: 'robot-1',
            robotName: 'Robot Alpha',
            action: 'move',
            timestamp: '2023-01-01T12:00:04Z',
          },
        ],
      };

      server.send(JSON.stringify(actionsUpdate));

      // Wait for the updates to be processed
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 100));
      });
    });

    then('the action window should allow scrolling', async () => {
      await waitFor(() => {
        const actionList = screen.getByTestId('action-list');
        expect(actionList).toHaveClass('scrollable');
      });
    });

    and('new actions should be added to the bottom of the window', async () => {
      await waitFor(() => {
        expect(screen.getByText('fire_laser')).toBeInTheDocument();
      });

      const actionItems = screen.getAllByTestId(/^action-item-/);
      expect(actionItems.length).toBeGreaterThan(2);
    });

    and(
      'older actions should scroll up and out of view as needed',
      async () => {
        // This is tested by ensuring the scrollable container has the correct class
        await waitFor(() => {
          const actionList = screen.getByTestId('action-list');
          expect(actionList).toHaveClass('scrollable');
        });
        await waitFor(() => {
          const actionList = screen.getByTestId('action-list');
          expect(actionList).toHaveClass('action-list');
        });
      }
    );
  });

  test('Action window fits within browser window for different arena sizes', ({
    given,
    when,
    then,
    and,
  }) => {
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

    given('the arena page is rendered in a desktop browser window', () => {
      // Mock desktop browser window size
      Object.defineProperty(window, 'innerWidth', {
        writable: true,
        configurable: true,
        value: 1920,
      });
      Object.defineProperty(window, 'innerHeight', {
        writable: true,
        configurable: true,
        value: 1080,
      });
    });

    when(
      /^I view an arena with dimensions (.*)x(.*)$/,
      async (width, height) => {
        const battleStateWithSize = {
          ...mockBattleStateInProgress,
          arenaWidth: parseInt(width),
          arenaHeight: parseInt(height),
        };

        render(
          React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
        );

        // Wait for the WebSocket connection to be established
        await server.connected;

        // Send the battle state with the specified dimensions
        server.send(JSON.stringify(battleStateWithSize));

        // Wait for the arena to render
        await waitFor(() => {
          expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
        });
      }
    );

    then('the entire arena should be visible without scrolling', async () => {
      await waitFor(() => {
        const arenaGrid = screen.getByTestId('arena-grid');
        expect(arenaGrid).toBeInTheDocument();
      });

      const arenaContainer = screen.getByTestId('arena-container');
      expect(arenaContainer).toHaveClass('fit-to-window');
    });

    and('the action window should be visible on the right', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('action-window')).toBeInTheDocument();
      });

      const actionWindow = screen.getByTestId('action-window');
      expect(actionWindow).toHaveClass('action-window');
    });

    and(
      'both the arena and action window should fit within the browser window',
      async () => {
        await waitFor(() => {
          const arenaContainer = screen.getByTestId('arena-container');
          expect(arenaContainer).toHaveClass('responsive-layout');
        });

        const actionWindow = screen.getByTestId('action-window');
        expect(actionWindow).toHaveClass('responsive-width');
      }
    );
  });
});
