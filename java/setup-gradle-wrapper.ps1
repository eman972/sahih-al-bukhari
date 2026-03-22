# setup-gradle-wrapper.ps1
# Run ONCE from the java/ directory to download the Gradle 8.7 wrapper JAR.
# After this, .\gradlew.bat works normally.
#
# Usage (from java/ directory):
#   powershell -ExecutionPolicy Bypass -File setup-gradle-wrapper.ps1

$GradleVersion = "8.7"
$JarUrl  = "https://raw.githubusercontent.com/gradle/gradle/v$GradleVersion.0/gradle/wrapper/gradle-wrapper.jar"
$JarDest = Join-Path $PSScriptRoot "gradle\wrapper\gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar for Gradle $GradleVersion..." -ForegroundColor Cyan
try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    (New-Object Net.WebClient).DownloadFile($JarUrl, $JarDest)
    Write-Host "Done! Saved to: $JarDest" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now run: .\gradlew.bat clean test" -ForegroundColor Yellow
} catch {
    Write-Host "Download failed: $_" -ForegroundColor Red
    Write-Host "Download manually from: $JarUrl" -ForegroundColor Yellow
    exit 1
}
