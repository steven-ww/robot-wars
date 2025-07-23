import React, { useEffect, useRef, useCallback } from 'react';
import Phaser from 'phaser';
import { getBackendUrl, getWebSocketUrl } from '../utils/apiConfig';

// Interfaces for robot data
interface Robot {
  id: string;
  name: string;
  positionX: number;
  positionY: number;
  direction: string;
  hitPoints: number;
  status: string;
  targetBlocks: number;
  blocksRemaining: number;
}

interface WallPosition {
  x: number;
  y: number;
}

interface Wall {
  type: string;
  positions: WallPosition[];
}

interface BattleState {
  battleId: string;
  battleName: string;
  arenaWidth: number;
  arenaHeight: number;
  robotMovementTimeSeconds: number;
  battleState: string;
  robots: Robot[];
  walls: Wall[];
  winnerId?: string;
  winnerName?: string;
}

interface LaserPosition {
  x: number;
  y: number;
}

interface LaserEvent {
  hit: boolean;
  hitRobotId?: string;
  hitRobotName?: string;
  damageDealt?: number;
  range: number;
  direction: string;
  laserPath: LaserPosition[];
  hitPosition?: LaserPosition;
  blockedBy?: string;
  firingRobotId?: string;
}

interface PhaserArenaComponentProps {
  battleId: string;
}

// Phaser scene class
class BattleArenaScene extends Phaser.Scene {
  private robots: Map<string, Phaser.GameObjects.Container> = new Map();
  private walls: Phaser.GameObjects.Group | null = null;
  private arenaWidth: number = 20;
  private arenaHeight: number = 20;
  private cellSize: number = 25;
  private webSocket: WebSocket | null = null;
  private battleId: string = '';
  private activeLasers: Map<string, Phaser.GameObjects.GameObject[]> =
    new Map();
  private robotPositions: Map<string, { x: number; y: number }> = new Map();

  constructor() {
    super({ key: 'BattleArenaScene' });
  }

  init(data: { battleId: string }) {
    this.battleId = data.battleId;
  }

  preload() {
    // Create simple colored textures for robots and walls
    this.add
      .graphics()
      .fillStyle(0x28a745)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('robot-idle', this.cellSize - 2, this.cellSize - 2);

    this.add
      .graphics()
      .fillStyle(0x007bff)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('robot-moving', this.cellSize - 2, this.cellSize - 2);

    this.add
      .graphics()
      .fillStyle(0xdc3545)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('robot-crashed', this.cellSize - 2, this.cellSize - 2);

    this.add
      .graphics()
      .fillStyle(0x6c757d)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('robot-destroyed', this.cellSize - 2, this.cellSize - 2);

    this.add
      .graphics()
      .fillStyle(0xffd700)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('robot-winner', this.cellSize - 2, this.cellSize - 2);

    this.add
      .graphics()
      .fillStyle(0x8b4513)
      .fillRect(0, 0, this.cellSize - 2, this.cellSize - 2)
      .generateTexture('wall', this.cellSize - 2, this.cellSize - 2);

    // Create laser spark texture
    this.add
      .graphics()
      .fillStyle(0xff6600)
      .fillCircle(2, 2, 2)
      .generateTexture('spark', 4, 4);
  }

  create() {
    // Create arena background
    const graphics = this.add.graphics();
    graphics.fillStyle(0xf0f0f0);
    graphics.fillRect(
      0,
      0,
      this.arenaWidth * this.cellSize,
      this.arenaHeight * this.cellSize
    );

    // Create arena border
    graphics.lineStyle(2, 0x333333);
    graphics.strokeRect(
      0,
      0,
      this.arenaWidth * this.cellSize,
      this.arenaHeight * this.cellSize
    );

    // Initialize walls group
    this.walls = this.add.group();

    // Connect to WebSocket
    this.connectWebSocket();
  }

  private connectWebSocket() {
    try {
      const backendUrl = getBackendUrl();
      const wsUrl = getWebSocketUrl(
        backendUrl,
        `/battle-state/${this.battleId}`
      );

      console.log(`Connecting to WebSocket: ${wsUrl}`);
      this.webSocket = new WebSocket(wsUrl);

      this.webSocket.onopen = () => {
        console.log('Connected to battle state WebSocket');
        console.log('WebSocket readyState:', this.webSocket?.readyState);
      };

      this.webSocket.onmessage = event => {
        try {
          const data = JSON.parse(event.data);
          console.log('Received WebSocket message:', event.data);
          console.log('Parsed battle state:', data);

          if (data.error) {
            console.error('WebSocket error:', data.error);
            return;
          }

          if (data.laserPath) {
            this.handleLaserEvent(data);
          } else {
            this.updateBattleState(data);
          }
        } catch (err) {
          console.error('Error parsing WebSocket message:', err);
        }
      };

      this.webSocket.onerror = event => {
        console.error('WebSocket error:', event);
        console.error('WebSocket readyState:', this.webSocket?.readyState);
      };

      this.webSocket.onclose = event => {
        console.log('Disconnected from battle state WebSocket');
        console.log('Close event:', event.code, event.reason);
      };
    } catch (err) {
      console.error('Error connecting to WebSocket:', err);
    }
  }

