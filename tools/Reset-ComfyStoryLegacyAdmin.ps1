[CmdletBinding()]
param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$toolDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $toolDirectory
$exitCode = 1
$originalJavaHome = $env:JAVA_HOME

$javaExecutable = (Get-Command java.exe -ErrorAction Stop).Source
$javaRuntimeHome = Split-Path -Parent (Split-Path -Parent $javaExecutable)
$javaVersion = (& $javaExecutable --version | Out-String)
if ($javaVersion -notmatch '(?m)^(openjdk|java) 21[\.]') {
    throw 'Java 21 is required to build and run the ComfyStory maintenance tool.'
}

Push-Location -LiteralPath $projectRoot
try {
    $env:JAVA_HOME = $javaRuntimeHome
    Write-Host 'Building the local ComfyStory maintenance tool...'
    & mvn.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }

    $serverJar = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'bin') -Filter 'maplestory-*.jar' |
        Where-Object { $_.Name -notlike 'original-*' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $serverJar) {
        throw 'The built ComfyStory server JAR was not found under bin.'
    }

    $javaArguments = @(
        '-cp',
        $serverJar.FullName,
        'net.swordie.tools.LegacyAdminCleanupTool'
    )
    if ($DryRun) {
        $javaArguments += '--dry-run'
    }

    & $javaExecutable @javaArguments
    $exitCode = $LASTEXITCODE
} catch {
    Write-Error $_
    $exitCode = 1
} finally {
    if ($null -eq $originalJavaHome) {
        Remove-Item Env:JAVA_HOME -ErrorAction SilentlyContinue
    } else {
        $env:JAVA_HOME = $originalJavaHome
    }
    Pop-Location
}

exit $exitCode
