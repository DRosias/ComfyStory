[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [switch]$Restore
)

$ErrorActionPreference = 'Stop'
$expectedOriginalHash = '1223A5E43E662C4FAF139B136DAB0FC44884D49316C4A7C98AED43A755214AEB'

if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    $ProjectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
}

$projectPath = (Resolve-Path -LiteralPath $ProjectRoot).Path
$sourcePath = Join-Path $PSScriptRoot 'MapleBrowserSuppressor.cs'
$originalPath = Join-Path $projectPath 'client-original\wz\MapleBrowser_WZ2.exe'
$workingPath = Join-Path $projectPath 'client\wz\MapleBrowser_WZ2.exe'

function Test-IsAdministrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = [Security.Principal.WindowsPrincipal]::new($identity)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Invoke-ElevatedApply {
    $hostPath = (Get-Process -Id $PID).Path
    $arguments = @(
        '-NoProfile',
        '-ExecutionPolicy', 'Bypass',
        '-File', ('"{0}"' -f $PSCommandPath),
        '-ProjectRoot', ('"{0}"' -f $projectPath)
    )
    if ($Restore) {
        $arguments += '-Restore'
    }

    try {
        $process = Start-Process -FilePath $hostPath -ArgumentList $arguments -Verb RunAs -Wait -PassThru
    } catch {
        throw 'Administrator approval is required to update the working Maple browser helper.'
    }

    if ($process.ExitCode -ne 0) {
        throw "The elevated Maple browser update failed with exit code $($process.ExitCode)."
    }
}

function Copy-ToWorkingClient([string]$Source) {
    try {
        Copy-Item -LiteralPath $Source -Destination $workingPath -Force
        return $true
    } catch [UnauthorizedAccessException] {
        if (Test-IsAdministrator) {
            throw
        }

        Write-Host 'Administrator approval is required to update the working client. Requesting elevation...'
        Invoke-ElevatedApply
        return $false
    }
}

foreach ($requiredPath in @($sourcePath, $originalPath, (Split-Path $workingPath -Parent))) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required path does not exist: $requiredPath"
    }
}

if (Get-Process -Name MapleStory, MapleBrowser_WZ2 -ErrorAction SilentlyContinue) {
    throw 'Close MapleStory and its browser popup before updating the browser helper.'
}

$originalHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $originalPath).Hash
if ($originalHash -ne $expectedOriginalHash) {
    throw "The pristine MapleBrowser_WZ2.exe does not match the expected v232.2 file. Refusing to modify the working client."
}

if ($Restore) {
    if (Copy-ToWorkingClient $originalPath) {
        Write-Host 'Restored the pristine MapleBrowser_WZ2.exe to the working client.'
    }
    exit 0
}

if (Test-Path -LiteralPath $workingPath) {
    $workingDescription = (Get-Item -LiteralPath $workingPath).VersionInfo.FileDescription
    $isOriginal = (Get-FileHash -Algorithm SHA256 -LiteralPath $workingPath).Hash -eq $expectedOriginalHash
    $isSuppressor = $workingDescription -eq 'ComfyStory Maple Browser Suppressor'
    if (-not $isOriginal -and -not $isSuppressor) {
        throw 'The working MapleBrowser_WZ2.exe contains an unrecognized modification. Refusing to overwrite it.'
    }
}

$compilerCandidates = @(
    (Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'),
    (Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319\csc.exe')
)
$compilerPath = $compilerCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
if (-not $compilerPath) {
    throw 'The Windows .NET Framework C# compiler was not found.'
}

$tempBase = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$tempRoot = Join-Path $tempBase ('comfystory-maple-browser-' + [guid]::NewGuid().ToString('N'))
$stubPath = Join-Path $tempRoot 'MapleBrowser_WZ2.exe'
New-Item -ItemType Directory -Path $tempRoot | Out-Null

try {
    & $compilerPath `
        /nologo `
        /target:winexe `
        /optimize+ `
        /platform:anycpu `
        "/out:$stubPath" `
        $sourcePath
    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $stubPath)) {
        throw 'Failed to build the Maple browser suppressor.'
    }

    if (Copy-ToWorkingClient $stubPath) {
        Write-Host 'Maple news browser suppression applied to the working client.'
    }
} finally {
    $resolvedTempRoot = [System.IO.Path]::GetFullPath($tempRoot)
    if ($resolvedTempRoot.StartsWith($tempBase, [System.StringComparison]::OrdinalIgnoreCase) -and
        (Test-Path -LiteralPath $resolvedTempRoot)) {
        Remove-Item -LiteralPath $resolvedTempRoot -Recurse -Force
    }
}
