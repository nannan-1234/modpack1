$ErrorActionPreference = "Stop"

# Same approach as openblocks_anvil_compat: compile with javac directly against the local
# SRG class libs plus the Mekanism jar. No Gradle, no network needed.
#
# NOTE: this file is intentionally ASCII-only. Windows PowerShell reads .ps1 files as ANSI
# unless they carry a UTF-8 BOM, so non-ASCII text (the Chinese Mekanism file name) would be
# mangled. The Mekanism jar is therefore located by pattern.

$root = $PSScriptRoot
$mcRoot = "E:\minecraft\.minecraft"
$modsDir = Join-Path $mcRoot "versions\1.20.1-Forge_47.4.22-wip\mods"
$libraries = Join-Path $mcRoot "libraries"
$jdk = "C:\Program Files\Java\jdk-17"

$javac = Join-Path $jdk "bin\javac.exe"
$jarTool = Join-Path $jdk "bin\jar.exe"

$clientSrgJar = Join-Path $libraries "net\minecraft\client\1.20.1-20230612.114412\client-1.20.1-20230612.114412-srg.jar"
$forgeDir = Join-Path $libraries "net\minecraftforge\forge\1.20.1-47.4.22"
$universalJar = Join-Path $forgeDir "forge-1.20.1-47.4.22-universal.jar"
$fmlLanguageJar = Join-Path $libraries "net\minecraftforge\javafmllanguage\1.20.1-47.4.22\javafmllanguage-1.20.1-47.4.22.jar"
$fmlCoreJar = Join-Path $libraries "net\minecraftforge\fmlcore\1.20.1-47.4.22\fmlcore-1.20.1-47.4.22.jar"
$eventbusJar = Join-Path $libraries "net\minecraftforge\eventbus\6.0.5\eventbus-6.0.5.jar"
$fastutilJar = Join-Path $libraries "it\unimi\dsi\fastutil\8.5.12\fastutil-8.5.12.jar"
$slf4jJar = Join-Path $libraries "org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
$brigadierJar = Join-Path $libraries "com\mojang\brigadier\1.0.18\brigadier-1.0.18.jar"
# GUI milestone: Dist (client/server marker) lives in forgespi; FriendlyByteBuf inherits its
# read/write methods from netty's ByteBuf, so netty-buffer/common must be on the compile classpath.
$forgeSpiJar = Join-Path $libraries "net\minecraftforge\forgespi\6.0.0\forgespi-6.0.0.jar"
$nettyBufferJar = Join-Path $libraries "io\netty\netty-buffer\4.1.97.Final\netty-buffer-4.1.97.Final.jar"
$nettyCommonJar = Join-Path $libraries "io\netty\netty-common\4.1.97.Final\netty-common-4.1.97.Final.jar"
# Recipes milestone: recipe JSON parsing uses Gson directly (GsonHelper is SRG-mapped, Gson is not).
$gsonJar = Join-Path $libraries "com\google\code\gson\gson\2.10.1\gson-2.10.1.jar"
# JEI (optional integration): the plugin/category classes need the JEI API on the classpath.
$jeiJar = (Get-ChildItem $modsDir -Filter "*.jar" |
    Where-Object { $_.Name -match "jei-1\.20\.1-forge" } |
    Select-Object -First 1).FullName

# Forge applies accesstransformer.cfg at runtime, but javac against the raw SRG jar sees the
# original (package-private) nested types. Patch a copy for the compile classpath.
# Single quotes: the '$' must stay literal (PowerShell would treat "$BlockEntitySupplier" as a variable).
$atClasses = @('net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier')
# MenuScreens$ScreenConstructor is private in the raw SRG jar; Forge widens it at runtime via its
# access transformer, so the compile-time copy has to be patched the same way.
$atClasses += 'net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor'

# Locate the Mekanism core jar (exclude addons: Generators / Tools / Empowered / kubejs plugin / JEI addon).
$mekanismJar = (Get-ChildItem $modsDir -Filter "*.jar" |
    Where-Object { $_.Name -match "Mekanism-1\.20\.1-" -and $_.Name -notmatch "Generators|Tools|Empowered|kubejs|JustEnough" } |
    Select-Object -First 1).FullName

$srcDir = Join-Path $root "src\main\java"
$resDir = Join-Path $root "src\main\resources"
$buildDir = Join-Path $root "build"
$cacheDir = Join-Path $buildDir "cache"
$classesDir = Join-Path $buildDir "classes"
$libsDir = Join-Path $buildDir "libs"
$jarFile = Join-Path $libsDir "nannan_multiblock-0.1.0.jar"
$patchedSrgJar = Join-Path $cacheDir "client-srg-at.jar"
$patchScript = Join-Path $root "scripts\patch_srg_at.py"

# -LiteralPath everywhere: the Mekanism jar name contains "[...]", which PowerShell treats as a wildcard.
foreach ($required in @($javac, $jarTool, $clientSrgJar, $universalJar, $fmlLanguageJar, $fmlCoreJar, $eventbusJar, $fastutilJar, $slf4jJar, $brigadierJar, $forgeSpiJar, $nettyBufferJar, $nettyCommonJar, $gsonJar, $jeiJar, $mekanismJar, $patchScript)) {
    if (-not $required -or -not (Test-Path -LiteralPath $required)) { throw "missing compile dependency: $required" }
}

$python = (Get-Command python -ErrorAction SilentlyContinue).Source
if (-not $python) { $python = (Get-Command py -ErrorAction SilentlyContinue).Source }
if (-not $python) { throw "python is required to patch the compile-time classpath" }

New-Item -ItemType Directory -Force -Path $cacheDir, $classesDir, $libsDir | Out-Null

if (-not (Test-Path -LiteralPath $patchedSrgJar)) {
    & $python $patchScript $clientSrgJar $patchedSrgJar @atClasses
    if ($LASTEXITCODE -ne 0) { throw "classpath patch failed with exit code $LASTEXITCODE" }
}

Write-Host "Mekanism jar: $mekanismJar"
Write-Host "Compile classpath uses: $patchedSrgJar"

$classpath = @($patchedSrgJar, $universalJar, $fmlLanguageJar, $fmlCoreJar, $eventbusJar, $fastutilJar, $slf4jJar, $brigadierJar, $forgeSpiJar, $nettyBufferJar, $nettyCommonJar, $gsonJar, $jeiJar, $mekanismJar) -join ";"
$sources = Get-ChildItem $srcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

Get-ChildItem $classesDir -Recurse -File | Remove-Item -Force

# -proc:none: no annotation processing (no mixin annotation processor, no refmap needed here).
& $javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }

if (Test-Path -LiteralPath $jarFile) { Remove-Item -LiteralPath $jarFile -Force }
& $jarTool cf $jarFile -C $classesDir . -C $resDir .
if ($LASTEXITCODE -ne 0) { throw "jar failed with exit code $LASTEXITCODE" }

Write-Host "Built: $jarFile"
Write-Host "Next: copy it into $modsDir and start the game."
