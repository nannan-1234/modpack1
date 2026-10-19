$ErrorActionPreference = "Stop"

# Recolors Applied Botanics' mana cell textures into a "Reiryoku" palette:
# blue/purple casing -> jade teal, orange core -> gold, cyan accents -> teal.
# Shapes, brightness and alpha are preserved.

Add-Type -AssemblyName System.Drawing

$root = Split-Path $PSScriptRoot -Parent
$srcAb = "E:\minecraft\.minecraft\versions\1.20.1-Forge_47.4.22-wip\kubejs\.codex\tmp_assets\ab\textures"
$outItem = Join-Path $root "src\main\resources\assets\reiryoku_ae2\textures\item"
$outBlock = Join-Path $root "src\main\resources\assets\reiryoku_ae2\textures\block\drive\cells"

New-Item -ItemType Directory -Force -Path $outItem | Out-Null
New-Item -ItemType Directory -Force -Path $outBlock | Out-Null

function Convert-HslToRgb([double]$h, [double]$s, [double]$l) {
    if ($s -eq 0) {
        $v = [int][Math]::Round($l * 255)
        return @($v, $v, $v)
    }
    $h = (($h % 360) + 360) % 360
    $hn = $h / 360.0
    $q = if ($l -lt 0.5) { $l * (1 + $s) } else { $l + $s - $l * $s }
    $p = 2 * $l - $q
    $offsets = @((1.0 / 3.0), 0.0, (-1.0 / 3.0)) # R, G, B channel hue offsets
    $result = @()
    foreach ($off in $offsets) {
        $t = ((($hn + $off) % 1.0) + 1.0) % 1.0
        if ($t -lt (1.0 / 6.0)) { $c = $p + ($q - $p) * 6 * $t }
        elseif ($t -lt (1.0 / 2.0)) { $c = $q }
        elseif ($t -lt (2.0 / 3.0)) { $c = $p + ($q - $p) * 6 * ((2.0 / 3.0) - $t) }
        else { $c = $p }
        $result += [int][Math]::Round($c * 255)
    }
    return $result
}

function Get-NewHue([double]$h, [double]$s) {
    if ($s -lt 0.15) { return $null }          # gray: keep
    if ($h -ge 150 -and $h -lt 180) { return 165.0 }   # cyan -> teal
    if ($h -ge 180 -and $h -lt 285) { return 165.0 }   # blue -> teal
    if ($h -ge 285 -and $h -lt 330) { return 140.0 }   # purple -> green
    if ($h -lt 60) { return 42.0 }                     # red/orange -> gold
    return $null                                       # green/yellow: keep
}

function Recolor-Texture([string]$src, [string]$dst) {
    $bmp = [System.Drawing.Bitmap]::FromFile($src)
    $out = New-Object System.Drawing.Bitmap($bmp.Width, $bmp.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($x = 0; $x -lt $bmp.Width; $x++) {
        for ($y = 0; $y -lt $bmp.Height; $y++) {
            $c = $bmp.GetPixel($x, $y)
            if ($c.A -eq 0) {
                $out.SetPixel($x, $y, $c)
                continue
            }
            $h = $c.GetHue()
            $s = $c.GetSaturation()
            $l = $c.GetBrightness()
            $newHue = Get-NewHue $h $s
            if ($null -eq $newHue) {
                $out.SetPixel($x, $y, $c)
            } else {
                $rgb = Convert-HslToRgb $newHue $s $l
                $out.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($c.A, $rgb[0], $rgb[1], $rgb[2]))
            }
        }
    }
    $out.Save($dst, [System.Drawing.Imaging.ImageFormat]::Png)
    $out.Dispose()
    $bmp.Dispose()
}

$itemTiers = @(
    @("1k", "mana_storage_cell_1k.png"),
    @("4k", "mana_storage_cell_4k.png"),
    @("16k", "mana_storage_cell_16k.png"),
    @("64k", "mana_storage_cell_64k.png"),
    @("256k", "mana_storage_cell_256k.png")
)

foreach ($tier in $itemTiers) {
    Recolor-Texture (Join-Path $srcAb "item\$($tier[1])") (Join-Path $outItem "reiryoku_storage_cell_$($tier[0]).png")
}
Recolor-Texture (Join-Path $srcAb "item\mana_cell_housing.png") (Join-Path $outItem "reiryoku_cell_housing.png")

foreach ($tier in @("1k", "4k", "16k", "64k", "256k")) {
    Recolor-Texture (Join-Path $srcAb "block\drive\cells\mana_storage_cell_1k.png") (Join-Path $outBlock "reiryoku_storage_cell_$tier.png")
}

Write-Host "textures generated in $outItem and $outBlock"
