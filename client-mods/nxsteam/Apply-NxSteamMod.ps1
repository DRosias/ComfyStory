[CmdletBinding()]
param(
    [string]$ProjectRoot
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    $ProjectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
}
$projectPath = (Resolve-Path -LiteralPath $ProjectRoot).Path
$originalLauncher = Join-Path $projectPath 'client-original\wz\nxsteam'
$originalAsar = Join-Path $originalLauncher 'resources\app.asar'
$workingLauncher = Join-Path $projectPath 'client\wz\ComfyStoryLauncher'
$workingResources = Join-Path $workingLauncher 'resources'
$workingSource = Join-Path $workingLauncher 'src'
$workingGameDir = Join-Path $projectPath 'client\wz'
$overlayPath = Join-Path $PSScriptRoot 'overlay'
$launcherPath = Join-Path $PSScriptRoot 'assets\Launch ComfyStory.cmd'

foreach ($requiredPath in @($originalLauncher, $originalAsar, $workingGameDir, $overlayPath, $launcherPath)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required path does not exist: $requiredPath"
    }
}

$npx = Get-Command npx.cmd -ErrorAction Stop
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('comfystory-nxsteam-' + [guid]::NewGuid().ToString('N'))
$extractPath = Join-Path $tempRoot 'app'
$packedAsar = Join-Path $tempRoot 'app.asar'
New-Item -ItemType Directory -Path $extractPath -Force | Out-Null

try {
    if (-not (Test-Path -LiteralPath $workingLauncher)) {
        New-Item -ItemType Directory -Path $workingLauncher -Force | Out-Null
        Copy-Item -Path (Join-Path $originalLauncher '*') -Destination $workingLauncher -Recurse -Force
    }

    & $npx.Source --yes '@electron/asar@3.2.10' extract $originalAsar $extractPath
    if ($LASTEXITCODE -ne 0) { throw 'Failed to extract the pristine app.asar.' }

    Copy-Item -Path (Join-Path $overlayPath '*') -Destination $extractPath -Recurse -Force

    & $npx.Source --yes '@electron/asar@3.2.10' pack $extractPath $packedAsar --unpack '**/*.{node,dll}'
    if ($LASTEXITCODE -ne 0) { throw 'Failed to pack the customized app.asar.' }

    Copy-Item -LiteralPath $packedAsar -Destination (Join-Path $workingResources 'app.asar') -Force
    $packedUnpacked = $packedAsar + '.unpacked'
    if (Test-Path -LiteralPath $packedUnpacked) {
        Copy-Item -Path (Join-Path $packedUnpacked '*') -Destination (Join-Path $workingResources 'app.asar.unpacked') -Recurse -Force
    }

    Copy-Item -Path (Join-Path $overlayPath '*') -Destination $workingSource -Recurse -Force
    $clientLauncher = Join-Path $projectPath 'client\Launch ComfyStory.cmd'
    $legacyLauncher = Join-Path $workingGameDir 'Launch ComfyStory.cmd'
    Copy-Item -LiteralPath $launcherPath -Destination $clientLauncher -Force
    if (Test-Path -LiteralPath $legacyLauncher) {
        Remove-Item -LiteralPath $legacyLauncher -Force
    }

    Write-Host 'ComfyStory nxsteam launcher applied successfully.'
} finally {
    $resolvedTempBase = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
    $resolvedTempRoot = [System.IO.Path]::GetFullPath($tempRoot)
    if ($resolvedTempRoot.StartsWith($resolvedTempBase, [System.StringComparison]::OrdinalIgnoreCase) -and
        (Test-Path -LiteralPath $resolvedTempRoot)) {
        Remove-Item -LiteralPath $resolvedTempRoot -Recurse -Force
    }
}
