#!/usr/bin/env node

/**
 * Generate configuration file for frontend build
 * This script creates a config.js file that can be used by the frontend application
 * Configuration values are read from environment variables (set by GitHub Actions)
 */

const fs = require('fs');
const path = require('path');

// Default configuration values
const defaultConfig = {
  backendUrl: 'http://localhost:8080',
  environment: 'development'
};

// Read configuration from environment variables
const config = {
  backendUrl: process.env.REACT_APP_BACKEND_URL || defaultConfig.backendUrl,
  environment: process.env.NODE_ENV || defaultConfig.environment
};

// Generate the config.js file content
const configContent = `// Auto-generated configuration file
// This file is generated during the build process
window.AppConfig = ${JSON.stringify(config, null, 2)};
`;

// Ensure the public directory exists
const publicDir = path.join(__dirname, '../public');
if (!fs.existsSync(publicDir)) {
  fs.mkdirSync(publicDir, { recursive: true });
}

// Write the config file
const configPath = path.join(publicDir, 'config.js');
fs.writeFileSync(configPath, configContent);

console.log('Configuration file generated:');
console.log(`  File: ${configPath}`);
console.log(`  Backend URL: ${config.backendUrl}`);
console.log(`  Environment: ${config.environment}`);
