# Run this script from an elevated PowerShell prompt.
$ErrorActionPreference = 'Stop'

$rules = @(
    @{ Name = 'ComfyStory Caddy HTTP'; Ports = '80' },
    @{ Name = 'ComfyStory Caddy HTTPS'; Ports = '443' },
    @{ Name = 'ComfyStory Game Login'; Ports = '8484' },
    @{ Name = 'ComfyStory Game Channels'; Ports = '8585-8604' }
)

foreach ($rule in $rules) {
    $existing = Get-NetFirewallRule -DisplayName $rule.Name -ErrorAction SilentlyContinue
    if ($existing) {
        Write-Host "Firewall rule already exists: $($rule.Name)"
        continue
    }
    New-NetFirewallRule `
        -DisplayName $rule.Name `
        -Direction Inbound `
        -Action Allow `
        -Protocol TCP `
        -LocalPort $rule.Ports `
        -Profile Any | Out-Null
    Write-Host "Created firewall rule: $($rule.Name)"
}
