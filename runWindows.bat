@echo off
cd /d %~dp0
java -version >nul 2>&1
if errorlevel 1 (
  echo Java is not installed or not in PATH.
  pause
  exit /b 1
)
java -jar dist\ChatApp.jar
pause
