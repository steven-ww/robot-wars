import { defineFeature, loadFeature } from 'jest-cucumber';
import {
  render,
  screen,
  waitFor,
  fireEvent,
  act,
} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import React from 'react';
import BattleManagement from '../../components/BattleManagement';

// Load the feature file
const feature = loadFeature('./src/features/battle_management.feature');

// Mock fetch globally
global.fetch = jest.fn();
const mockFetch = global.fetch as jest.MockedFunction<typeof fetch>;

// Helper function to render component
const renderBattleManagement = () => {
  render(React.createElement(BattleManagement));
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
    id: 'battle-1',
    name: 'Test Battle 1',
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
    id: 'battle-2',
    name: 'Test Battle 2',
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

const mockNewBattle = {
  id: 'battle-3',
  name: 'Test Battle',
  arenaWidth: 100,
  arenaHeight: 100,
  robotMovementTimeSeconds: 1.0,
  state: 'CREATED',
  robotCount: 0,
  robots: [],
};

defineFeature(feature, test => {
  beforeEach(() => {
    mockFetch.mockClear();
  });

  test('View all existing battles on page load', ({
    given,
    when,
    then,
    and,
  }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the when step
    });

    given('there are existing battles on the server', () => {
      // Mock the GET /api/battles endpoint to return existing battles
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    when('I navigate to the battle management page', () => {
      renderBattleManagement();
    });

    then('I should see a list of all battles', async () => {
      await waitFor(() => {
        expect(screen.getByText('Current Battles')).toBeInTheDocument();
      });

      expect(screen.getByText('Test Battle 1')).toBeInTheDocument();
      expect(screen.getByText('Test Battle 2')).toBeInTheDocument();
    });

    and('each battle should display its name and current status', async () => {
      await waitFor(() => {
        expect(screen.getByText('READY')).toBeInTheDocument();
      });

      expect(screen.getByText('IN_PROGRESS')).toBeInTheDocument();
    });

    and('each battle should show the number of robots registered', async () => {
      await waitFor(() => {
        expect(screen.getByText('Robots (2):')).toBeInTheDocument();
      });

      expect(screen.getByText('Robots (1):')).toBeInTheDocument();
    });

    and('each battle should show the arena dimensions', async () => {
      await waitFor(() => {
        const arena20x20Elements = screen.getAllByText((content, element) => {
          const text = element?.textContent || '';
          return text.includes('Arena:') && text.includes('20 x 20');
        })[0];
        expect(arena20x20Elements).toBeInTheDocument();
      });

      const arena30x30Elements = screen.getAllByText((content, element) => {
        const text = element?.textContent || '';
        return text.includes('Arena:') && text.includes('30 x 30');
      })[0];
      expect(arena30x30Elements).toBeInTheDocument();
    });
  });

  test('View empty battle list', ({ given, when, then, and }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given('there are no battles on the server', () => {
      // Mock the GET /api/battles endpoint to return empty array
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    when('I navigate to the battle management page', () => {
      renderBattleManagement();
    });

    then('I should see a message indicating no battles exist', async () => {
      await waitFor(() => {
        expect(screen.getByText('No battles found')).toBeInTheDocument();
      });
    });

    and('I should see a "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });
    });
  });

  test('Create a new battle with default settings', ({
    given,
    when,
    and,
    then,
  }) => {
    given('the battle management API is available', () => {
      // Mock initial GET request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    given('I am on the battle management page', () => {
      renderBattleManagement();
    });

    when('I click the "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });

      await userInteraction(async () => {
        const createButton = screen.getByText('Create Battle');
        fireEvent.click(createButton);
      });
    });

    and('I enter "Test Battle" as the battle name', async () => {
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Enter battle name')
        ).toBeInTheDocument();
      });

      await userInteraction(async () => {
        const nameInput = screen.getByPlaceholderText('Enter battle name');
        await userEvent.type(nameInput, 'Test Battle');
      });
    });

    and('I click "Create" without specifying optional parameters', async () => {
      // Mock the POST request for battle creation
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockNewBattle,
      } as Response);

      const createSubmitButton = screen.getByRole('button', { name: 'Create' });
      await userInteraction(() => {
        fireEvent.click(createSubmitButton);
      });
    });

    then('a new battle should be created with default settings', async () => {
      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith('/api/battles', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: 'Test Battle',
          }),
        });
      });
    });

    and('the battle should appear in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle')).toBeInTheDocument();
      });
    });

    and('I should see a success message', async () => {
      await waitFor(() => {
        expect(
          screen.getByText('Battle "Test Battle" created successfully!')
        ).toBeInTheDocument();
      });
    });
  });

  test('Create a new battle with custom arena dimensions', ({
    given,
    when,
    and,
    then,
  }) => {
    given('the battle management API is available', () => {
      // Mock initial GET request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('I click the "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Battle');
      fireEvent.click(createButton);
    });

    and('I enter "Custom Battle" as the battle name', async () => {
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Enter battle name')
        ).toBeInTheDocument();
      });

      const nameInput = screen.getByPlaceholderText('Enter battle name');
      await userEvent.type(nameInput, 'Custom Battle');
    });

    and('I set arena width to 50', async () => {
      const widthInput = screen.getByPlaceholderText('Default arena width');
      await userEvent.type(widthInput, '50');
    });

    and('I set arena height to 50', async () => {
      const heightInput = screen.getByPlaceholderText('Default arena height');
      await userEvent.type(heightInput, '50');
    });

    and('I click "Create"', async () => {
      // Mock the POST request for battle creation with custom dimensions
      const customBattle = {
        ...mockNewBattle,
        name: 'Custom Battle',
        arenaWidth: 50,
        arenaHeight: 50,
      };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => customBattle,
      } as Response);

      const createSubmitButton = screen.getByRole('button', { name: 'Create' });
      fireEvent.click(createSubmitButton);
    });

    then('a new battle should be created with custom dimensions', async () => {
      await waitFor(() => {
        expect(mockFetch).toHaveBeenCalledWith('/api/battles', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: 'Custom Battle',
            width: 50,
            height: 50,
          }),
        });
      });
    });

    and(
      'the battle should appear in the battle list with dimensions 50x50',
      async () => {
        await waitFor(() => {
          expect(screen.getByText('Custom Battle')).toBeInTheDocument();
        });

        // Check for arena dimensions 50x50
        const arena50x50Elements = screen.getAllByText((content, element) => {
          const text = element?.textContent || '';
          return text.includes('Arena:') && text.includes('50 x 50');
        })[0];
        expect(arena50x50Elements).toBeInTheDocument();
      }
    );
  });

  test('Create a new battle with custom robot movement time', ({
    given,
    when,
    and,
    then,
  }) => {
    given('the battle management API is available', () => {
      // Mock initial GET request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('I click the "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Battle');
      fireEvent.click(createButton);
    });

    and('I enter "Speed Battle" as the battle name', async () => {
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Enter battle name')
        ).toBeInTheDocument();
      });

      const nameInput = screen.getByPlaceholderText('Enter battle name');
      await userEvent.type(nameInput, 'Speed Battle');
    });

    and('I set robot movement time to 0.5 seconds', async () => {
      const timeInput = screen.getByPlaceholderText('Default movement time');
      await userEvent.type(timeInput, '0.5');
    });

    and('I click "Create"', async () => {
      // Mock the POST request for battle creation with custom movement time
      const speedBattle = {
        ...mockNewBattle,
        name: 'Speed Battle',
        robotMovementTimeSeconds: 0.5,
      };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => speedBattle,
      } as Response);

      const createSubmitButton = screen.getByRole('button', { name: 'Create' });
      fireEvent.click(createSubmitButton);
    });

    then(
      'a new battle should be created with custom movement time',
      async () => {
        await waitFor(() => {
          expect(mockFetch).toHaveBeenCalledWith('/api/battles', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              name: 'Speed Battle',
              robotMovementTimeSeconds: 0.5,
            }),
          });
        });
      }
    );

    and('the battle should appear in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Speed Battle')).toBeInTheDocument();
      });

      // Check for the movement time text
      expect(
        screen.getByText((content, element) => content.includes('0.5'))
      ).toBeInTheDocument();
    });
  });

  test('Create a new battle with all custom parameters', ({
    given,
    when,
    and,
    then,
  }) => {
    given('the battle management API is available', () => {
      // Mock initial GET request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('I click the "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Battle');
      fireEvent.click(createButton);
    });

    and('I enter "Full Custom Battle" as the battle name', async () => {
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Enter battle name')
        ).toBeInTheDocument();
      });

      const nameInput = screen.getByPlaceholderText('Enter battle name');
      await userEvent.type(nameInput, 'Full Custom Battle');
    });

    and('I set arena width to 30', async () => {
      const widthInput = screen.getByPlaceholderText('Default arena width');
      await userEvent.type(widthInput, '30');
    });

    and('I set arena height to 40', async () => {
      const heightInput = screen.getByPlaceholderText('Default arena height');
      await userEvent.type(heightInput, '40');
    });

    and('I set robot movement time to 2.0 seconds', async () => {
      const timeInput = screen.getByPlaceholderText('Default movement time');
      await userEvent.type(timeInput, '2.0');
    });

    and('I click "Create"', async () => {
      // Mock the POST request for battle creation with all custom parameters
      const fullCustomBattle = {
        ...mockNewBattle,
        name: 'Full Custom Battle',
        arenaWidth: 30,
        arenaHeight: 40,
        robotMovementTimeSeconds: 2.0,
      };
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => fullCustomBattle,
      } as Response);

      const createSubmitButton = screen.getByRole('button', { name: 'Create' });
      fireEvent.click(createSubmitButton);
    });

    then(
      'a new battle should be created with all custom parameters',
      async () => {
        await waitFor(() => {
          expect(mockFetch).toHaveBeenCalledWith('/api/battles', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              name: 'Full Custom Battle',
              width: 30,
              height: 40,
              robotMovementTimeSeconds: 2.0,
            }),
          });
        });
      }
    );

    and('the battle should appear in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Full Custom Battle')).toBeInTheDocument();
      });

      // Check for arena dimensions and movement time
      expect(
        screen.getByText(content => content.includes('30'))
      ).toBeInTheDocument();
      expect(
        screen.getByText(content => content.includes('40'))
      ).toBeInTheDocument();
      expect(
        screen.getByText(content => content.includes('2'))
      ).toBeInTheDocument();
    });
  });

  test('Handle battle creation errors', ({ given, when, and, then }) => {
    given('the battle management API is available', () => {
      // Mock initial GET request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('I click the "Create Battle" button', async () => {
      await waitFor(() => {
        expect(screen.getByText('Create Battle')).toBeInTheDocument();
      });

      const createButton = screen.getByText('Create Battle');
      fireEvent.click(createButton);
    });

    and('I enter a duplicate battle name', async () => {
      await waitFor(() => {
        expect(
          screen.getByPlaceholderText('Enter battle name')
        ).toBeInTheDocument();
      });

      const nameInput = screen.getByPlaceholderText('Enter battle name');
      await userEvent.type(nameInput, 'Duplicate Battle');
    });

    and('I click "Create"', async () => {
      // Mock the POST request to return an error
      mockFetch.mockResolvedValueOnce({
        ok: false,
        json: async () => ({
          message: 'Battle with name "Duplicate Battle" already exists',
        }),
      } as Response);

      const createSubmitButton = screen.getByRole('button', { name: 'Create' });
      fireEvent.click(createSubmitButton);
    });

    then('I should see an error message', async () => {
      await waitFor(() => {
        expect(
          screen.getByText('Battle with name "Duplicate Battle" already exists')
        ).toBeInTheDocument();
      });
    });

    and('the battle should not be created', async () => {
      // Verify that no new battle appears in the list
      expect(screen.queryByText('Duplicate Battle')).not.toBeInTheDocument();
    });
  });

  test('View robot details for each battle', ({ given, when, then, and }) => {
    given('the battle management API is available', () => {
      // API will be mocked in the next step
    });

    given('there is a battle with 2 robots registered', () => {
      // Mock the GET /api/battles endpoint to return a battle with robots
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [mockBattles[0]],
      } as Response);
    });

    when('I view the battle in the battle list', () => {
      render(React.createElement(BattleManagement));
    });

    then('I should see the robot names', async () => {
      await waitFor(() => {
        const robot1Elements = screen.getAllByText((content, element) => {
          const text = element?.textContent || '';
          return text.includes('Robot 1');
        })[0];
        expect(robot1Elements).toBeInTheDocument();
      });

      const robot2Elements = screen.getAllByText((content, element) => {
        const text = element?.textContent || '';
        return text.includes('Robot 2');
      })[0];
      expect(robot2Elements).toBeInTheDocument();
    });

    and("I should see each robot's current status", async () => {
      await waitFor(() => {
        const robot1Elements = screen.getAllByText((content, element) => {
          const text = element?.textContent || '';
          return text.includes('Robot 1');
        })[0];
        expect(robot1Elements).toBeInTheDocument();
      });

      const robot2Elements = screen.getAllByText((content, element) => {
        const text = element?.textContent || '';
        return text.includes('Robot 2');
      })[0];
      expect(robot2Elements).toBeInTheDocument();
      expect(
        screen.getAllByText((content, element) => {
          const text = element?.textContent || '';
          return (
            element?.tagName === 'LI' &&
            text.includes('Robot') &&
            text.includes('IDLE')
          );
        })
      ).toHaveLength(2);
    });

    and('I should not see robot positions', async () => {
      await waitFor(() => {
        expect(screen.getByText('Current Battles')).toBeInTheDocument();
      });

      // Verify that position information is not displayed
      expect(screen.queryByText(/position/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/x:/i)).not.toBeInTheDocument();
      expect(screen.queryByText(/y:/i)).not.toBeInTheDocument();
    });
  });

  test('Refresh battle list', ({ given, when, and, then }) => {
    given('the battle management API is available', () => {
      // Mock GET request with multiple battles
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('new battles are created by other users', () => {
      // This would happen on the server side
    });

    and('I refresh the page', async () => {
      // Simulated by the initial fetch in this test
    });

    then('I should see the updated battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle 1')).toBeInTheDocument();
      });
    });

    and('all new battles should be displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle 2')).toBeInTheDocument();
      });
    });
  });

  test('Render the arena for a selected battle', ({
    given,
    when,
    and,
    then,
  }) => {
    given('the battle management API is available', () => {
      // Mock GET request with multiple battles
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockBattles,
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    when('new battles are created by other users', () => {
      // This would happen on the server side - battles are already loaded
    });

    and('I select a battle to view', async () => {
      await waitFor(() => {
        expect(screen.getByText('Test Battle 1')).toBeInTheDocument();
      });

      // Find and click the "View Arena" button for the first battle
      const viewArenaButton = screen.getByTestId('view-arena-battle-1');
      await userInteraction(() => {
        fireEvent.click(viewArenaButton);
      });
    });

    then('I should see arena for the battle rendered', async () => {
      await waitFor(() => {
        // Check that the arena component is rendered
        expect(screen.getByTestId('arena-component')).toBeInTheDocument();
      });

      // Check that the arena shows the correct battle information
      expect(screen.getByText('Battle Arena')).toBeInTheDocument();

      // Check that we can go back to the battle list
      expect(screen.getByText('Back to Battle List')).toBeInTheDocument();
    });
  });

  test('Delete a completed battle from the battle list', ({
    given,
    and,
    when,
    then,
  }) => {
    const completedBattle = {
      id: 'completed-battle-123',
      name: 'Completed Battle',
      arenaWidth: 20,
      arenaHeight: 20,
      robotMovementTimeSeconds: 1,
      state: 'COMPLETED',
      robotCount: 2,
      robots: [
        {
          id: 'robot-winner',
          name: 'WinnerBot',
          status: 'IDLE',
        },
        {
          id: 'robot-loser',
          name: 'LoserBot',
          status: 'CRASHED',
        },
      ],
      winnerId: 'robot-winner',
      winnerName: 'WinnerBot',
    };

    given('the battle management API is available', () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [completedBattle],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    and('there is a completed battle with results displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('Completed Battle')).toBeInTheDocument();
      });
    });

    and('the battle shows a winner in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('WinnerBot')).toBeInTheDocument();
        expect(screen.getByText('Winner:')).toBeInTheDocument();
      });
    });

    when('I click the "Delete" button for the completed battle', async () => {
      const deleteButton = screen.getByTestId(
        'delete-battle-completed-battle-123'
      );
      await userInteraction(() => {
        fireEvent.click(deleteButton);
      });
    });

    then(
      'I should see a confirmation dialog asking if I want to delete the battle',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByText(/Are you sure you want to delete/)
          ).toBeInTheDocument();
        });
      }
    );

    when('I confirm the deletion', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      } as Response);

      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);

      const confirmButton = screen.getByText('Confirm');
      await userInteraction(() => {
        fireEvent.click(confirmButton);
      });
    });

    then('the battle should be removed from the battle list', async () => {
      await waitFor(() => {
        expect(screen.queryByText('Completed Battle')).not.toBeInTheDocument();
      });
    });

    and(
      'I should see a success message confirming the battle was deleted',
      async () => {
        await waitFor(() => {
          expect(screen.getByText(/successfully deleted/)).toBeInTheDocument();
        });
      }
    );

    and('the battle should no longer appear in the list', async () => {
      await waitFor(() => {
        expect(screen.queryByText('Completed Battle')).not.toBeInTheDocument();
      });
    });
  });

  test('Cancel deletion of a completed battle', ({
    given,
    and,
    when,
    then,
  }) => {
    const completedBattle = {
      id: 'completed-battle-456',
      name: 'Battle to Cancel',
      arenaWidth: 20,
      arenaHeight: 20,
      robotMovementTimeSeconds: 1,
      state: 'COMPLETED',
      robotCount: 1,
      robots: [
        {
          id: 'robot-survivor',
          name: 'SurvivorBot',
          status: 'IDLE',
        },
      ],
      winnerId: 'robot-survivor',
      winnerName: 'SurvivorBot',
    };

    given('the battle management API is available', () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [completedBattle],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    and('there is a completed battle with results displayed', async () => {
      await waitFor(() => {
        expect(screen.getByText('Battle to Cancel')).toBeInTheDocument();
      });
    });

    when('I click the "Delete" button for the completed battle', async () => {
      const deleteButton = screen.getByTestId(
        'delete-battle-completed-battle-456'
      );
      await userInteraction(() => {
        fireEvent.click(deleteButton);
      });
    });

    and('I see a confirmation dialog', async () => {
      await waitFor(() => {
        expect(
          screen.getByText(/Are you sure you want to delete/)
        ).toBeInTheDocument();
      });
    });

    when('I cancel the deletion', async () => {
      const cancelButton = screen.getByText('Cancel');
      await userInteraction(() => {
        fireEvent.click(cancelButton);
      });
    });

    then('the battle should remain in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Battle to Cancel')).toBeInTheDocument();
      });
    });

    and('no changes should be made', async () => {
      await waitFor(() => {
        expect(screen.getByText('Battle to Cancel')).toBeInTheDocument();
      });
      expect(screen.getByText('SurvivorBot')).toBeInTheDocument();
      expect(screen.getByText('Winner:')).toBeInTheDocument();
    });
  });

  test('Only completed battles can be deleted', ({ given, and, then }) => {
    const battles = [
      {
        id: 'waiting-battle',
        name: 'Waiting Battle',
        state: 'WAITING_ON_ROBOTS',
        robotCount: 0,
        robots: [],
      },
      {
        id: 'ready-battle',
        name: 'Ready Battle',
        state: 'READY',
        robotCount: 2,
        robots: [],
      },
      {
        id: 'in-progress-battle',
        name: 'In Progress Battle',
        state: 'IN_PROGRESS',
        robotCount: 2,
        robots: [],
      },
      {
        id: 'completed-battle',
        name: 'Completed Battle',
        state: 'COMPLETED',
        robotCount: 2,
        robots: [],
        winnerId: 'robot-1',
        winnerName: 'Winner',
      },
    ];

    given('the battle management API is available', () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => battles,
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    and('there are battles in different states', async () => {
      await waitFor(() => {
        expect(screen.getByText('Waiting Battle')).toBeInTheDocument();
      });
      expect(screen.getByText('Ready Battle')).toBeInTheDocument();
      expect(screen.getByText('In Progress Battle')).toBeInTheDocument();
      expect(screen.getByText('Completed Battle')).toBeInTheDocument();
    });

    then(
      'I should only see "Delete" buttons for battles with COMPLETED status',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByTestId('delete-battle-completed-battle')
          ).toBeInTheDocument();
        });
      }
    );

    and(
      'battles with WAITING_ON_ROBOTS, READY, or IN_PROGRESS status should not have delete buttons',
      async () => {
        await waitFor(() => {
          expect(
            screen.queryByTestId('delete-battle-waiting-battle')
          ).not.toBeInTheDocument();
        });
        expect(
          screen.queryByTestId('delete-battle-ready-battle')
        ).not.toBeInTheDocument();
        expect(
          screen.queryByTestId('delete-battle-in-progress-battle')
        ).not.toBeInTheDocument();
      }
    );
  });

  test('Handle battle deletion errors', ({ given, and, when, then }) => {
    const completedBattle = {
      id: 'error-battle-789',
      name: 'Error Battle',
      arenaWidth: 20,
      arenaHeight: 20,
      robotMovementTimeSeconds: 1,
      state: 'COMPLETED',
      robotCount: 1,
      robots: [
        {
          id: 'robot-error',
          name: 'ErrorBot',
          status: 'IDLE',
        },
      ],
      winnerId: 'robot-error',
      winnerName: 'ErrorBot',
    };

    given('the battle management API is available', () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [completedBattle],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    and('there is a completed battle', async () => {
      await waitFor(() => {
        expect(screen.getByText('Error Battle')).toBeInTheDocument();
      });
    });

    when('I attempt to delete the battle', async () => {
      const deleteButton = screen.getByTestId('delete-battle-error-battle-789');
      await userInteraction(() => {
        fireEvent.click(deleteButton);
      });
    });

    and('the deletion fails due to a server error', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        statusText: 'Internal Server Error',
      } as Response);

      const confirmButton = screen.getByText('Confirm');
      await userInteraction(() => {
        fireEvent.click(confirmButton);
      });
    });

    then('I should see an error message', async () => {
      await waitFor(() => {
        expect(screen.getByText('Failed to delete battle. Please try again.')).toBeInTheDocument();
      });
    });

    and('the battle should remain in the battle list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Error Battle')).toBeInTheDocument();
      });
    });
  });

  test('Remove completed battle with winner results from management page', ({
    given,
    and,
    when,
    then,
  }) => {
    const completedBattle = {
      id: 'epic-battle-123',
      name: 'Epic Robot Showdown',
      arenaWidth: 20,
      arenaHeight: 20,
      robotMovementTimeSeconds: 1,
      state: 'COMPLETED',
      robotCount: 2,
      robots: [
        {
          id: 'robot-warrior',
          name: 'RobotWarrior',
          status: 'IDLE',
        },
        {
          id: 'robot-defeated',
          name: 'DefeatedBot',
          status: 'CRASHED',
        },
      ],
      winnerId: 'robot-warrior',
      winnerName: 'RobotWarrior',
    };

    given('the battle management API is available', () => {
      // Mock GET request with completed battle
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [completedBattle],
      } as Response);
    });

    given('I am on the battle management page', () => {
      render(React.createElement(BattleManagement));
    });

    and('there is a completed battle named "Epic Robot Showdown"', async () => {
      await waitFor(() => {
        expect(screen.getByText('Epic Robot Showdown')).toBeInTheDocument();
      });
    });

    and('the battle results show "RobotWarrior" as the winner', async () => {
      await waitFor(() => {
        expect(screen.getByText('RobotWarrior')).toBeInTheDocument();
        expect(screen.getByText('Winner:')).toBeInTheDocument();
      });
    });

    and('the battle status displays as "COMPLETED"', async () => {
      await waitFor(() => {
        expect(screen.getByText('COMPLETED')).toBeInTheDocument();
      });
    });

    and(
      'the winner information is visible in the battle list entry',
      async () => {
        await waitFor(() => {
          expect(screen.getByText('RobotWarrior')).toBeInTheDocument();
          expect(screen.getByText('Winner:')).toBeInTheDocument();
        });
      }
    );

    when('I locate the completed battle in the list', async () => {
      await waitFor(() => {
        expect(screen.getByText('Epic Robot Showdown')).toBeInTheDocument();
      });
    });

    then(
      'I should see a "Delete" button available for this battle',
      async () => {
        await waitFor(() => {
          const deleteButton = screen.getByTestId(
            'delete-battle-epic-battle-123'
          );
          expect(deleteButton).toBeInTheDocument();
        });

        const deleteButton = screen.getByTestId(
          'delete-battle-epic-battle-123'
        );
        expect(deleteButton).toHaveTextContent('Delete');
      }
    );

    when('I click the "Delete" button for "Epic Robot Showdown"', async () => {
      const deleteButton = screen.getByTestId('delete-battle-epic-battle-123');
      await userInteraction(() => {
        fireEvent.click(deleteButton);
      });
    });

    then(
      'I should see a confirmation dialog with the message "Are you sure you want to delete the battle \'Epic Robot Showdown\'? This action cannot be undone."',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByText(
              "Are you sure you want to delete the battle 'Epic Robot Showdown'? This action cannot be undone."
            )
          ).toBeInTheDocument();
        });
      }
    );

    when('I click "Confirm" in the deletion dialog', async () => {
      // Mock successful DELETE request
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      } as Response);

      // Mock updated battle list without the deleted battle
      mockFetch.mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      } as Response);

      const confirmButton = screen.getByText('Confirm');
      await userInteraction(() => {
        fireEvent.click(confirmButton);
      });
    });

    then(
      'the battle "Epic Robot Showdown" should be removed from the battle list',
      async () => {
        await waitFor(() => {
          expect(
            screen.queryByText('Epic Robot Showdown')
          ).not.toBeInTheDocument();
        });
      }
    );

    and(
      'I should see a success notification "Battle \'Epic Robot Showdown\' has been successfully deleted"',
      async () => {
        await waitFor(() => {
          expect(
            screen.getByText(
              "Battle 'Epic Robot Showdown' has been successfully deleted"
            )
          ).toBeInTheDocument();
        });
      }
    );

    and(
      'the battle should no longer be visible in the management page',
      async () => {
        await waitFor(() => {
          expect(
            screen.queryByText('Epic Robot Showdown')
          ).not.toBeInTheDocument();
        });
      }
    );

    and('the total battle count should be reduced by one', async () => {
      await waitFor(() => {
        // Since we started with 1 battle and deleted it, we should see the empty state
        expect(screen.getByText('No battles found')).toBeInTheDocument();
      });
    });
  });
});
