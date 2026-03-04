import * as PIXI from 'pixi.js';
import { Robot, Wall, BattleState, LaserEvent } from './types';

// ── Visual constants ────────────────────────────────────────────────────────

const COLORS = {
  background: 0x0f0f1a,
  gridLine: 0x1e1e38,
  gridMajor: 0x2a2a50,
  wall: 0x4a3520,
  wallHighlight: 0x5a4530,
  wallShadow: 0x3a2510,
  healthBarBg: 0x1a1a2e,
  text: 0xffffff,
  textShadow: 0x000000,
  laserHit: 0xff4444,
  laserMiss: 0x44ff88,
  statusIdle: 0x00e676,
  statusMoving: 0x2979ff,
  statusCrashed: 0xff1744,
  statusDestroyed: 0x555577,
  statusWinner: 0xffd600,
};

const ROBOT_PALETTE = [
  0x00e676, 0x2979ff, 0xff6d00, 0xe040fb, 0x00e5ff, 0xffd600, 0xff1744,
  0x76ff03, 0x448aff, 0xff9100,
];

const DIRECTION_ANGLES: Record<string, number> = {
  EAST: 0,
  E: 0,
  SE: Math.PI / 4,
  SOUTH: Math.PI / 2,
  S: Math.PI / 2,
  SW: (3 * Math.PI) / 4,
  WEST: Math.PI,
  W: Math.PI,
  NW: -(3 * Math.PI) / 4,
  NORTH: -Math.PI / 2,
  N: -Math.PI / 2,
  NE: -Math.PI / 4,
};

// ── Helper utilities ────────────────────────────────────────────────────────

function easeOutCubic(t: number): number {
  return 1 - Math.pow(1 - t, 3);
}

function healthToColor(pct: number): number {
  if (pct > 0.5) {
    const t = (pct - 0.5) * 2;
    const r = Math.round(0xff * (1 - t));
    const g = Math.round(0xe6 * t + 0xd6 * (1 - t));
    const b = Math.round(0x76 * t);
    return (r << 16) | (g << 8) | b;
  }
  const t = pct * 2;
  const r = 0xff;
  const g = Math.round(0xd6 * t + 0x17 * (1 - t));
  const b = Math.round(0x44 * (1 - t));
  return (r << 16) | (g << 8) | b;
}

// ── Robot sprite data ───────────────────────────────────────────────────────

interface RobotSpriteData {
  container: PIXI.Container;
  body: PIXI.Graphics;
  glow: PIXI.Graphics;
  dirArrow: PIXI.Graphics;
  nameText: PIXI.Text;
  healthBarBg: PIXI.Graphics;
  healthBar: PIXI.Graphics;
  statusText: PIXI.Text;
  targetX: number;
  targetY: number;
  startX: number;
  startY: number;
  animStartTime: number;
  animDuration: number;
  animating: boolean;
  color: number;
}

// ── Main renderer ───────────────────────────────────────────────────────────

export class ArenaRenderer {
  private app: PIXI.Application | null = null;
  private worldContainer!: PIXI.Container;
  private gridLayer!: PIXI.Container;
  private wallLayer!: PIXI.Container;
  private robotLayer!: PIXI.Container;
  private effectLayer!: PIXI.Container;

  private robotSprites: Map<string, RobotSpriteData> = new Map();
  private robotColorMap: Map<string, number> = new Map();
  private colorIndex: number = 0;

  private arenaWidth: number = 20;
  private arenaHeight: number = 20;
  private cellSize: number = 32;

  private robotPositions: Map<string, { x: number; y: number }> = new Map();
  private activeLasers: Map<string, PIXI.Container[]> = new Map();