  private updateBattleState(battleState: BattleState) {
    // Update arena dimensions if they changed
    if (
      battleState.arenaWidth !== this.arenaWidth ||
      battleState.arenaHeight !== this.arenaHeight
    ) {
      this.arenaWidth = battleState.arenaWidth;
      this.arenaHeight = battleState.arenaHeight;
      this.redrawArena();
    }

    // Update walls
    this.updateWalls(battleState.walls);

    // Update robots
    this.updateRobots(battleState.robots, battleState.winnerId);
  }

  private redrawArena() {
    // Clear and redraw arena background
    this.children.removeAll();

    const graphics = this.add.graphics();
    graphics.fillStyle(0xf0f0f0);
    graphics.fillRect(
      0,
      0,
      this.arenaWidth * this.cellSize,
      this.arenaHeight * this.cellSize
    );

    graphics.lineStyle(2, 0x333333);
    graphics.strokeRect(
      0,
      0,
      this.arenaWidth * this.cellSize,
      this.arenaHeight * this.cellSize
    );

    this.walls = this.add.group();
  }

  private updateWalls(walls: Wall[]) {
    if (!this.walls) return;

    // Clear existing walls
    this.walls.clear(true, true);

    // Add new walls
    walls.forEach(wall => {
      wall.positions.forEach(position => {
        const wallSprite = this.add.image(
          position.x * this.cellSize + this.cellSize / 2,
          (this.arenaHeight - position.y - 1) * this.cellSize +
            this.cellSize / 2,
          'wall'
        );
        this.walls!.add(wallSprite);
      });
    });
  }

  private clearActiveLasersForRobot(robotId: string) {
    const lasers = this.activeLasers.get(robotId);
    if (lasers) {
      lasers.forEach(laser => {
        if (laser && laser.active) {
          laser.destroy();
        }
      });
      this.activeLasers.delete(robotId);
    }
  }

  private updateRobots(robots: Robot[], winnerId?: string) {
    // Remove robots that no longer exist
    const currentRobotIds = new Set(robots.map(r => r.id));
    this.robots.forEach((container, id) => {
      if (!currentRobotIds.has(id)) {
        // Clear any active lasers for this robot
        this.clearActiveLasersForRobot(id);
        container.destroy();
        this.robots.delete(id);
      }
    });

    // Update or create robots
    robots.forEach(robot => {
      const isWinner = winnerId === robot.id;
      let container = this.robots.get(robot.id);

      // Check if robot moved and clear its lasers if so
      const lastPosition = this.robotPositions.get(robot.id);
      const currentPosition = { x: robot.positionX, y: robot.positionY };

      if (
        lastPosition &&
        (lastPosition.x !== currentPosition.x ||
          lastPosition.y !== currentPosition.y)
      ) {
        // Robot has moved, clear any active lasers from this robot
        this.clearActiveLasersForRobot(robot.id);
      }

      // Update stored position
      this.robotPositions.set(robot.id, currentPosition);

      if (!container) {
        // Create new robot container
        container = this.add.container(0, 0);
        this.robots.set(robot.id, container);
      }

      // Clear container
      container.removeAll(true);

      // Determine robot texture based on status and winner
      let texture = 'robot-idle';
      if (isWinner) {
        texture = 'robot-winner';
      } else {
        switch (robot.status.toLowerCase()) {
          case 'moving':
            texture = 'robot-moving';
            break;
          case 'crashed':
            texture = 'robot-crashed';
            break;
          case 'destroyed':
            texture = 'robot-destroyed';
            break;
          default:
            texture = 'robot-idle';
        }
      }

      // Create robot sprite
      const robotSprite = this.add.image(0, 0, texture);
      container.add(robotSprite);

      // Add robot name text
      const nameText = this.add
        .text(0, -15, robot.name, {
          fontSize: '10px',
          color: '#ffffff',
          stroke: '#000000',
          strokeThickness: 1,
        })
        .setOrigin(0.5);
      container.add(nameText);

      // Add status indicator
      const statusText = this.add
        .text(0, 15, robot.status, {
          fontSize: '8px',
          color: '#ffffff',
          stroke: '#000000',
          strokeThickness: 1,
        })
        .setOrigin(0.5);
      container.add(statusText);

      // Add winner crown
      if (isWinner) {
        const crown = this.add
          .text(0, -25, '👑', {
            fontSize: '16px',
          })
          .setOrigin(0.5);
        container.add(crown);

        // Add glow effect for winner
        this.tweens.add({
          targets: robotSprite,
          alpha: { from: 1, to: 0.8 },
          duration: 1000,
          yoyo: true,
          repeat: -1,
        });
      }

      // Position the container
      const x = robot.positionX * this.cellSize + this.cellSize / 2;
      const y =
        (this.arenaHeight - robot.positionY - 1) * this.cellSize +
        this.cellSize / 2;

      // Animate movement if robot is moving
      if (robot.status.toLowerCase() === 'moving') {
        this.tweens.add({
          targets: container,
          x: x,
          y: y,
          duration: 500,
          ease: 'Power2',
        });
      } else {
        container.setPosition(x, y);
      }
    });
  }

