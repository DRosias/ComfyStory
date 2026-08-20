[CmdletBinding()]
param(
    [string]$Source = (Join-Path $PSScriptRoot '..\..\resources\Data.wz'),
    [string]$Destination = (Join-Path $PSScriptRoot '..\..\client\wz\Data.wz')
)

$ErrorActionPreference = 'Stop'
$sourcePath = [IO.Path]::GetFullPath($Source)
$destinationPath = [IO.Path]::GetFullPath($Destination)
$javaSource = Join-Path $PSScriptRoot 'StringWzTextPatcher.java'
$buildDir = Join-Path ([IO.Path]::GetTempPath()) ("ComfyStory-WzPatcher-" + [Guid]::NewGuid().ToString('N'))

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
        '-Source', ('"{0}"' -f $sourcePath),
        '-Destination', ('"{0}"' -f $destinationPath)
    )

    try {
        $process = Start-Process -FilePath $hostPath -ArgumentList $arguments -Verb RunAs -Wait -PassThru
    } catch {
        throw 'Administrator approval is required to update the working client Data.wz file.'
    }

    if ($process.ExitCode -ne 0) {
        throw "The elevated WZ update failed with exit code $($process.ExitCode)."
    }
}

New-Item -ItemType Directory -Path $buildDir | Out-Null
& javac -d $buildDir $javaSource
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to compile the WZ text patcher.'
}

# resources/Data.wz is the tracked source of truth for the project's custom WZ overrides.
& java -cp $buildDir StringWzTextPatcher $sourcePath $sourcePath
if ($LASTEXITCODE -ne 0) {
    throw 'Unable to apply the WZ text patch.'
}

try {
    Copy-Item -LiteralPath $sourcePath -Destination $destinationPath -Force
} catch [UnauthorizedAccessException] {
    if (Test-IsAdministrator) {
        throw
    }

    Write-Host 'Administrator approval is required to update the working client. Requesting elevation...'
    Invoke-ElevatedApply
}