  constructor(parentElement: HTMLElement) {
    try {
      const rect = parentElement.getBoundingClientRect();
      const width = rect.width || 800;
      const height = rect.height || 600;

      this.app = new PIXI.Application({
        width,
        height,
        backgroundColor: COLORS.background,
        antialias: true,
        resolution: window?.devicePixelRatio || 1,
        autoDensity: true,
      });

      const canvas = this.app.view as HTMLCanvasElement;
      if (canvas && canvas.style) {
        canvas.style.display = 'block';
      }
      if (canvas instanceof Node) {
        parentElement.appendChild(canvas);
      }

      this.worldContainer = new PIXI.Container();
      this.gridLayer = new PIXI.Container();
      this.wallLayer = new PIXI.Container();
      this.robotLayer = new PIXI.Container();
      this.effectLayer = new PIXI.Container();

      this.worldContainer.addChild(this.gridLayer as any);
      this.worldContainer.addChild(this.wallLayer as any);
      this.worldContainer.addChild(this.robotLayer as any);
      this.worldContainer.addChild(this.effectLayer as any);
      this.app.stage.addChild(this.worldContainer as any);

      this.app.ticker.add(this.animate, this);
      this.drawGrid();
      this.fitToScreen();
    } catch (error) {
      console.error('Failed to create PixiJS application:', error);
    }
  }

  // ── Public API ──────────────────────────────────────────────────────────

  destroy(): void {
    if (this.app) {
      try {
        this.app.ticker.remove(this.animate, this);
      } catch (_) {
        /* ignore cleanup errors */
      }
      this.robotSprites.clear();
      this.robotColorMap.clear();
      this.robotPositions.clear();
      this.activeLasers.clear();
      try {
        this.app.destroy(true, { children: true, texture: true });
      } catch (_) {
        /* ignore cleanup errors */
      }
      this.app = null;
    }
  }

  resize(width: number, height: number): void {
    if (!this.app) return;
    this.app.renderer.resize(width, height);
    this.fitToScreen();
  }

  updateBattleState(state: BattleState): void {
    if (!this.app) return;

    if (
      state.arenaWidth !== this.arenaWidth ||
      state.arenaHeight !== this.arenaHeight
    ) {
      this.arenaWidth = state.arenaWidth;
      this.arenaHeight = state.arenaHeight;
      this.drawGrid();
      this.fitToScreen();
    }

    this.clearLasersForMovedRobots(state.robots);
    this.updateWalls(state.walls);
    this.updateRobots(state.robots, state.winnerId);
  }

  handleLaserEvent(laser: LaserEvent): void {
    if (!this.app) return;
    if (!laser.laserPath || laser.laserPath.length < 2) return;

    const startPos = laser.laserPath[0];
    const endPos = laser.laserPath[laser.laserPath.length - 1];
    const startX = this.gameToScreenX(startPos.x);
    const startY = this.gameToScreenY(startPos.y);
    const endX = this.gameToScreenX(endPos.x);
    const endY = this.gameToScreenY(endPos.y);
    const color = laser.hit ? COLORS.laserHit : COLORS.laserMiss;

    const laserContainer = new PIXI.Container();

    // Outer glow
    const glowGfx = new PIXI.Graphics();
    glowGfx.lineStyle(8, color, 0.2);
    glowGfx.moveTo(startX, startY);
    glowGfx.lineTo(endX, endY);
    laserContainer.addChild(glowGfx as any);

    // Main beam
    const beamGfx = new PIXI.Graphics();
    beamGfx.lineStyle(2, color, 0.9);
    beamGfx.moveTo(startX, startY);
    beamGfx.lineTo(endX, endY);
    laserContainer.addChild(beamGfx as any);

    // Bright core
    const coreGfx = new PIXI.Graphics();
    coreGfx.lineStyle(1, 0xffffff, 0.6);
    coreGfx.moveTo(startX, startY);
    coreGfx.lineTo(endX, endY);
    laserContainer.addChild(coreGfx as any);

    this.effectLayer.addChild(laserContainer as any);

    // Track laser for cleanup on robot move
    const firingRobotId = laser.firingRobotId || 'unknown';
    if (!this.activeLasers.has(firingRobotId)) {
      this.activeLasers.set(firingRobotId, []);
    }
    this.activeLasers.get(firingRobotId)!.push(laserContainer);

    // Hit particle burst
    if (laser.hit && laser.hitPosition) {
      this.createHitParticles(
        this.gameToScreenX(laser.hitPosition.x),
        this.gameToScreenY(laser.hitPosition.y),
        color
      );
    }

    // Fade-out animation
    const startTime = Date.now();
    const fadeMs = 150;
    const fadeCallback = () => {
      const elapsed = Date.now() - startTime;
      if (elapsed > fadeMs) {
        if (!laserContainer.destroyed) laserContainer.destroy();
        this.app!.ticker.remove(fadeCallback);
        this.removeLaserFromTracking(firingRobotId, laserContainer);
      } else {
        laserContainer.alpha = 1 - elapsed / fadeMs;
      }
    };
    this.app.ticker.add(fadeCallback);
  }

