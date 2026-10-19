$ErrorActionPreference = "Stop"

# Primary Windows entry. The Gradle wrapper owns mappings and reobfuscation.

$root = $PSScriptRoot
$repoRoot = Split-Path (Split-Path $root -Parent) -Parent
if (-not $env:GRADLE_USER_HOME) {
    $env:GRADLE_USER_HOME = Join-Path $repoRoot ".gradle-home"
}

Push-Location $root
try {
    $hasTask = $args | Where-Object { -not $_.StartsWith('-') } | Select-Object -First 1
    if ($hasTask) {
        & "$root\gradlew.bat" @args
    } else {
        & "$root\gradlew.bat" build @args
    }
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
