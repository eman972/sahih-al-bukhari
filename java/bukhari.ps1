# bukhari.ps1 — Sahih al-Bukhari CLI wrapper for Windows PowerShell

# Force UTF-8 console so Arabic, ﷺ, and box-drawing characters render correctly
chcp 65001 | Out-Null
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

$JarPattern = Join-Path $PSScriptRoot "build\libs\sahih-al-bukhari-*-all.jar"
$Jar = Get-Item $JarPattern -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not $Jar) {
    Write-Host "Building fat JAR first..." -ForegroundColor Cyan
    Push-Location $PSScriptRoot
    .\gradlew.bat shadowJar
    Pop-Location
    $Jar = Get-Item $JarPattern -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $Jar) {
        Write-Host "Build failed — could not find JAR." -ForegroundColor Red
        exit 1
    }
}

& java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar $Jar.FullName @args