  // ── Animation loop ──────────────────────────────────────────────────────

  private animate = (): void => {
    const now = Date.now();
    this.robotSprites.forEach(sprite => {
      if (!sprite.animating) return;
      const elapsed = now - sprite.animStartTime;
      const progress = Math.min(elapsed / sprite.animDuration, 1);
      const eased = easeOutCubic(progress);

      sprite.container.x =
        sprite.startX + (sprite.targetX - sprite.startX) * eased;
      sprite.container.y =
        sprite.startY + (sprite.targetY - sprite.startY) * eased;

      if (progress >= 1) {
        sprite.animating = false;
        sprite.container.x = sprite.targetX;
        sprite.container.y = sprite.targetY;
      }
    });
  };

  // ── Coordinate helpers ──────────────────────────────────────────────────

  private gameToScreenX(gx: number): number {
    return gx * this.cellSize + this.cellSize / 2;
  }

  private gameToScreenY(gy: number): number {
    return (this.arenaHeight - gy - 1) * this.cellSize + this.cellSize / 2;
  }

  // ── Camera fitting ──────────────────────────────────────────────────────

  private fitToScreen(): void {
    if (!this.app) return;
    const worldW = this.arenaWidth * this.cellSize;
    const worldH = this.arenaHeight * this.cellSize;
    const screenW = this.app.screen.width;
    const screenH = this.app.screen.height;
    const padding = 30;
    const scale = Math.min(
      (screenW - padding * 2) / worldW,
      (screenH - padding * 2) / worldH,
      1
    );
    this.worldContainer.scale.set(scale);
    this.worldContainer.x = (screenW - worldW * scale) / 2;
    this.worldContainer.y = (screenH - worldH * scale) / 2;
  }

  // ── Grid drawing ────────────────────────────────────────────────────────

  private drawGrid(): void {
    if (!this.app) return;
    this.gridLayer.removeChildren();
    const gfx = new PIXI.Graphics();
    const w = this.arenaWidth * this.cellSize;
    const h = this.arenaHeight * this.cellSize;

    gfx.beginFill(COLORS.background);
    gfx.drawRect(0, 0, w, h);
    gfx.endFill();

    // Minor lines
    for (let x = 0; x <= this.arenaWidth; x++) {
      const major = x % 5 === 0;
      gfx.lineStyle(major ? 1 : 0.5, COLORS.gridLine, major ? 0.4 : 0.15);
      gfx.moveTo(x * this.cellSize, 0);
      gfx.lineTo(x * this.cellSize, h);
    }
    for (let y = 0; y <= this.arenaHeight; y++) {
      const major = y % 5 === 0;
      gfx.lineStyle(major ? 1 : 0.5, COLORS.gridLine, major ? 0.4 : 0.15);
      gfx.moveTo(0, y * this.cellSize);
      gfx.lineTo(w, y * this.cellSize);
    }

    // Border
    gfx.lineStyle(2, COLORS.gridMajor, 0.8);
    gfx.drawRect(0, 0, w, h);

    this.gridLayer.addChild(gfx as any);
  }

  // ── Wall rendering ──────────────────────────────────────────────────────

