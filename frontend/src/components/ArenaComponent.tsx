import React, { useEffect, useRef, useCallback } from 'react';
import { ArenaRenderer } from '../game/ArenaRenderer';
import { BattleState, LaserEvent } from '../game/types';
import { getBackendUrl, getWebSocketUrl } from '../utils/apiConfig';

interface ArenaComponentProps {
  battleId: string;
}

const ArenaComponent: React.FC<ArenaComponentProps> = ({ battleId }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const rendererRef = useRef<ArenaRenderer | null>(null);
  const wsRef = useRef<WebSocket | null>(null);

  const connectWebSocket = useCallback(
    (renderer: ArenaRenderer) => {
      try {
        const backendUrl = getBackendUrl();
        const wsUrl = getWebSocketUrl(
          backendUrl,
          `/battle-state/${battleId}`
        );

        console.log(`Connecting to WebSocket: ${wsUrl}`);
        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
          console.log('Connected to battle state WebSocket');
          console.log('WebSocket readyState:', ws.readyState);
        };

        ws.onmessage = event => {
          try {
            const data = JSON.parse(event.data);
            console.log('Received WebSocket message:', event.data);

            if (data.error) {
              console.error('WebSocket error:', data.error);
              return;
            }

            if (data.laserPath) {
              renderer.handleLaserEvent(data as LaserEvent);
            } else {
              renderer.updateBattleState(data as BattleState);
            }
          } catch (err) {
            console.error('Error parsing WebSocket message:', err);
          }
        };

        ws.onerror = event => {
          console.error('WebSocket error:', event);
          console.error('WebSocket readyState:', ws.readyState);
        };

        ws.onclose = event => {
          console.log('Disconnected from battle state WebSocket');
          console.log('Close event:', event.code, event.reason);
        };

        return ws;
      } catch (err) {
        console.error('Error connecting to WebSocket:', err);
        return null;
      }
    },
    [battleId]
  );

  useEffect(() => {
    if (!containerRef.current) return;

    try {
      const renderer = new ArenaRenderer(containerRef.current);
      rendererRef.current = renderer;

      const ws = connectWebSocket(renderer);
      wsRef.current = ws;

      return () => {
        if (ws) ws.close();
        renderer.destroy();
        rendererRef.current = null;
        wsRef.current = null;
      };
    } catch (error) {
      console.error('Failed to initialize arena renderer:', error);
    }
  }, [connectWebSocket]);

  return (
    <div style={{ padding: '10px' }}>
      <h2>Battle Arena</h2>
      <div
        ref={containerRef}
        style={{
          width: '100%',
          height: '600px',
          border: '1px solid #333',
          borderRadius: '8px',
          overflow: 'hidden',
          backgroundColor: '#0f0f1a',
        }}
        data-testid="arena-container"
      />
    </div>
  );
};

export default ArenaComponent;
