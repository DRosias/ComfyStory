# ComfyStory nxsteam launcher

This directory contains the tracked source overlay for the customized v232.2 Electron launcher. The working client and generated ASAR remain outside Git.

## Apply to the working client

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\client-mods\nxsteam\Apply-NxSteamMod.ps1
```

The script creates a side-by-side `client/wz/ComfyStoryLauncher` from the pristine connector, extracts its `app.asar` into a temporary directory, applies `overlay/`, repacks the result, mirrors the readable sources, and creates `client/Launch ComfyStory.cmd`.

The original `client/wz/nxsteam` remains unchanged. The side-by-side layout also avoids the administrator-only ACL inherited by the original launcher files.

Starting `client/Launch ComfyStory.cmd` prompts for elevation because the original `MapleStory.exe` manifest requires administrator access. Accept the UAC prompt so the Electron launcher can start the game. Distribute the complete `client` folder and keep its `wz` subfolder intact.

On its first elevated launch for each Windows user, the script attempts to set MapleStory's 32-bit `soScreenMode` registry value to `3` so the game starts windowed. It then records `HKCU\Software\ComfyStory\WindowedModeInitialized`, regardless of whether the optional update succeeded, so later launches preserve the display mode selected in game. A failed update never blocks launch and is logged on a best-effort basis to `%LOCALAPPDATA%\ComfyStory\launcher.log`.

It requires Node/npm so that the pinned `@electron/asar` package can be invoked through `npx`. It never writes to `client-original/`.