  private updateWalls(walls: Wall[]): void {
    this.wallLayer.removeChildren();
    const gfx = new PIXI.Graphics();
    const s = this.cellSize;
    const pad = 1;

    walls.forEach(wall => {
      wall.positions.forEach(pos => {
        const x = pos.x * s;
        const y = (this.arenaHeight - pos.y - 1) * s;

        // Shadow
        gfx.beginFill(COLORS.wallShadow);
        gfx.drawRect(x + pad + 2, y + pad + 2, s - pad * 2, s - pad * 2);
        gfx.endFill();
        // Body
        gfx.beginFill(COLORS.wall);
        gfx.drawRect(x + pad, y + pad, s - pad * 2, s - pad * 2);
        gfx.endFill();
        // Highlight edge
        gfx.lineStyle(1, COLORS.wallHighlight, 0.6);
        gfx.moveTo(x + pad, y + s - pad);
        gfx.lineTo(x + pad, y + pad);
        gfx.lineTo(x + s - pad, y + pad);
      });
    });

    this.wallLayer.addChild(gfx as any);
  }

  // ── Robot rendering ─────────────────────────────────────────────────────

  private getRobotColor(robotId: string): number {
    if (!this.robotColorMap.has(robotId)) {
      this.robotColorMap.set(
        robotId,
        ROBOT_PALETTE[this.colorIndex % ROBOT_PALETTE.length]
      );
      this.colorIndex++;
    }
    return this.robotColorMap.get(robotId)!;
  }

  private createRobotSprite(robotId: string): RobotSpriteData {
    const container = new PIXI.Container();
    const color = this.getRobotColor(robotId);
    const radius = this.cellSize * 0.4;

    // Glow ring
    const glow = new PIXI.Graphics();
    glow.beginFill(color, 0.15);
    glow.drawCircle(0, 0, radius + 6);
    glow.endFill();
    container.addChild(glow as any);

    // Body
    const body = new PIXI.Graphics();
    body.beginFill(color);
    body.drawCircle(0, 0, radius);
    body.endFill();
    body.lineStyle(2, 0xffffff, 0.3);
    body.drawCircle(0, 0, radius);
    container.addChild(body as any);

    // Direction arrow
    const dirArrow = new PIXI.Graphics();
    container.addChild(dirArrow as any);

    // Name label
    const nameText = new PIXI.Text('', {
      fontFamily: 'Arial, Helvetica, sans-serif',
      fontSize: 10,
      fill: COLORS.text,
      stroke: COLORS.textShadow,
      strokeThickness: 3,
      align: 'center',
    } as PIXI.TextStyle);
    nameText.anchor.set(0.5, 1);
    nameText.y = -(radius + 10);
    container.addChild(nameText as any);

    // Health bar bg
    const barWidth = this.cellSize * 0.75;
    const barHeight = 3;
    const barY = radius + 8;
    const healthBarBg = new PIXI.Graphics();
    healthBarBg.beginFill(COLORS.healthBarBg);
    healthBarBg.drawRoundedRect(-barWidth / 2, barY, barWidth, barHeight, 1);
    healthBarBg.endFill();
    container.addChild(healthBarBg as any);

    const healthBar = new PIXI.Graphics();
    container.addChild(healthBar as any);

    // Status text
    const statusText = new PIXI.Text('', {
      fontFamily: 'Arial, Helvetica, sans-serif',
      fontSize: 8,
      fill: 0xaaaacc,
      stroke: COLORS.textShadow,
      strokeThickness: 2,
      align: 'center',
    } as PIXI.TextStyle);
    statusText.anchor.set(0.5, 0);
    statusText.y = barY + barHeight + 3;
    container.addChild(statusText as any);

    this.robotLayer.addChild(container as any);

    return {
      container,
      body,
      glow,
      dirArrow,
      nameText,
      healthBarBg,
      healthBar,
      statusText,
      targetX: 0,
      targetY: 0,
      startX: 0,
      startY: 0,
      animStartTime: 0,
      animDuration: 500,
      animating: false,
      color,
    };
  }

