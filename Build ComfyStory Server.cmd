@echo off
setlocal

cd /d "%~dp0"

if defined COMFYSTORY_JAVA_HOME set "COMFYSTORY_BUILD_JAVA=%COMFYSTORY_JAVA_HOME%"

if not defined COMFYSTORY_BUILD_JAVA (
    for /d %%J in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do (
        if exist "%%~fJ\bin\java.exe" set "COMFYSTORY_BUILD_JAVA=%%~fJ"
    )
)

if not defined COMFYSTORY_BUILD_JAVA (
    echo Java 21 was not found for the ComfyStory build.
    echo Set COMFYSTORY_JAVA_HOME to the Java 21 JDK directory and try again.
    pause
    exit /b 1
)

if not exist "%COMFYSTORY_BUILD_JAVA%\bin\javac.exe" (
    echo The selected ComfyStory Java directory is not a JDK: %COMFYSTORY_BUILD_JAVA%
    pause
    exit /b 1
)

for /f "tokens=3" %%V in ('call "%COMFYSTORY_BUILD_JAVA%\bin\java.exe" -version 2^>^&1 ^| findstr /i "version"') do set "COMFYSTORY_JAVA_VERSION=%%~V"
for /f "tokens=1 delims=." %%V in ("%COMFYSTORY_JAVA_VERSION%") do set "COMFYSTORY_JAVA_MAJOR=%%V"

if not "%COMFYSTORY_JAVA_MAJOR%"=="21" (
    echo ComfyStory requires Java 21, but %COMFYSTORY_BUILD_JAVA% contains Java %COMFYSTORY_JAVA_VERSION%.
    pause
    exit /b 1
)

where mvn.cmd >nul 2>&1
if errorlevel 1 (
    echo Maven was not found on PATH.
    pause
    exit /b 1
)

set "JAVA_HOME=%COMFYSTORY_BUILD_JAVA%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Building ComfyStory with Java %COMFYSTORY_JAVA_VERSION%...
call mvn.cmd clean package -DskipTests
set "BUILD_EXIT=%ERRORLEVEL%"

echo.
if "%BUILD_EXIT%"=="0" (
    echo ComfyStory build completed successfully.
) else (
    echo ComfyStory build failed with exit code %BUILD_EXIT%.
)
pause
exit /b %BUILD_EXIT%
