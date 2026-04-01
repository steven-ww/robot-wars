import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import ArenaComponent from '../ArenaComponent';

describe('ArenaComponent WebSocket error handling', () => {
  let origWebSocket: any;

  beforeEach(() => {
    jest.useFakeTimers();
    origWebSocket = (global as any).WebSocket;
  });

  afterEach(() => {
    jest.useRealTimers();
    (global as any).WebSocket = origWebSocket;
    jest.clearAllMocks();
  });

  test('logs errors on WebSocket onerror and on JSON parse failure; logs on close', () => {
    const wsInstances: any[] = [];
    (global as any).WebSocket = jest.fn(() => {
      const ws: any = {
        onopen: null,
        onmessage: null,
        onerror: null,
        onclose: null,
        readyState: 1,
        close: jest.fn(),
      };
      wsInstances.push(ws);
      return ws;
    });

    const consoleErrorSpy = jest
      .spyOn(console, 'error')
      .mockImplementation(() => {});
    const consoleLogSpy = jest
      .spyOn(console, 'log')
      .mockImplementation(() => {});

    render(<ArenaComponent battleId="ws-error-test" />);

    expect(wsInstances.length).toBe(1);
    const ws = wsInstances[0];

    // Simulate onerror
    if (ws.onerror) ws.onerror({ type: 'error' });
    // Simulate onmessage with invalid JSON
    if (ws.onmessage) ws.onmessage({ data: '{invalid' });
    // Simulate onclose
    if (ws.onclose) ws.onclose({ code: 1006, reason: 'abnormal' });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      expect.stringContaining('WebSocket error'),
      expect.anything()
    );
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      expect.stringContaining('Error parsing WebSocket message'),
      expect.anything()
    );
    expect(consoleLogSpy).toHaveBeenCalledWith(
      expect.stringContaining('Disconnected from battle state WebSocket')
    );
  });
});
