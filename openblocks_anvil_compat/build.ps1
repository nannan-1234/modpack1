$ErrorActionPreference = "Stop"

$root = $PSScriptRoot
$mcRoot = "E:\minecraft\.minecraft"
$jdk = "C:\Program Files\Java\jdk-17"

$javac = Join-Path $jdk "bin\javac.exe"
$jarTool = Join-Path $jdk "bin\jar.exe"

$forgeDir = Join-Path $mcRoot "libraries\net\minecraftforge\forge\1.20.1-47.4.22"
$clientSrgJar = Join-Path $mcRoot "libraries\net\minecraft\client\1.20.1-20230612.114412\client-1.20.1-20230612.114412-srg.jar"
$universalJar = Join-Path $forgeDir "forge-1.20.1-47.4.22-universal.jar"
$fmlLanguageJar = Join-Path $mcRoot "libraries\net\minecraftforge\javafmllanguage\1.20.1-47.4.22\javafmllanguage-1.20.1-47.4.22.jar"
$eventbusJar = Join-Path $mcRoot "libraries\net\minecraftforge\eventbus\6.0.5\eventbus-6.0.5.jar"
$mixinJar = Join-Path $mcRoot "libraries\org\spongepowered\mixin\0.8.5\mixin-0.8.5.jar"
$openblocksJar = Join-Path $mcRoot "versions\1.20.1-Forge_47.4.22-wip\mods\openblocks_reborn-1.0.2-1.20.1.jar"

$srcDir = Join-Path $root "src\main\java"
$resDir = Join-Path $root "src\main\resources"
$classesDir = Join-Path $root "build\classes"
$libsDir = Join-Path $root "build\libs"
$jarFile = Join-Path $libsDir "openblocks_anvil_compat-1.0.3.jar"

foreach ($required in @($javac, $jarTool, $clientSrgJar, $universalJar, $fmlLanguageJar, $eventbusJar, $mixinJar, $openblocksJar)) {
    if (-not (Test-Path $required)) { throw "missing compile dependency: $required" }
}

$classpath = @($clientSrgJar, $universalJar, $fmlLanguageJar, $eventbusJar, $mixinJar, $openblocksJar) -join ";"
$sources = Get-ChildItem $srcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $libsDir | Out-Null
Get-ChildItem $classesDir -Recurse -File | Remove-Item -Force

# -proc:none: the Mixin annotation processor needs Gson and would generate a
# refmap, but we compile straight against the SRG-named runtime classes, so no
# remapping is needed at all.
& $javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }

if (Test-Path $jarFile) { Remove-Item $jarFile -Force }
& $jarTool cfm $jarFile (Join-Path $resDir "META-INF\MANIFEST.MF") -C $classesDir . -C $resDir .
if ($LASTEXITCODE -ne 0) { throw "jar failed with exit code $LASTEXITCODE" }

Write-Host "Built: $jarFile"
