@echo off
SETLOCAL EnableDelayedExpansion

set MICROSERVICES=backend\api backend\realTimeChat backend\realTimeNotification

for %%S in (%MICROSERVICES%) do (
    echo ========================================================
    echo Initializing microservice: %%S
    echo ========================================================
    
    pushd %%S
    
    set VENV_DIR=
    if exist venv\Scripts\activate.bat set VENV_DIR=venv
    if exist env\Scripts\activate.bat set VENV_DIR=env
    
    if not defined VENV_DIR (
        echo Creating virtual environment...
        python -m venv venv
        set VENV_DIR=venv
    ) else (
        echo Virtual environment '!VENV_DIR!' already exists.
    )
    
    echo Activating virtual environment...
    call !VENV_DIR!\Scripts\activate.bat
    
    if exist requirements.txt (
        echo Installing requirements...
        pip install -r requirements.txt
    ) else (
        echo No requirements.txt found. Skipping pip install.
    )
    
    call !VENV_DIR!\Scripts\deactivate.bat
    
    popd
)

echo ========================================================
echo Starting all microservices...
echo ========================================================

start "API Microservice (Port 5000)" cmd /k "cd backend\api && if exist venv\Scripts\python.exe (venv\Scripts\python.exe main.py) else (env\Scripts\python.exe main.py)"

start "RealTime Chat Microservice (Port 8001)" cmd /k "cd backend\realTimeChat && if exist venv\Scripts\python.exe (venv\Scripts\python.exe app.py) else (env\Scripts\python.exe app.py)"

start "RealTime Notification Microservice (Port 8088)" cmd /k "cd backend\realTimeNotification && if exist venv\Scripts\python.exe (venv\Scripts\python.exe app.py) else (env\Scripts\python.exe app.py)"

echo All microservices have been started in separate windows.
pause
