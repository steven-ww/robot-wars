import { defineFeature, loadFeature } from 'jest-cucumber';
import {
  render,
  screen,
  waitFor,
  fireEvent,
  act,
} from '@testing-library/react';
import '@testing-library/jest-dom';
import React from 'react';
import App from '../../App';

// Load the feature file
const feature = loadFeature('./src/features/main_app_arena.feature');

// Mock fetch globally
global.fetch = jest.fn();
const mockFetch = global.fetch as jest.MockedFunction<typeof fetch>;

// Helper function to render App component
const renderApp = () => {
  render(React.createElement(App));
};

// Helper function for user interactions
const userInteraction = async (fn: () => Promise<void> | void) => {
  await act(async () => {
    await fn();
  });
};

// Mock data
const mockBattles = [
  {
    id: 'battle-123',
    name: 'Test Battle 123',
    arenaWidth: 20,
    arenaHeight: 20,
    robotMovementTimeSeconds: 1.0,
    state: 'READY',
    robotCount: 2,
    robots: [
      {
        id: 'robot-1',
        name: 'Robot 1',
        status: 'IDLE',
      },
      {
        id: 'robot-2',
        name: 'Robot 2',
        status: 'IDLE',
      },
    ],
  },
  {
    id: 'battle-456',
    name: 'Test Battle 456',
    arenaWidth: 30,
    arenaHeight: 30,
    robotMovementTimeSeconds: 0.5,
    state: 'IN_PROGRESS',
    robotCount: 1,
    robots: [
      {
        id: 'robot-3',
        name: 'Robot 3',
        status: 'MOVING',
      },
    ],
  },
];

const mockSingleBattle = [
  {
    id: 'default-battle',
    name: 'Default Battle',
    arenaWidth: 25,
    arenaHeight: 25,
    robotMovementTimeSeconds: 1.0,
    state: 'CREATED',
    robotCount: 0,
    robots: [],
  },
];

