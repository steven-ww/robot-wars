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
        expect(screen.getByText('No battles available.')).toBeInTheDocument();
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
});
