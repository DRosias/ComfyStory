# Data.wz text modification

This workflow makes the client and server display item `4310096` as `Comfy Coin`. The text is stored in the project's small custom `Data.wz` override, not in the pristine `String.wz`.

Run this from the repository root after restoring a working client from `client-original`:

```powershell
.\client-mods\wz\Apply-StringWzTextMod.ps1
```

The script updates the tracked `resources\Data.wz` source of truth and copies it to `client\wz\Data.wz`. It never modifies `client-original`, and is safe to rerun once the source already contains the exact generated result. The item name `Arcane Umbra Two-handed Swordie` is intentionally untouched.

If the working client was installed with administrator-only write permissions, the script requests UAC elevation only when it needs to replace `client\wz\Data.wz`. Declining the prompt leaves the working client unchanged.

For a future client version, inspect the desired property with HaRepacker (v11.0.0 was used for manual format verification), then update the source hash and narrowly adapt `StringWzTextPatcher.java`. Do not copy a full pristine WZ tree into Git.
