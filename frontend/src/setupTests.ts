// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';

// Mock Phaser globally to avoid canvas/WebGL initialization issues
jest.mock('phaser', () => {
  // Mock Scene class
  const MockScene = jest.fn().mockImplementation(() => ({
    add: {
      image: jest.fn(),
      sprite: jest.fn(),
      text: jest.fn(),
      graphics: jest.fn(),
      rectangle: jest.fn(),
      circle: jest.fn(),
      line: jest.fn(),
      container: jest.fn(),
    },
    physics: {
      add: {
        sprite: jest.fn(),
        image: jest.fn(),
      },
      world: {
        setBounds: jest.fn(),
      },
    },
    cameras: {
      main: {
        setBounds: jest.fn(),
        centerOn: jest.fn(),
        setZoom: jest.fn(),
      },
    },
    input: {
      on: jest.fn(),
      keyboard: {
        createCursorKeys: jest.fn(),
        addKey: jest.fn(),
      },
    },
    load: {
      image: jest.fn(),
      spritesheet: jest.fn(),
      json: jest.fn(),
      on: jest.fn(),
      start: jest.fn(),
    },
    create: jest.fn(),
    update: jest.fn(),
    preload: jest.fn(),
    init: jest.fn(),
    registry: {
      get: jest.fn(),
      set: jest.fn(),
    },
    data: {
      get: jest.fn(),
      set: jest.fn(),
    },
    time: {
      addEvent: jest.fn(),
      delayedCall: jest.fn(),
    },
    tweens: {
      add: jest.fn(),
    },
    sound: {
      add: jest.fn(),
    },
    sys: {
      game: {
        destroy: jest.fn(),
      },
    },
  }));

  // Mock Game class
  const MockGame = jest.fn().mockImplementation(() => ({
    scene: {
      add: jest.fn(),
      start: jest.fn(),
      stop: jest.fn(),
      get: jest.fn(),
      remove: jest.fn(),
      getScene: jest.fn().mockReturnValue({
        scene: {
          isActive: jest.fn().mockReturnValue(false),
        },
      }),
    },
    canvas: {
      style: {},
      parentNode: {
        removeChild: jest.fn(),
      },
    },
    destroy: jest.fn(),
    registry: {
      get: jest.fn(),
      set: jest.fn(),
    },
    events: {
      on: jest.fn(),
      off: jest.fn(),
      emit: jest.fn(),
    },
    scale: {
      resize: jest.fn(),
    },
    config: {},
  }));

  // Mock GameObjects
  const MockGameObjects = {
    Container: jest.fn(),
    Image: jest.fn(),
    Sprite: jest.fn(),
    Text: jest.fn(),
    Graphics: jest.fn(),
    Rectangle: jest.fn(),
  };

  // Mock input objects
  const MockInput = {
    Keyboard: {
      Key: jest.fn(),
    },
  };

  // Main Phaser mock object
  return {
    __esModule: true,
    default: {
      Game: MockGame,
      Scene: MockScene,
      GameObjects: MockGameObjects,
      Input: MockInput,
      AUTO: 'AUTO',
      WEBGL: 'WEBGL',
      CANVAS: 'CANVAS',
      Scale: {
        FIT: 'FIT',
        RESIZE: 'RESIZE',
      },
      Physics: {
        Arcade: {
          ArcadePhysics: jest.fn(),
        },
      },
      Cameras: {
        Scene2D: {
          Camera: jest.fn(),
        },
      },
      Loader: {
        LoaderPlugin: jest.fn(),
      },
      Sound: {
        SoundManagerCreator: jest.fn(),
      },
      Core: {
        Config: jest.fn(),
      },
      Device: {
        OS: {},
        Browser: {},
        Features: {},
        Input: {},
        Audio: {},
        Video: {},
      },
    },
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
