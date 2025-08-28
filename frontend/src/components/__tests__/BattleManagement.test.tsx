import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import BattleManagement from '../../components/BattleManagement';

// Mock global fetch
const originalFetch = global.fetch;

describe('BattleManagement - test mode enhancements', () => {
  beforeEach(() => {
    global.fetch = jest.fn() as any;
    (global.fetch as jest.Mock).mockReset();
  });

  afterAll(() => {
    global.fetch = originalFetch as any;
  });

  const renderComponent = async (battles: any[] = []) => {
    // Initial GET /api/battles
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => battles,
    } as Response);

    render(<BattleManagement />);
  };

  test('renders Test badge for testMode battles', async () => {
    const battles = [
      {
        id: 'b1',
        name: 'Dev Test Battle',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 0.5,
        state: 'READY',
        robotCount: 1,
        robots: [{ id: 'r1', name: 'Solo', status: 'IDLE' }],
        testMode: true,
      },
    ];

    await renderComponent(battles);

    await waitFor(() => {
      expect(screen.getByText('Dev Test Battle')).toBeInTheDocument();
    });

    expect(screen.getByTestId('badge-test-b1')).toHaveTextContent('Test');
  });

  test('shows Start Battle button for READY battles and calls start API', async () => {
    const battles = [
      {
        id: 'b2',
        name: 'Startable',
        arenaWidth: 20,
        arenaHeight: 20,
        robotMovementTimeSeconds: 1,
        state: 'READY',
        robotCount: 1,
        robots: [{ id: 'r1', name: 'Solo', status: 'IDLE' }],
      },
    ];

    await renderComponent(battles);

    // Mock POST /api/battles/{id}/start
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ ...battles[0], state: 'IN_PROGRESS' }),
    } as Response);
    // Mock refresh GET /api/battles
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => [{ ...battles[0], state: 'IN_PROGRESS' }],
    } as Response);

    const startBtn = await screen.findByTestId('start-battle-b2');
    fireEvent.click(startBtn);

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/battles/b2/start'),
        expect.objectContaining({ method: 'POST' })
      );
    });
  });

  test('can create a test battle via the Create Test Battle form', async () => {
    await renderComponent([]);

    const openBtn = await screen.findByTestId('create-test-battle-button');

    fireEvent.click(openBtn);

    const nameInput = screen.getByPlaceholderText('Enter test battle name');

    // Mock POST /api/battles/test
    const created = {
      id: 'b3',
      name: 'My Dev Battle',
      arenaWidth: 40,
      arenaHeight: 30,
      robotMovementTimeSeconds: 0.5,
      state: 'WAITING_ON_ROBOTS',
      robotCount: 0,
      robots: [],
      testMode: true,
    };
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => created,
    } as Response);

    fireEvent.change(nameInput, { target: { value: 'My Dev Battle' } });
    fireEvent.click(screen.getByTestId('submit-create-test-battle'));

    // New battle should be visible in list after creation (success message appears and list includes name)
    await waitFor(() => {
      expect(
        screen.getByText('Test Battle "My Dev Battle" created successfully!')
      ).toBeInTheDocument();
    });
  });
});
