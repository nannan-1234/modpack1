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
$fmlcoreJar = Join-Path $mcRoot "libraries\net\minecraftforge\fmlcore\1.20.1-47.4.22\fmlcore-1.20.1-47.4.22.jar"
$eventbusJar = Join-Path $mcRoot "libraries\net\minecraftforge\eventbus\6.0.5\eventbus-6.0.5.jar"
$forgespiJar = Join-Path $mcRoot "libraries\net\minecraftforge\forgespi\6.0.0\forgespi-6.0.0.jar"
$mixinJar = Join-Path $mcRoot "libraries\org\spongepowered\mixin\0.8.5\mixin-0.8.5.jar"
$nettyBufferJar = Join-Path $mcRoot "libraries\io\netty\netty-buffer\4.1.82.Final\netty-buffer-4.1.82.Final.jar"
$nettyCommonJar = Join-Path $mcRoot "libraries\io\netty\netty-common\4.1.82.Final\netty-common-4.1.82.Final.jar"
$brigadierJar = Join-Path $mcRoot "libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar"
$jomlJar = Join-Path $mcRoot "libraries\org\joml\joml\1.10.5\joml-1.10.5.jar"
$gsonJar = Join-Path $mcRoot "libraries\com\google\code\gson\gson\2.10\gson-2.10.jar"
$slf4jJar = Join-Path $mcRoot "libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"

$modsDir = Join-Path $mcRoot "versions\1.20.1-Forge_47.4.22-wip\mods"
$ae2Jar = (Get-ChildItem $modsDir | Where-Object { $_.Name -like "*appliedenergistics2-forge-15.4.10.jar" }).FullName
$urushiJar = (Get-ChildItem $modsDir | Where-Object { $_.Name -like "*urushi-1.20.1-6.6.3.jar" }).FullName
$kubejsJar = (Get-ChildItem $modsDir | Where-Object { $_.Name -like "*kubejs-forge-2001.6.5*" }).FullName

$srcDir = Join-Path $root "src\main\java"
$resDir = Join-Path $root "src\main\resources"
$classesDir = Join-Path $root "build\classes"
$libsDir = Join-Path $root "build\libs"
$jarFile = Join-Path $libsDir "reiryoku_expansion-1.3.0.jar"

foreach ($required in @($javac, $jarTool, $clientSrgJar, $universalJar, $fmlLanguageJar, $fmlcoreJar, $eventbusJar, $forgespiJar, $mixinJar, $nettyBufferJar, $nettyCommonJar, $brigadierJar, $jomlJar, $gsonJar, $slf4jJar, $kubejsJar, $ae2Jar, $urushiJar)) {
    if (-not (Test-Path -LiteralPath $required)) { throw "missing compile dependency: $required" }
}

$classpath = @($clientSrgJar, $universalJar, $fmlLanguageJar, $fmlcoreJar, $eventbusJar, $forgespiJar, $mixinJar, $nettyBufferJar, $nettyCommonJar, $brigadierJar, $jomlJar, $gsonJar, $slf4jJar, $kubejsJar, $ae2Jar, $urushiJar) -join ";"
$sources = Get-ChildItem $srcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $libsDir | Out-Null
Get-ChildItem $classesDir -Recurse -File -ErrorAction SilentlyContinue | Remove-Item -Force

# -proc:none: the Mixin annotation processor would need Gson and generates a
# refmap; we compile straight against SRG-named runtime classes, so no remap.
& $javac --release 17 -encoding UTF-8 -proc:none -cp $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }

if (Test-Path $jarFile) { Remove-Item $jarFile -Force }
& $jarTool cfm $jarFile (Join-Path $resDir "META-INF\MANIFEST.MF") -C $classesDir . -C $resDir .
if ($LASTEXITCODE -ne 0) { throw "jar failed with exit code $LASTEXITCODE" }

Write-Host "Built: $jarFile"
