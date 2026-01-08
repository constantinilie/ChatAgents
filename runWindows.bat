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
REM Check and Set .env (API KEY)
REM -----------------------
if not exist "%ENVFILE%" (
    echo.
    echo [CONFIGURARE] Fisierul .env lipseste.
    set /p API_KEY="Introduceti cheia GEMINI_API_KEY: "
    
    if "!API_KEY!"=="" (
        echo Eroare: Nu ati introdus nicio cheia.
        pause
        exit /b 1
    )

    echo GEMINI_API_KEY=!API_KEY! > "%ENVFILE%"
    echo Fisierul .env a fost creat cu succes la %ENVFILE%.
    echo.
) else (
    echo [INFO] Fisierul .env exista deja. 
    set /p RASPUNS="Doriti sa schimbati cheia API actuala(GEMINI)? (y/n): "
    if /I "!RASPUNS!"=="y" (
        set /p API_KEY="Introduceti noua cheia GEMINI_API_KEY: "
        echo GEMINI_API_KEY=!API_KEY! > "%ENVFILE%"
        echo Cheia a fost actualizata.
    )
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
