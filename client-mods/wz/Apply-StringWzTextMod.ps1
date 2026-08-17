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

Copy-Item -LiteralPath $sourcePath -Destination $destinationPath -Force
