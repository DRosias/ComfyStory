@echo off
setlocal

cd /d "%~dp0"

where java.exe >nul 2>&1
if errorlevel 1 (
    echo Java was not found on PATH. Java 21 is required.
    pause
    exit /b 1
)

if not defined COMFYSTORY_DB_PASSWORD (
    echo COMFYSTORY_DB_PASSWORD is not set in this environment.
    echo Set the database password before starting the server.
    pause
    exit /b 1
)

set "SERVER_JAR=bin\maplestory-1.77.3-shaded.jar"
if not exist "%SERVER_JAR%" (
    echo The server JAR was not found: %SERVER_JAR%
    echo Build the project with Maven before starting the server.
    pause
    exit /b 1
)

echo Starting ComfyStory from %CD%...
echo Press Ctrl+C to stop the server.
echo.
java.exe -jar "%SERVER_JAR%"
set "SERVER_EXIT=%ERRORLEVEL%"

echo.
echo ComfyStory stopped with exit code %SERVER_EXIT%.
pause
exit /b %SERVER_EXIT%
