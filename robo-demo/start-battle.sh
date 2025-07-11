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

echo "Starting backend service in dev mode..."
# Check if backend service is already running
if lsof -Pi :8080 -sTCP:LISTEN -t > /dev/null ; then
  echo "Backend service is already running."
else
  echo "Starting backend service in dev mode..."
  # Start the backend service in dev mode in the background
  cd backend
  ../gradlew quarkusDev &
  BACKEND_PID=$!
  cd ..

  # Wait for the backend service to start
  echo "Waiting for backend service to start..."
  sleep 10
fi

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
