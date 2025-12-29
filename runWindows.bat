@echo off
setlocal enabledelayedexpansion
cd /d %~dp0

REM -----------------------
REM Check Java
REM -----------------------
java -version >nul 2>&1
if errorlevel 1 (
  echo Java is not installed or not in PATH.
  pause
  exit /b 1
)

REM -----------------------
REM Check Python launcher
REM -----------------------
where py >nul 2>&1
if errorlevel 1 (
  where python >nul 2>&1
  if errorlevel 1 (
    echo Python is not installed or not in PATH.
    echo Install Python 3.10+ or ensure 'py' or 'python' is available.
    pause
    exit /b 1
  ) else (
    set "PYTHON=python"
  )
) else (
  set "PYTHON=py -3"
)

REM -----------------------
REM Paths
REM -----------------------
set "LLM_DIR=%~dp0python-llm"
set "VENV_PY=%LLM_DIR%\.venv\Scripts\python.exe"
set "REQ=%LLM_DIR%\requirements.txt"
set "ENVFILE=%LLM_DIR%\.env"

REM -----------------------
REM Ensure venv exists
REM -----------------------
if not exist "%VENV_PY%" (
  echo Creating python venv...
  pushd "%LLM_DIR%"
  %PYTHON% -m venv .venv
  if errorlevel 1 (
    echo Failed to create venv.
    popd
    pause
    exit /b 1
  )
  popd
)

REM -----------------------
REM Install requirements
REM -----------------------
if exist "%REQ%" (
  echo Installing python requirements...
  "%VENV_PY%" -m pip install --upgrade pip >nul
  "%VENV_PY%" -m pip install -r "%REQ%"
  if errorlevel 1 (
    echo Failed to install requirements.
    pause
    exit /b 1
  )
) else (
  echo Missing requirements.txt at %REQ%
  pause
  exit /b 1
)

REM -----------------------
REM Check .env
REM -----------------------
if not exist "%ENVFILE%" (
  echo Missing python-llm\.env
  echo Copy python-llm\.env.example to python-llm\.env and set GEMINI_API_KEY
  pause
  exit /b 1
)

REM -----------------------
REM Start Python LLM service
REM -----------------------
echo Starting LLM service...
start "LLM Service" /D "%LLM_DIR%" "%VENV_PY%" run_llm.py

REM Optional wait
timeout /t 2 >nul

REM -----------------------
REM Start Java app
REM -----------------------
echo Starting Java app...
java -jar dist\ChatApp.jar

pause
