# E2E PoC Validation Script (Windows PowerShell)
# Usage: .\e2e-tests\scripts\run-poc.ps1

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

Write-Host "=== Building JAR with e2e profile ===" -ForegroundColor Cyan
Set-Location $ProjectRoot
# Remove any older JARs first so the post-build glob can only pick the artifact
# we just produced. Otherwise stale solusi-program-erp-<old>.jar files left in
# target/ get selected alphabetically by Get-ChildItem and the run boots an
# obsolete build.
Get-ChildItem "$ProjectRoot\target\solusi-program-erp-*.jar" -ErrorAction SilentlyContinue |
    Remove-Item -Force -ErrorAction SilentlyContinue
.\mvnw.cmd -B package -DskipTests -Pe2e -q
if ($LASTEXITCODE -ne 0) { Write-Error "Build failed"; exit 1 }

# Pick the newest JAR by modification time (defensive even after the cleanup
# above, in case a developer drops a JAR into target/ between operations).
$jar = (Get-ChildItem "$ProjectRoot\target\solusi-program-erp-*.jar" |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
Write-Host "=== Starting server: $jar ===" -ForegroundColor Cyan

$proc = Start-Process -FilePath "java" -ArgumentList "-jar",$jar,"--spring.profiles.active=e2e" -PassThru -WindowStyle Hidden -RedirectStandardOutput "$ProjectRoot\target\e2e-server.log" -RedirectStandardError "$ProjectRoot\target\e2e-server-err.log"

try {
    Write-Host "=== Waiting for server (max 60s) ===" -ForegroundColor Cyan
    $ready = $false
    for ($i = 1; $i -le 60; $i++) {
        try {
            $r = Invoke-WebRequest -Uri "http://localhost:18080/login" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
            if ($r.StatusCode -eq 200) { $ready = $true; Write-Host "Server ready after ${i}s"; break }
        } catch { Start-Sleep -Seconds 1 }
    }
    if (-not $ready) { Write-Error "Server failed to start within 60s"; exit 1 }

    Write-Host "=== Installing Playwright dependencies ===" -ForegroundColor Cyan
    Set-Location "$ProjectRoot\e2e-tests"
    npm ci --silent
    npx playwright install chromium

    Write-Host "=== Running Playwright tests ===" -ForegroundColor Cyan
    npx playwright test
    $testExit = $LASTEXITCODE
} finally {
    Write-Host "=== Stopping server (PID: $($proc.Id)) ===" -ForegroundColor Cyan
    Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
}

exit $testExit
