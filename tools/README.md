# Legacy Admin account cleanup

This local tool keeps user ID 1 and its characters, securely changes that user's
login name and password, and removes the empty Swordie seed users with IDs 2-4.
It refuses to continue if the database no longer matches those assumptions.

1. Stop the ComfyStory server. Its MySQL80 service on port 3307 should remain running.

The unrelated MariaDB service on port 3306 is not used or modified by this tool.
2. Preview the exact affected accounts:

   ```powershell
   .\tools\Reset-ComfyStoryLegacyAdmin.ps1 -DryRun
   ```

3. Run the interactive cleanup:

   ```powershell
   .\tools\Reset-ComfyStoryLegacyAdmin.ps1
   ```

The password is entered through a hidden console prompt and is never passed on
the command line or written to logs. Before applying changes, the tool saves the
four original `users` rows under `backups/account-security/`; that directory is
ignored by Git. The backup contains password data and should remain private.
