@echo off
setlocal

if /I "%~1"=="--elevated" goto launch

set "COMFYSTORY_LAUNCH_SCRIPT=%~f0"
powershell.exe -NoProfile -Command "Start-Process -FilePath $env:COMFYSTORY_LAUNCH_SCRIPT -ArgumentList '--elevated' -Verb RunAs"
exit /b %ERRORLEVEL%

:launch
reg.exe query "HKCU\Software\ComfyStory" /v "WindowedModeInitialized" >nul 2>&1
if errorlevel 1 call :initializeWindowedMode

start "" /D "%~dp0" "%~dp0ComfyStoryLauncher\nxsteam.exe" "%~dp0MapleStory.exe"
exit /b

:initializeWindowedMode
"%SystemRoot%\System32\reg.exe" add "HKLM\Software\Wizet\MapleStory" /v "soScreenMode" /t REG_DWORD /d 3 /f /reg:32 >nul 2>&1
set "WINDOWED_MODE_EXIT_CODE=%ERRORLEVEL%"

reg.exe add "HKCU\Software\ComfyStory" /v "WindowedModeInitialized" /t REG_DWORD /d 1 /f >nul 2>&1
set "WINDOWED_MODE_MARKER_EXIT_CODE=%ERRORLEVEL%"

if not "%WINDOWED_MODE_EXIT_CODE%"=="0" call :logWindowedModeFailure "Windowed-mode registry update failed with exit code %WINDOWED_MODE_EXIT_CODE%. Continuing launch."
if not "%WINDOWED_MODE_MARKER_EXIT_CODE%"=="0" call :logWindowedModeFailure "Windowed-mode initialization marker could not be saved (exit code %WINDOWED_MODE_MARKER_EXIT_CODE%). Continuing launch."
exit /b

:logWindowedModeFailure
if not defined LOCALAPPDATA exit /b
set "WINDOWED_MODE_LOG_DIR=%LOCALAPPDATA%\ComfyStory"
if not exist "%WINDOWED_MODE_LOG_DIR%" mkdir "%WINDOWED_MODE_LOG_DIR%" >nul 2>&1
>>"%WINDOWED_MODE_LOG_DIR%\launcher.log" echo [%date% %time%] %~1 2>nul
exit /b
