import { defineFeature, loadFeature } from 'jest-cucumber';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import ArenaComponent from '../../components/ArenaComponent';

// Mock WebSocket
class MockWebSocket {
  onopen: ((this: WebSocket, ev: Event) => any) | null = null;
  onmessage: ((this: WebSocket, ev: MessageEvent) => any) | null = null;
  onerror: ((this: WebSocket, ev: Event) => any) | null = null;
  onclose: ((this: WebSocket, ev: CloseEvent) => any) | null = null;
  url: string;
  
  constructor(url: string) {
    this.url = url;
  }
  
  send(data: string): void {
    // Mock implementation
  }
  
  close(): void {
    // Mock implementation
  }
  
  // Helper method to simulate receiving a message
  mockReceiveMessage(data: any): void {
    if (this.onmessage) {
      this.onmessage({ data: JSON.stringify(data) } as unknown as MessageEvent);
    }
  }
  
  // Helper method to simulate connection open
  mockOpen(): void {
    if (this.onopen) {
      this.onopen({} as Event);
    }
  }
  
  // Helper method to simulate error
  mockError(): void {
    if (this.onerror) {
      this.onerror({} as Event);
    }
  }
}

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

// Mock the WebSocket constructor
global.WebSocket = MockWebSocket as any;

defineFeature(feature, (test) => {
  beforeEach(() => {
    // Reset mocks between tests
    jest.clearAllMocks();
  });
  
  test('Render the initial arena state', ({ given, when, then, and }) => {
    let mockWs: MockWebSocket;
    
    given('the battle state websocket is available', () => {
      // This is handled by the mock WebSocket
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
    
    and('I connect to the battle state websocket', () => {
      // Get the mock WebSocket instance
      mockWs = (global.WebSocket as any).mock.instances[0];
      
      // Simulate connection open
      mockWs.mockOpen();
      
      // Simulate receiving battle state
      mockWs.mockReceiveMessage(mockBattleState);
    });
    
    then('I should see the arena with dimensions 20x20', async () => {
      await waitFor(() => {
        expect(screen.getByTestId('arena-grid')).toBeInTheDocument();
        expect(screen.getByTestId('arena-grid')).toHaveAttribute('data-width', '20');
        expect(screen.getByTestId('arena-grid')).toHaveAttribute('data-height', '20');
      });
    });
    
    and('I should see 2 robots on the arena', async () => {
      await waitFor(() => {
        const robots = screen.getAllByTestId(/robot-/);
        expect(robots).toHaveLength(2);
      });
    });
    
    and('each robot should be displayed at its correct position', async () => {
      await waitFor(() => {
        const robot1 = screen.getByTestId('robot-robot-1');
        const robot2 = screen.getByTestId('robot-robot-2');
        
        expect(robot1).toHaveAttribute('data-x', '5');
        expect(robot1).toHaveAttribute('data-y', '5');
        
        expect(robot2).toHaveAttribute('data-x', '15');
        expect(robot2).toHaveAttribute('data-y', '15');
      });
    });
  });
  
  test('Update the arena when robot positions change', ({ given, when, then }) => {
    let mockWs: MockWebSocket;
    
    given('I am viewing the arena', async () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));
      
      // Get the mock WebSocket instance
      mockWs = (global.WebSocket as any).mock.instances[0];
      
      // Simulate connection open
      mockWs.mockOpen();
      
      // Simulate receiving battle state
      mockWs.mockReceiveMessage(mockBattleState);
      
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
      
      // Simulate receiving updated battle state
      mockWs.mockReceiveMessage(updatedBattleState);
    });
    
    then('the robot\'s position on the arena should be updated', async () => {
      await waitFor(() => {
        const robot1 = screen.getByTestId('robot-robot-1');
        
        expect(robot1).toHaveAttribute('data-x', '6');
        expect(robot1).toHaveAttribute('data-y', '6');
      });
    });
  });
  
  test('Display robot status', ({ given, when, then }) => {
    let mockWs: MockWebSocket;
    
    given('I am viewing the arena', async () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));
      
      // Get the mock WebSocket instance
      mockWs = (global.WebSocket as any).mock.instances[0];
      
      // Simulate connection open
      mockWs.mockOpen();
      
      // Simulate receiving battle state
      mockWs.mockReceiveMessage(mockBattleState);
      
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
      
      // Simulate receiving updated battle state
      mockWs.mockReceiveMessage(updatedBattleState);
    });
    
    then('the robot should be displayed with a "MOVING" indicator', async () => {
      await waitFor(() => {
        const robot1 = screen.getByTestId('robot-robot-1');
        
        expect(robot1).toHaveAttribute('data-status', 'MOVING');
        expect(screen.getByText('MOVING')).toBeInTheDocument();
      });
    });
  });
  
  test('Handle connection errors', ({ when, then, and }) => {
    let mockWs: MockWebSocket;
    
    when('the websocket connection fails', () => {
      render(React.createElement(ArenaComponent, { battleId: "test-battle-id" }));
      
      // Get the mock WebSocket instance
      mockWs = (global.WebSocket as any).mock.instances[0];
      
      // Simulate error
      mockWs.mockError();
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