  private updateRobotVisual(
    sprite: RobotSpriteData,
    robot: Robot,
    isWinner: boolean
  ): void {
    const radius = this.cellSize * 0.4;
    const color = sprite.color;
    const status = robot.status.toLowerCase();

    // Name
    sprite.nameText.text = robot.name;

    // Body colour based on status
    sprite.body.clear();
    if (status === 'destroyed') {
      sprite.body.beginFill(COLORS.statusDestroyed, 0.5);
    } else if (status === 'crashed') {
      sprite.body.beginFill(COLORS.statusCrashed, 0.7);
    } else {
      sprite.body.beginFill(color);
    }
    sprite.body.drawCircle(0, 0, radius);
    sprite.body.endFill();
    sprite.body.lineStyle(2, 0xffffff, 0.3);
    sprite.body.drawCircle(0, 0, radius);

    // Glow ring
    sprite.glow.clear();
    let glowColor = color;
    let glowAlpha = 0.15;
    let glowRadius = radius + 6;

    if (isWinner) {
      glowColor = COLORS.statusWinner;
      glowAlpha = 0.4;
      glowRadius = radius + 10;
    } else if (status === 'moving') {
      glowColor = COLORS.statusMoving;
      glowAlpha = 0.3;
    } else if (status === 'crashed') {
      glowColor = COLORS.statusCrashed;
      glowAlpha = 0.3;
    } else if (status === 'destroyed') {
      glowAlpha = 0;
    }

    if (glowAlpha > 0) {
      sprite.glow.beginFill(glowColor, glowAlpha);
      sprite.glow.drawCircle(0, 0, glowRadius);
      sprite.glow.endFill();
    }

    // Direction arrow
    sprite.dirArrow.clear();
    const angle = DIRECTION_ANGLES[robot.direction.toUpperCase()] ?? 0;
    const arrowLen = radius + 4;
    const arrowWidth = 4;
    sprite.dirArrow.beginFill(0xffffff, 0.8);
    sprite.dirArrow.moveTo(
      Math.cos(angle) * arrowLen,
      Math.sin(angle) * arrowLen
    );
    sprite.dirArrow.lineTo(
      Math.cos(angle + 2.6) * arrowWidth + Math.cos(angle) * (arrowLen - 6),
      Math.sin(angle + 2.6) * arrowWidth + Math.sin(angle) * (arrowLen - 6)
    );
    sprite.dirArrow.lineTo(
      Math.cos(angle - 2.6) * arrowWidth + Math.cos(angle) * (arrowLen - 6),
      Math.sin(angle - 2.6) * arrowWidth + Math.sin(angle) * (arrowLen - 6)
    );
    sprite.dirArrow.endFill();

    // Health bar
    const barWidth = this.cellSize * 0.75;
    const barHeight = 3;
    const barY = radius + 8;
    const hp = Math.max(0, Math.min(100, robot.hitPoints));
    const pct = hp / 100;

    sprite.healthBar.clear();
    if (pct > 0) {
      sprite.healthBar.beginFill(healthToColor(pct));
      sprite.healthBar.drawRoundedRect(
        -barWidth / 2,
        barY,
        barWidth * pct,
        barHeight,
        1
      );
      sprite.healthBar.endFill();
    }

    // Status text
    sprite.statusText.text = robot.status;
    sprite.statusText.y = barY + barHeight + 3;

    // Winner crown
    if (isWinner) {
      const hasCrown = sprite.container.children.some((c: any) => c._isCrown);
      if (!hasCrown) {
        const crown = new PIXI.Text('\uD83D\uDC51', {
          fontSize: 16,
        } as PIXI.TextStyle);
        crown.anchor.set(0.5, 1);
        crown.y = -(radius + 24);
        (crown as any)._isCrown = true;
        sprite.container.addChild(crown as any);
      }
    }
  }

