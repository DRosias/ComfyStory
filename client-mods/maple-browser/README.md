# Maple news browser suppression

MapleStory v232.2 launches `MapleBrowser_WZ2.exe` to display live Nexon news in game. This modification replaces only the working client's browser helper with a small no-window program that exits successfully, preventing the popup and its outbound request.

The pristine client remains untouched. The suppressor is built locally from `MapleBrowserSuppressor.cs`; no generated executable is stored in Git.

Apply from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\client-mods\maple-browser\Apply-MapleBrowserSuppression.ps1
```

Close MapleStory first. The script requests administrator approval when the working client retains its original protected ACL.

Restore the original helper from `client-original`:

```powershell
powershell -ExecutionPolicy Bypass -File .\client-mods\maple-browser\Apply-MapleBrowserSuppression.ps1 -Restore
```