defineFeature(feature, test => {
  beforeEach(() => {
    mockFetch.mockClear();
    // Clear localStorage to reset battle selection
    localStorage.clear();
  });

  test('Battle Arena tab shows message when no battle is selected', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given('there are existing battles on the server', () => {
      // Mock the GET /api/battles endpoint to return existing battles
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    and('no battle has been selected yet', () => {
      // localStorage is already cleared in beforeEach
      expect(localStorage.getItem('selectedBattleId')).toBeNull();
    });

    when('I navigate to the Battle Arena tab', async () => {
      renderApp();

      await waitFor(() => {
        expect(screen.getByText('Battle Arena')).toBeInTheDocument();
      });

      const arenaTab = screen.getByText('Battle Arena');
      await userInteraction(() => {
        fireEvent.click(arenaTab);
      });
    });

    then(
      'I should see a message indicating no battle is selected',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByText('No battle selected for arena view.')
          ).toBeInTheDocument();
        });
      }
    );

    and('I should see a "Select Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Select Battle')).toBeInTheDocument();
      });
    });

    and('I should not see any arena visualization', async () => {
      // Should not see the arena grid or battle info
      expect(screen.queryByTestId('arena-grid')).not.toBeInTheDocument();
      expect(screen.queryByText('Battle State:')).not.toBeInTheDocument();
    });
  });

  test('Battle Arena tab shows the last selected battle', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given('there are existing battles on the server', () => {
      // Mock the GET /api/battles endpoint to return existing battles
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    and('I have previously selected a battle with ID "battle-123"', () => {
      localStorage.setItem('selectedBattleId', 'battle-123');
    });

    when('I navigate to the Battle Arena tab', async () => {
      renderApp();

      await waitFor(() => {
        expect(screen.getByText('Battle Arena')).toBeInTheDocument();
      });

      const arenaTab = screen.getByText('Battle Arena');
      await userInteraction(() => {
        fireEvent.click(arenaTab);
      });
    });

    then('I should see the arena for battle "battle-123"', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('arena-component')).toBeInTheDocument();
      });
    });

    and('I should see the battle name displayed', async () => {
      await waitFor(() => {
        expect(
          screen.getByText('Battle Arena - Test Battle 123')
        ).toBeInTheDocument();
      });
    });

    and('I should see a "Change Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Change Battle')).toBeInTheDocument();
      });
    });
  });

  test('Battle Arena tab shows default battle when available', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given(
      'there is exactly one battle on the server with ID "default-battle"',
      () => {
        // Mock the GET /api/battles endpoint to return single battle
        mockFetch.mockResolvedValue({
          ok: true,
          json: async () => mockSingleBattle,
        } as Response);
      }
    );

    and('no battle has been previously selected', () => {
      // localStorage is already cleared in beforeEach
      expect(localStorage.getItem('selectedBattleId')).toBeNull();
    });

    when('I navigate to the Battle Arena tab', async () => {
      renderApp();

      await waitFor(() => {
        expect(screen.getByText('Battle Arena')).toBeInTheDocument();
      });

      const arenaTab = screen.getByText('Battle Arena');
      await userInteraction(() => {
        fireEvent.click(arenaTab);
      });
    });

    then('I should see the arena for battle "default-battle"', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('arena-component')).toBeInTheDocument();
      });
    });

    and('I should see the battle name displayed', async () => {
      await waitFor(() => {
        expect(
          screen.getByText('Battle Arena - Default Battle')
        ).toBeInTheDocument();
      });
    });

    and('I should see a "Change Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Change Battle')).toBeInTheDocument();
      });
    });
  });

  test('Select a battle from the Battle Arena tab', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given('there are multiple battles on the server', () => {
      // Mock the GET /api/battles endpoint to return multiple battles
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    and('I am on the Battle Arena tab with no battle selected', async () => {
      renderApp();

      await waitFor(() => {
        expect(screen.getByText('Battle Arena')).toBeInTheDocument();
      });

      const arenaTab = screen.getByText('Battle Arena');
      await userInteraction(() => {
        fireEvent.click(arenaTab);
      });

      await waitFor(() => {
        expect(
          screen.getByText('No battle selected for arena view.')
        ).toBeInTheDocument();
      });
    });

    when('I click the "Select Battle" button', async () => {
      const selectButton = screen.getByText('Select Battle');
      await userInteraction(() => {
        fireEvent.click(selectButton);
      });
    });

    then('I should see a list of available battles', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle 123')).toBeInTheDocument();
      });
      expect(screen.getByText('Test Battle 456')).toBeInTheDocument();
    });

    and('I can select a battle to view its arena', async () => {
      const battle123Button = screen.getByTestId('select-battle-battle-123');
      await userInteraction(() => {
        fireEvent.click(battle123Button);
      });

      await waitFor(() => {
        expect(screen.getByTestId('arena-component')).toBeInTheDocument();
      });
    });

    and(
      'the selected battle should be remembered for future visits to the tab',
      async () => {
        await waitFor(() => {
          expect(localStorage.getItem('selectedBattleId')).toBe('battle-123');
        });
      }
    );
  });

  test('Change battle from the Battle Arena tab', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given(
      'I am viewing a battle arena for battle "current-battle"',
      async () => {
        localStorage.setItem('selectedBattleId', 'battle-123');

        // Mock the GET /api/battles endpoint to return battles
        mockFetch.mockResolvedValue({
          ok: true,
          json: async () => mockBattles,
        } as Response);

        renderApp();

        await waitFor(() => {
          expect(screen.getByText('Battle Arena')).toBeInTheDocument();
        });

        const arenaTab = screen.getByText('Battle Arena');
        await userInteraction(() => {
          fireEvent.click(arenaTab);
        });

        await waitFor(() => {
          expect(screen.getByTestId('arena-component')).toBeInTheDocument();
        });
      }
    );

    and('there are other battles available on the server', () => {
      // Already mocked in the previous step
    });

    when('I click the "Change Battle" button', async () => {
      const changeButton = screen.getByText('Change Battle');
      await userInteraction(() => {
        fireEvent.click(changeButton);
      });
    });

    then('I should see a list of available battles', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle 123')).toBeInTheDocument();
      });
      expect(screen.getByText('Test Battle 456')).toBeInTheDocument();
    });

    and('I can select a different battle to view its arena', async () => {
      const battle456Button = screen.getByTestId('select-battle-battle-456');
      await userInteraction(() => {
        fireEvent.click(battle456Button);
      });

      await waitFor(() => {
        expect(screen.getByTestId('arena-component')).toBeInTheDocument();
      });
    });

    and(
      'the newly selected battle should be remembered for future visits to the tab',
      async () => {
        await waitFor(() => {
          expect(localStorage.getItem('selectedBattleId')).toBe('battle-456');
        });
      }
    );
  });

  test('Battle Arena tab handles invalid battle ID gracefully', ({
    given,
    and,
    when,
    then,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given(
      'I have previously selected a battle with ID "deleted-battle"',
      () => {
        localStorage.setItem('selectedBattleId', 'deleted-battle');
      }
    );

    and('the battle "deleted-battle" no longer exists on the server', () => {
      // Mock the GET /api/battles endpoint to return battles without the deleted one
      mockFetch.mockResolvedValue({
        ok: true,
        json: async () => mockBattles, // doesn't include 'deleted-battle'
      } as Response);
    });

    when('I navigate to the Battle Arena tab', async () => {
      renderApp();

      await waitFor(() => {
        expect(screen.getByText('Battle Arena')).toBeInTheDocument();
      });

      const arenaTab = screen.getByText('Battle Arena');
      await userInteraction(() => {
        fireEvent.click(arenaTab);
      });
    });

    then(
      'I should see a message indicating the battle is no longer available',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByText(
              'The previously selected battle is no longer available.'
            )
          ).toBeInTheDocument();
        });
      }
    );

    and('I should see a "Select Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Select Battle')).toBeInTheDocument();
      });
    });

    and('I should not see any arena visualization', async () => {
      // Should not see the arena grid or battle info
      expect(screen.queryByTestId('arena-grid')).not.toBeInTheDocument();
      expect(screen.queryByText('Battle State:')).not.toBeInTheDocument();
    });
  });
});
