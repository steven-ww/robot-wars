// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Mock PixiJS globally to avoid canvas/WebGL initialization issues in tests
jest.mock('pixi.js', () => {
  const mockContainer = () => ({
    addChild: jest.fn(),
    removeChildren: jest.fn(),
    destroy: jest.fn(),
    destroyed: false,
    scale: { set: jest.fn() },
    x: 0,
    y: 0,
    alpha: 1,
    children: [] as any[],
  });

  const mockGraphics = () => ({
    beginFill: jest.fn().mockReturnThis(),
    endFill: jest.fn().mockReturnThis(),
    drawCircle: jest.fn().mockReturnThis(),
    drawRect: jest.fn().mockReturnThis(),
    drawRoundedRect: jest.fn().mockReturnThis(),
    lineStyle: jest.fn().mockReturnThis(),
    moveTo: jest.fn().mockReturnThis(),
    lineTo: jest.fn().mockReturnThis(),
    clear: jest.fn().mockReturnThis(),
    destroy: jest.fn(),
    destroyed: false,
    x: 0,
    y: 0,
    alpha: 1,
    scale: { set: jest.fn() },
    children: [] as any[],
  });

  const mockText = () => ({
    anchor: { set: jest.fn() },
    destroy: jest.fn(),
    x: 0,
    y: 0,
    text: '',
    children: [] as any[],
  });

  return {
    Application: jest.fn().mockImplementation(() => ({
      stage: mockContainer(),
      view: { style: {} },
      screen: { width: 800, height: 600 },
      ticker: {
        add: jest.fn(),
        remove: jest.fn(),
      },
      renderer: {
        resize: jest.fn(),
        width: 800,
        height: 600,
        resolution: 1,
      },
      destroy: jest.fn(),
    })),
    Container: jest.fn().mockImplementation(mockContainer),
    Graphics: jest.fn().mockImplementation(mockGraphics),
    Text: jest.fn().mockImplementation(mockText),
  };
});

// Mock HTMLCanvasElement.getContext to avoid JSDOM canvas issues
Object.defineProperty(HTMLCanvasElement.prototype, 'getContext', {
  value: jest.fn(contextType => {
    if (contextType === 'webgl' || contextType === 'webgl2') {
      return {
        // Mock WebGL context methods
        clearColor: jest.fn(),
        clear: jest.fn(),
        viewport: jest.fn(),
        createShader: jest.fn(),
        shaderSource: jest.fn(),
        compileShader: jest.fn(),
        createProgram: jest.fn(),
        attachShader: jest.fn(),
        linkProgram: jest.fn(),
        useProgram: jest.fn(),
        createBuffer: jest.fn(),
        bindBuffer: jest.fn(),
        bufferData: jest.fn(),
        getAttribLocation: jest.fn(),
        enableVertexAttribArray: jest.fn(),
        vertexAttribPointer: jest.fn(),
        drawArrays: jest.fn(),
        getUniformLocation: jest.fn(),
        uniform1f: jest.fn(),
        uniform2f: jest.fn(),
        uniform3f: jest.fn(),
        uniform4f: jest.fn(),
        uniformMatrix4fv: jest.fn(),
        activeTexture: jest.fn(),
        bindTexture: jest.fn(),
        createTexture: jest.fn(),
        texImage2D: jest.fn(),
        texParameteri: jest.fn(),
        generateMipmap: jest.fn(),
        enable: jest.fn(),
        disable: jest.fn(),
        blendFunc: jest.fn(),
        depthFunc: jest.fn(),
        cullFace: jest.fn(),
        frontFace: jest.fn(),
      };
    }
    // Fall back to 2D context mock
    return {
      fillRect: jest.fn(),
      clearRect: jest.fn(),
      getImageData: jest.fn(() => ({
        data: new Array(4),
      })),
      putImageData: jest.fn(),
      createImageData: jest.fn(() => []),
      setTransform: jest.fn(),
      drawImage: jest.fn(),
      save: jest.fn(),
      fillText: jest.fn(),
      restore: jest.fn(),
      beginPath: jest.fn(),
      moveTo: jest.fn(),
      lineTo: jest.fn(),
      closePath: jest.fn(),
      stroke: jest.fn(),
      translate: jest.fn(),
      scale: jest.fn(),
      rotate: jest.fn(),
      arc: jest.fn(),
      fill: jest.fn(),
      measureText: jest.fn(() => ({ width: 0 })),
      transform: jest.fn(),
      rect: jest.fn(),
      clip: jest.fn(),
      fillStyle: '',
      strokeStyle: '',
      lineWidth: 1,
    };
  }),
});

// Mock HTMLCanvasElement properties
Object.defineProperty(HTMLCanvasElement.prototype, 'width', {
  value: 800,
  writable: true,
});

Object.defineProperty(HTMLCanvasElement.prototype, 'height', {
  value: 600,
  writable: true,
});

// Suppress console.error for specific React warnings during testing
const originalError = console.error;
beforeAll(() => {
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      (args[0].includes('Warning: An update to') ||
        args[0].includes('Warning: `ReactDOMTestUtils.act` is deprecated'))
    ) {
      return;
    }
    originalError.call(console, ...args);
  };
});

afterAll(() => {
  console.error = originalError;
});
