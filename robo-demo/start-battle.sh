#!/bin/bash

# Script to start the backend service locally and run a robot battle

# Exit on error
set -e

# Function to clean up on exit
cleanup() {
  echo "Cleaning up..."
  if [ -n "$BACKEND_PID" ]; then
    echo "Stopping backend service (PID: $BACKEND_PID)..."
    kill $BACKEND_PID || true
  fi
  echo "Cleanup complete."
}

# Register the cleanup function to be called on exit
trap cleanup EXIT

# Function to check if server is running and accessible
check_server_accessibility() {
  local url="$1"
  echo "Checking if server is accessible at: $url"
  
  # Try to connect to the server with a timeout
  if curl -s --connect-timeout 5 --max-time 10 "$url/q/health" > /dev/null 2>&1; then
    echo "✓ Server is accessible and running at $url"
    return 0
  else
    echo "✗ Server is not accessible at $url"
    return 1
  fi
}

# Function to check if URL is localhost
is_localhost_url() {
  local url="$1"
  if [[ "$url" == *"localhost"* ]] || [[ "$url" == *"127.0.0.1"* ]] || [[ "$url" == *"::1"* ]]; then
    return 0
  else
    return 1
  fi
}

# Function to extract port from URL
extract_port_from_url() {
  local url="$1"
  # Extract port using regex - handles http://localhost:8080 format
  if [[ "$url" =~ :([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
  else
    # Default HTTP port
    echo "80"
  fi
}

# Function to start local server if needed
start_local_server_if_needed() {
  local url="$1"
  
  # Only start local server if URL is localhost and server is not running
  if is_localhost_url "$url"; then
    local port=$(extract_port_from_url "$url")
    echo "URL points to localhost (port: $port)"
    
    # Check if port is already in use
    if lsof -Pi :$port -sTCP:LISTEN -t > /dev/null 2>&1; then
      echo "Port $port is already in use"
      if check_server_accessibility "$url"; then
        echo "Server is already running and accessible"
        return 0
      else
        echo "Port is in use but server is not accessible. This might be a different service."
        return 1
      fi
    else
      echo "Port $port is not in use. Starting local server..."
      cd backend
      ../gradlew quarkusDev &
      BACKEND_PID=$!
      cd ..
      
      echo "Waiting for backend service to start..."
      # Wait up to 30 seconds for server to start
      for i in {1..30}; do
        if check_server_accessibility "$url"; then
          echo "✓ Local server started successfully"
          return 0
        fi
        echo "Waiting for server to start... ($i/30)"
        sleep 1
      done
      
      echo "✗ Failed to start local server within 30 seconds"
      return 1
    fi
  else
    echo "URL is not localhost. Will not start local server."
    return 0
  fi
}

# Set default values
BASE_URL="http://localhost:8080"
TIME_LIMIT="5m" # 5 minutes by default

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -u|--url)
      BASE_URL="$2"
      shift # past argument
      shift # past value
      ;;
    -t|--time)
      TIME_LIMIT="$2"
      shift # past argument
      shift # past value
      ;;
    -h|--help)
      echo "Usage: $0 [OPTIONS]"
      echo "Options:"
      echo "  -u, --url URL           Base URL for the Robot Wars API (default: http://localhost:8080)"
      echo "  -t, --time TIME         Time limit for the battle (e.g., 5m, 30s) (default: 5m)"
      echo "  -h, --help              Show this help message"
      echo ""
      echo "The demo will run until one robot wins or the time limit is reached."
      echo "Note: Local server will only be started if URL points to localhost and server is not running."
      exit 0
      ;;
    -*|--*)
      echo "Unknown option $1"
      echo "Use -h or --help for usage information"
      exit 1
      ;;
    *)
      shift # past argument
      ;;
  esac
done

# Set the working directory to the project root
cd "$(dirname "$0")/.."

echo "=== Robot Wars Demo Server Setup ==="
echo "Target URL: $BASE_URL"
echo "Time limit: $TIME_LIMIT"
echo ""

# First, check if the server is already accessible
if check_server_accessibility "$BASE_URL"; then
  echo "Server is already running and accessible. No need to start local server."
else
  echo "Server is not accessible. Checking if we should start a local server..."
  
  if ! start_local_server_if_needed "$BASE_URL"; then
    echo "Failed to start local server or server is not accessible."
    echo "Please ensure:"
    echo "1. The server is running at $BASE_URL"
    echo "2. The server is accessible from this machine"
    echo "3. If using a remote server, check network connectivity"
    exit 1
  fi
fi

# Final validation that server is accessible
if ! check_server_accessibility "$BASE_URL"; then
  echo "❌ Server is still not accessible at $BASE_URL"
  echo "Cannot proceed with demo. Please check server status."
  exit 1
fi

echo "✅ Server validation complete. Server is accessible at $BASE_URL"

# Build the arguments for the robo-demo application
ROBO_ARGS="--url $BASE_URL --time $TIME_LIMIT"

# Run the robo-demo application
echo "Running robo-demo application..."
echo "  Base URL: $BASE_URL"
echo "  Time limit: $TIME_LIMIT"
echo "  Battle will run until one robot wins or time limit is reached"
cd robo-demo
../gradlew run --args="$ROBO_ARGS"

echo "Battle complete!"
