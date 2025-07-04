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

# Set the working directory to the project root
cd "$(dirname "$0")/.."

echo "Starting backend service in dev mode..."
# Start the backend service in dev mode in the background
cd backend
./gradlew quarkusDev &
BACKEND_PID=$!
cd ..

# Wait for the backend service to start
echo "Waiting for backend service to start..."
sleep 10

# Run the robo-demo application
echo "Running robo-demo application..."
cd robo-demo
../gradlew run

echo "Battle complete!"