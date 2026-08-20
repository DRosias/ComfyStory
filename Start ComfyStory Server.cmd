@echo off
setlocal

cd /d "%~dp0"

if defined COMFYSTORY_JAVA_HOME set "COMFYSTORY_SERVER_JAVA=%COMFYSTORY_JAVA_HOME%"

if not defined COMFYSTORY_SERVER_JAVA (
    for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do (
        if exist "%%~fJ\bin\java.exe" set "COMFYSTORY_SERVER_JAVA=%%~fJ"
    )
)

if not defined COMFYSTORY_SERVER_JAVA (
    echo Java 21 was not found for the ComfyStory server.
    echo Set COMFYSTORY_JAVA_HOME to the Java 21 JDK directory and try again.
    pause
    exit /b 1
)

for /f "tokens=3" %%V in ('call "%COMFYSTORY_SERVER_JAVA%\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do set "COMFYSTORY_JAVA_VERSION=%%~V"
for /f "tokens=1 delims=." %%V in ("%COMFYSTORY_JAVA_VERSION%") do set "COMFYSTORY_JAVA_MAJOR=%%V"

if not "%COMFYSTORY_JAVA_MAJOR%"=="21" (
    echo ComfyStory requires Java 21, but %COMFYSTORY_SERVER_JAVA% contains Java %COMFYSTORY_JAVA_VERSION%.
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
echo Using Java %COMFYSTORY_JAVA_VERSION% from %COMFYSTORY_SERVER_JAVA%.
echo Press Ctrl+C to stop the server.
echo If asked to terminate the batch job, answer N and wait for "Shutdown complete".
echo.
"%COMFYSTORY_SERVER_JAVA%\bin\java.exe" -jar "%SERVER_JAR%"
set "SERVER_EXIT=%ERRORLEVEL%"

echo.
echo ComfyStory stopped with exit code %SERVER_EXIT%.
pause
exit /b %SERVER_EXIT%
