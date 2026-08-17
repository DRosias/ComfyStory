@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-ComfyStoryLegacyAdmin.ps1" %*
set "COMFYSTORY_TOOL_EXIT=%ERRORLEVEL%"
echo.
if not "%COMFYSTORY_TOOL_EXIT%"=="0" echo The account cleanup did not make changes successfully.
pause
exit /b %COMFYSTORY_TOOL_EXIT%
