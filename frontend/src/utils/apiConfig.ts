// Declare global configuration type
declare global {
  interface Window {
    AppConfig?: {
      backendUrl: string;
      environment: string;
    };
  }
}

// Function to get the backend URL
export const getBackendUrl = (): string => {
  // Check if there's a global configuration (set during build)
  if (window.AppConfig?.backendUrl) {
    return window.AppConfig.backendUrl;
  }

  // Fallback to development default
  return 'http://localhost:8080';
};

// Function to convert backend URL to WebSocket URL
export const getWebSocketUrl = (backendUrl: string, path: string): string => {
  const url = new URL(backendUrl);

  // Convert HTTP protocol to WebSocket protocol
  const wsProtocol = url.protocol === 'https:' ? 'wss:' : 'ws:';

  // Build WebSocket URL
  return `${wsProtocol}//${url.host}${path}`;
};

// Function to build API URL with backend URL
export const buildApiUrl = (endpoint: string): string => {
  const backendUrl = getBackendUrl();
  return `${backendUrl}${endpoint}`;
};