  private updateRobots(robots: Robot[], winnerId?: string): void {
    const currentIds = new Set(robots.map(r => r.id));

    // Remove departed robots
    const toRemove: string[] = [];
    this.robotSprites.forEach((sprite, id) => {
      if (!currentIds.has(id)) {
        this.clearActiveLasersForRobot(id);
        sprite.container.destroy();
        toRemove.push(id);
      }
    });
    toRemove.forEach(id => {
      this.robotSprites.delete(id);
      this.robotPositions.delete(id);
    });

    robots.forEach(robot => {
      let sprite = this.robotSprites.get(robot.id);

      if (!sprite) {
        sprite = this.createRobotSprite(robot.id);
        this.robotSprites.set(robot.id, sprite);
        const sx = this.gameToScreenX(robot.positionX);
        const sy = this.gameToScreenY(robot.positionY);
        sprite.container.x = sx;
        sprite.container.y = sy;
        sprite.targetX = sx;
        sprite.targetY = sy;
      }

      // Guard out-of-range
      if (
        robot.positionX < 0 ||
        robot.positionY < 0 ||
        robot.positionX >= this.arenaWidth ||
        robot.positionY >= this.arenaHeight
      ) {
        return;
      }

      this.updateRobotVisual(sprite, robot, winnerId === robot.id);

      const newX = this.gameToScreenX(robot.positionX);
      const newY = this.gameToScreenY(robot.positionY);
      const lastPos = this.robotPositions.get(robot.id);
      const moved =
        lastPos &&
        (lastPos.x !== robot.positionX || lastPos.y !== robot.positionY);

      if (moved && robot.status.toLowerCase() === 'moving') {
        sprite.startX = sprite.container.x;
        sprite.startY = sprite.container.y;
        sprite.targetX = newX;
        sprite.targetY = newY;
        sprite.animStartTime = Date.now();
        sprite.animDuration = 500;
        sprite.animating = true;
      } else if (moved || !lastPos) {
        sprite.container.x = newX;
        sprite.container.y = newY;
        sprite.targetX = newX;
        sprite.targetY = newY;
        sprite.animating = false;
      }

      this.robotPositions.set(robot.id, {
        x: robot.positionX,
        y: robot.positionY,
      });
    });
  }

  // ── Laser cleanup helpers ───────────────────────────────────────────────

  private clearLasersForMovedRobots(robots: Robot[]): void {
    robots.forEach(robot => {
      const lastPos = this.robotPositions.get(robot.id);
      if (
        lastPos &&
        (lastPos.x !== robot.positionX || lastPos.y !== robot.positionY)
      ) {
        this.clearActiveLasersForRobot(robot.id);
      }
    });
  }

  private clearActiveLasersForRobot(robotId: string): void {
    const lasers = this.activeLasers.get(robotId);
    if (lasers) {
      lasers.forEach(l => {
        if (!l.destroyed) l.destroy();
      });
      this.activeLasers.delete(robotId);
    }
  }

  private removeLaserFromTracking(
    robotId: string,
    container: PIXI.Container
  ): void {
    const lasers = this.activeLasers.get(robotId);
    if (lasers) {
      const idx = lasers.indexOf(container);
      if (idx > -1) lasers.splice(idx, 1);
      if (lasers.length === 0) this.activeLasers.delete(robotId);
    }
  }

  // ── Hit particles ───────────────────────────────────────────────────────

  private createHitParticles(x: number, y: number, color: number): void {
    const count = 12;
    for (let i = 0; i < count; i++) {
      const particle = new PIXI.Graphics();
      particle.beginFill(color);
      particle.drawCircle(0, 0, 1.5 + Math.random() * 1.5);
      particle.endFill();
      particle.x = x;
      particle.y = y;
      this.effectLayer.addChild(particle as any);

      const angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.5;
      const speed = 40 + Math.random() * 60;
      const vx = Math.cos(angle) * speed;
      const vy = Math.sin(angle) * speed;
      const lifetime = 300 + Math.random() * 200;
      const startTime = Date.now();

      const animCb = () => {
        const elapsed = Date.now() - startTime;
        const t = elapsed / lifetime;
        if (t >= 1 || particle.destroyed) {
          if (!particle.destroyed) particle.destroy();
          this.app!.ticker.remove(animCb);
          return;
        }
        particle.x = x + vx * (elapsed / 1000);
        particle.y = y + vy * (elapsed / 1000);
        particle.alpha = 1 - t;
        particle.scale.set(1 - t * 0.5);
      };
      this.app!.ticker.add(animCb);
    }
  }
}
