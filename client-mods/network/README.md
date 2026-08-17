# ComfyStory network setup

## Caddy

Merge `Caddyfile.example` into `C:\Caddy\Caddyfile`. It preserves the existing `D:/HostedFiles` server and sends only `/api/*` to Swordie's loopback-only web API.

Validate and restart Caddy from an elevated PowerShell prompt:

```powershell
C:\Caddy\caddy.exe validate --config C:\Caddy\Caddyfile
Restart-Service Caddy
```

Do this after `danny-games.servegame.com` resolves to the router's public IPv4 address.

## Windows Firewall

Review and then run `Configure-ComfyStoryFirewall.ps1` from an elevated PowerShell prompt. The script is idempotent and creates TCP-only rules for ports 80, 443, 8484, and 8585-8604.

## Router forwarding

Reserve this PC's LAN IPv4 address, then forward these external TCP ports to the same internal ports on that address:

- 80: Caddy HTTP and certificate handling
- 443: HTTPS launcher authentication
- 8484: MapleStory login
- 8585-8604: Bera channels 1-20

Do not forward 8483 (legacy raw authentication), 3000 (internal web API), or 3306 (MySQL). No UDP forwarding is required.

Test from a different internet connection. If the ISP uses CGNAT, ordinary router forwarding will not make the server reachable.
