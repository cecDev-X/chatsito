#!/bin/bash

MICROSERVICES=("backend/api" "backend/realTimeChat" "backend/realTimeNotification")

for SERVICE in "${MICROSERVICES[@]}"; do
    echo "========================================================"
    echo "Initializing microservice: $SERVICE"
    echo "========================================================"
    
    pushd "$SERVICE" > /dev/null
    
    VENV_DIR=""
    if [ -d "venv" ]; then
        VENV_DIR="venv"
    elif [ -d "env" ]; then
        VENV_DIR="env"
    fi
    
    if [ -z "$VENV_DIR" ]; then
        echo "Creating virtual environment..."
        python3 -m venv venv
        VENV_DIR="venv"
    else
        echo "Virtual environment '$VENV_DIR' already exists."
    fi
    
    echo "Activating virtual environment..."
    source "$VENV_DIR/bin/activate"
    
    if [ -f "requirements.txt" ]; then
        echo "Installing requirements..."
        pip install -r requirements.txt
    else
        echo "No requirements.txt found. Skipping pip install."
    fi
    
    deactivate
    
    popd > /dev/null
done

echo "========================================================"
echo "Starting all microservices..."
echo "========================================================"

# Function to cleanup background processes on exit
cleanup() {
    echo ""
    echo "Stopping all microservices..."
    kill $(jobs -p) 2>/dev/null
    exit
}

# Trap CTRL+C and termination signals to stop child processes
trap cleanup SIGINT SIGTERM

run_service() {
    cd "$1"
    if [ -d "venv" ]; then
        source venv/bin/activate
    elif [ -d "env" ]; then
        source env/bin/activate
    fi
    python3 "$2"
}

echo "Starting API Microservice on port 5000..."
run_service "backend/api" "main.py" &

echo "Starting RealTime Chat Microservice on port 8001..."
run_service "backend/realTimeChat" "app.py" &

echo "Starting RealTime Notification Microservice on port 8088..."
run_service "backend/realTimeNotification" "app.py" &

echo "All microservices are running in the background. Press [CTRL+C] to stop them all."
wait