  private handleLaserEvent(laserData: LaserEvent) {
    if (!laserData.laserPath || laserData.laserPath.length < 2) return;

    const startPosition = laserData.laserPath[0];
    const endPosition = laserData.laserPath[laserData.laserPath.length - 1];

    // Convert to screen coordinates
    const startX = startPosition.x * this.cellSize + this.cellSize / 2;
    const startY =
      (this.arenaHeight - startPosition.y - 1) * this.cellSize +
      this.cellSize / 2;
    const endX = endPosition.x * this.cellSize + this.cellSize / 2;
    const endY =
      (this.arenaHeight - endPosition.y - 1) * this.cellSize +
      this.cellSize / 2;

    // Create laser beam graphics
    const laser = this.add.graphics();
    laser.lineStyle(3, laserData.hit ? 0xff0000 : 0x00ff00);
    laser.lineBetween(startX, startY, endX, endY);

    // Add laser glow effect
    const glowLaser = this.add.graphics();
    glowLaser.lineStyle(6, laserData.hit ? 0xff0000 : 0x00ff00, 0.3);
    glowLaser.lineBetween(startX, startY, endX, endY);

    // Track active lasers if we have the firing robot ID
    const firingRobotId = laserData.firingRobotId || 'unknown';
    if (!this.activeLasers.has(firingRobotId)) {
      this.activeLasers.set(firingRobotId, []);
    }
    this.activeLasers.get(firingRobotId)!.push(laser, glowLaser);

    // Add hit effect if laser hit something
    if (laserData.hit && laserData.hitPosition) {
      const hitX = laserData.hitPosition.x * this.cellSize + this.cellSize / 2;
      const hitY =
        (this.arenaHeight - laserData.hitPosition.y - 1) * this.cellSize +
        this.cellSize / 2;

      // Create explosion particles
      const particles = this.add.particles(hitX, hitY, 'spark', {
        scale: { start: 0.5, end: 0 },
        alpha: { start: 1, end: 0 },
        speed: { min: 50, max: 100 },
        lifespan: 200,
        quantity: 8,
      });

      // Stop particles after animation
      this.time.delayedCall(200, () => {
        particles.destroy();
      });
    }

    // Remove laser after shorter duration (500ms total)
    this.tweens.add({
      targets: [laser, glowLaser],
      alpha: 0,
      duration: 200,
      delay: 300,
      onComplete: () => {
        laser.destroy();
        glowLaser.destroy();

        // Remove from active lasers tracking
        const activeLasers = this.activeLasers.get(firingRobotId);
        if (activeLasers) {
          const laserIndex = activeLasers.indexOf(laser);
          const glowIndex = activeLasers.indexOf(glowLaser);
          if (laserIndex > -1) activeLasers.splice(laserIndex, 1);
          if (glowIndex > -1) activeLasers.splice(glowIndex, 1);

          if (activeLasers.length === 0) {
            this.activeLasers.delete(firingRobotId);
          }
        }
      },
    });
  }

  destroy() {
    if (this.webSocket) {
      this.webSocket.close();
      this.webSocket = null;
    }
    // Clean up scene resources
    this.robots.clear();
    if (this.walls) {
      this.walls.destroy();
    }
  }
}

const PhaserArenaComponent: React.FC<PhaserArenaComponentProps> = ({
  battleId,
}) => {
  const gameRef = useRef<HTMLDivElement>(null);
  const phaserGameRef = useRef<Phaser.Game | null>(null);

  const initializePhaser = useCallback(() => {
    if (!gameRef.current || phaserGameRef.current) return;

    try {
      const config: Phaser.Types.Core.GameConfig = {
        type: Phaser.AUTO,
        width: 800,
        height: 600,
        parent: gameRef.current,
        backgroundColor: '#f8f9fa',
        scale: {
          mode: Phaser.Scale.FIT,
          autoCenter: Phaser.Scale.CENTER_BOTH,
          width: 800,
          height: 600,
        },
        scene: BattleArenaScene,
      };

      phaserGameRef.current = new Phaser.Game(config);

      // Use a timeout to ensure the game is ready before starting the scene
      setTimeout(() => {
        if (phaserGameRef.current?.scene?.start) {
          phaserGameRef.current.scene.start('BattleArenaScene', { battleId });
        }
      }, 100);
    } catch (error) {
      console.error('Failed to initialize Phaser:', error);
    }
  }, [battleId]);

  useEffect(() => {
    initializePhaser();

    return () => {
      if (
        phaserGameRef.current &&
        typeof phaserGameRef.current.destroy === 'function'
      ) {
        phaserGameRef.current.destroy(true);
        phaserGameRef.current = null;
      }
    };
  }, [initializePhaser]);

  return (
    <div style={{ padding: '10px' }}>
      <h2>Battle Arena</h2>
      <div
        ref={gameRef}
        style={{
          width: '100%',
          height: '600px',
          border: '1px solid #333',
          borderRadius: '8px',
          overflow: 'hidden',
        }}
        data-testid="phaser-arena-container"
      />
    </div>
  );
};

export default PhaserArenaComponent;
