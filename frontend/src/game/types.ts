export interface Robot {
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

export interface WallPosition {
  x: number;
  y: number;
}

export interface Wall {
  type: string;
  positions: WallPosition[];
}

export interface BattleState {
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

export interface LaserPosition {
  x: number;
  y: number;
}

export interface LaserEvent {
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
