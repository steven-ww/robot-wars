import { defineFeature, loadFeature } from 'jest-cucumber';
import { render, screen, waitFor } from '@testing-library/react';
import WS from 'jest-websocket-mock';
import React from 'react';
import ArenaComponent from '../../components/ArenaComponent';

const feature = loadFeature('src/features/battle_results.feature');

defineFeature(feature, test => {
  let server: WS;

  beforeEach(() => {
    server = new WS('ws://localhost/battle-state/test-battle-id');
  });

  afterEach(() => {
    WS.clean();
  });

  test('Display battle results when battle is completed with a winner', ({
    given,
    when,
    then,
  }) => {
    given('a battle arena is displayed for a completed battle', async () => {
      render(
        React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
      );
      await server.connected;
    });

    when('the battle state shows COMPLETED with a winner', async () => {
      const completedBattleState = {
        battleId: 'test-battle-id',
        battleName: 'Test Battle',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 1,
        battleState: 'COMPLETED',
        winnerId: 'robot-1',
        winnerName: 'Robot Alpha',
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
            status: 'CRASHED',
            targetBlocks: 0,
            blocksRemaining: 0,
          },
        ],
        walls: [],
      };

      server.send(JSON.stringify(completedBattleState));
    });

    then('the battle results should be displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('🏆 Battle Complete!')).toBeInTheDocument();
      });
    });

    then('the winner should be announced', async () => {
      await waitFor(() => {
        expect(screen.getByText('Winner: Robot Alpha')).toBeInTheDocument();
      });
      await waitFor(() => {
        expect(
          screen.getByText('Congratulations! Robot Alpha has won the battle!')
        ).toBeInTheDocument();
      });
    });

    then('the battle summary should show final statistics', async () => {
      await waitFor(() => {
        expect(screen.getByText('Battle Summary:')).toBeInTheDocument();
      });
      await waitFor(() => {
        expect(screen.getByText('Battle: Test Battle')).toBeInTheDocument();
      });
      await waitFor(() => {
        // Use getAllByText to handle multiple instances and check the one in battle summary
        const arenaSizeElements = screen.getAllByText('Arena Size: 20x20');
        expect(arenaSizeElements.length).toBeGreaterThan(0);
      });
      await waitFor(() => {
        expect(screen.getByText('Total Robots: 2')).toBeInTheDocument();
      });
    });

    then('the final robot status should be displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('Final Robot Status:')).toBeInTheDocument();
      });
      await waitFor(() => {
        const robotAlphaElements = screen.getAllByText('Robot Alpha');
        expect(robotAlphaElements.length).toBeGreaterThan(0);
      });
      await waitFor(() => {
        const robotBetaElements = screen.getAllByText('Robot Beta');
        expect(robotBetaElements.length).toBeGreaterThan(0);
      });
      await waitFor(() => {
        expect(screen.getByText('👑 WINNER')).toBeInTheDocument();
      });
    });

    then('the winner robot should be highlighted on the arena', async () => {
      await waitFor(() => {
        const winnerRobot = screen.getByTestId('robot-robot-1');
        expect(winnerRobot).toHaveAttribute('data-winner', 'true');
      });
      await waitFor(() => {
        const winnerRobot = screen.getByTestId('robot-robot-1');
        expect(winnerRobot).toHaveClass('robot-winner');
      });
    });
  });

  test('Display battle results when battle is completed without a clear winner', ({
    given,
    when,
    then,
  }) => {
    given('a battle arena is displayed for a completed battle', async () => {
      render(
        React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
      );
      await server.connected;
    });

    when('the battle state shows COMPLETED without a winner', async () => {
      const completedBattleState = {
        battleId: 'test-battle-id',
        battleName: 'Test Battle',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 1,
        battleState: 'COMPLETED',
        robots: [
          {
            id: 'robot-1',
            name: 'Robot Alpha',
            battleId: 'test-battle-id',
            positionX: 5,
            positionY: 5,
            direction: 'NORTH',
            status: 'CRASHED',
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
            status: 'CRASHED',
            targetBlocks: 0,
            blocksRemaining: 0,
          },
        ],
        walls: [],
      };

      server.send(JSON.stringify(completedBattleState));
    });

    then('the battle results should be displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('🏆 Battle Complete!')).toBeInTheDocument();
      });
    });

    then('a no winner message should be shown', async () => {
      await waitFor(() => {
        expect(screen.getByText('Battle Ended')).toBeInTheDocument();
      });
      await waitFor(() => {
        expect(
          screen.getByText('The battle has completed with no clear winner.')
        ).toBeInTheDocument();
      });
    });
  });

  test('Battle state badge shows correct status', ({ given, when, then }) => {
    given('a battle arena is displayed', async () => {
      render(
        React.createElement(ArenaComponent, { battleId: 'test-battle-id' })
      );
      await server.connected;
    });

    when('the battle state is IN_PROGRESS', async () => {
      const inProgressBattleState = {
        battleId: 'test-battle-id',
        battleName: 'Test Battle',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 1,
        battleState: 'IN_PROGRESS',
        robots: [
          {
            id: 'robot-1',
            name: 'Robot Alpha',
            battleId: 'test-battle-id',
            positionX: 5,
            positionY: 5,
            direction: 'NORTH',
            status: 'MOVING',
            targetBlocks: 0,
            blocksRemaining: 0,
          },
        ],
        walls: [],
      };

      server.send(JSON.stringify(inProgressBattleState));
    });

    then('the battle state badge should show IN_PROGRESS', async () => {
      await waitFor(() => {
        const badge = screen.getByText('IN_PROGRESS');
        expect(badge).toHaveClass('battle-state-badge');
      });
      await waitFor(() => {
        const badge = screen.getByText('IN_PROGRESS');
        expect(badge).toHaveClass('state-in_progress');
      });
    });

    when('the battle state changes to COMPLETED', async () => {
      const completedBattleState = {
        battleId: 'test-battle-id',
        battleName: 'Test Battle',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 1,
        battleState: 'COMPLETED',
        winnerId: 'robot-1',
        winnerName: 'Robot Alpha',
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
        ],
        walls: [],
      };

      server.send(JSON.stringify(completedBattleState));
    });

    then('the battle state badge should show COMPLETED', async () => {
      await waitFor(() => {
        const badge = screen.getByText('COMPLETED');
        expect(badge).toHaveClass('battle-state-badge');
      });
      await waitFor(() => {
        const badge = screen.getByText('COMPLETED');
        expect(badge).toHaveClass('state-completed');
      });
    });
  });
});
