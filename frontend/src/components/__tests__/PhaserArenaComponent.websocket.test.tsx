import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import PhaserArenaComponent from '../PhaserArenaComponent';

// Mock Phaser with minimal functionality to allow the Scene to run
jest.mock('phaser', () => {
  const makeGraphics = () => {
    const g: any = {};
    g.fillStyle = jest.fn().mockReturnValue(g);
    g.fillRect = jest.fn().mockReturnValue(g);
    g.generateTexture = jest.fn().mockReturnValue(g);
    g.lineStyle = jest.fn().mockReturnValue(g);
    g.strokeRect = jest.fn().mockReturnValue(g);
    g.lineBetween = jest.fn().mockReturnValue(g);
    g.destroy = jest.fn();
    return g;
  };

  class Scene {
    public add: any;
    public cameras: any;
    public scale: any;
    public tweens: any;
    public time: any;
    public children: any;

    constructor() {
      this.add = {
        graphics: jest.fn(() => makeGraphics()),
        image: jest.fn(() => ({ setOrigin: jest.fn().mockReturnThis(), destroy: jest.fn(), active: true })),
        text: jest.fn(() => ({ setOrigin: jest.fn().mockReturnThis() })),
        group: jest.fn(() => ({ add: jest.fn(), clear: jest.fn(), destroy: jest.fn() })),
        particles: jest.fn(() => ({ destroy: jest.fn() })),
      };
      this.cameras = { main: { setBounds: jest.fn(), setZoom: jest.fn(), centerOn: jest.fn() } };
      this.scale = { width: 800, height: 600 };
      this.tweens = { add: jest.fn() };
      this.time = { delayedCall: jest.fn((ms: number, cb: () => void) => cb()) };
      // eslint-disable-next-line testing-library/no-node-access
      this.children = { removeAll: jest.fn() };
    }
  }

  class Game {
    public config: any;
    public scene: any;

    constructor(config: any) {
      this.config = config;
      this.scene = {
        start: (_key: string, data: any) => {
          const SceneClass = config.scene;
          const sceneInstance = new SceneClass();
          if (sceneInstance.init) sceneInstance.init(data);
          if (sceneInstance.create) sceneInstance.create();
        },
      };
    }

    destroy() {}
  }

  return {
    AUTO: 'AUTO',
    Scale: { FIT: 'FIT', CENTER_BOTH: 'CENTER_BOTH' },
    Scene,
    Game,
  };
});


describe('PhaserArenaComponent WebSocket error handling', () => {
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
      const ws: any = { onopen: null, onmessage: null, onerror: null, onclose: null, readyState: 1, close: jest.fn() };
      wsInstances.push(ws);
      return ws;
    });

    const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
    const consoleLogSpy = jest.spyOn(console, 'log').mockImplementation(() => {});

    render(<PhaserArenaComponent battleId="ws-error-test" />);

    // Advance timers to trigger scene.start in component
    jest.advanceTimersByTime(150);

    expect(wsInstances.length).toBe(1);
    const ws = wsInstances[0];
    // Simulate onerror
    if (ws.onerror) ws.onerror({ type: 'error' });
    // Simulate onmessage with invalid JSON
    if (ws.onmessage) ws.onmessage({ data: '{invalid' });
    // Simulate onclose
    if (ws.onclose) ws.onclose({ code: 1006, reason: 'abnormal' });

    expect(consoleErrorSpy).toHaveBeenCalledWith(expect.stringContaining('WebSocket error'), expect.anything());
    expect(consoleErrorSpy).toHaveBeenCalledWith(expect.stringContaining('Error parsing WebSocket message'), expect.anything());
    expect(consoleLogSpy).toHaveBeenCalledWith(expect.stringContaining('Disconnected from battle state WebSocket'));
  });
});
