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

    Write-Host "=== Pre-warming JVM (login + key endpoints) ===" -ForegroundColor Cyan
    # Spring Boot lazy-initialises beans on first request per controller path.
    # Without warmup, the first hit by Playwright on each /create or /list
    # endpoint can take 10-25s, racing past page.goto's 15s timeout.
    # Hit the same endpoints the test suite uses so JIT + bean graphs are warm
    # before Playwright starts.
    try {
        $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
        # Get CSRF token from login page
        $loginPage = Invoke-WebRequest -Uri "http://localhost:18080/login" -WebSession $session -UseBasicParsing -TimeoutSec 30
        $csrfMatch = [regex]::Match($loginPage.Content, 'name="_csrf"\s+value="([^"]+)"')
        if (-not $csrfMatch.Success) {
            $csrfMatch = [regex]::Match($loginPage.Content, 'value="([^"]+)"\s+name="_csrf"')
        }
        if ($csrfMatch.Success) {
            $csrfToken = $csrfMatch.Groups[1].Value
            $body = @{ username = 'admin'; password = 'admin123'; '_csrf' = $csrfToken }
            Invoke-WebRequest -Uri "http://localhost:18080/login" -WebSession $session -Method Post -Body $body -UseBasicParsing -TimeoutSec 30 -MaximumRedirection 5 | Out-Null
        } else {
            Invoke-WebRequest -Uri "http://localhost:18080/login" -WebSession $session -Method Post -Body @{ username = 'admin'; password = 'admin123' } -UseBasicParsing -TimeoutSec 30 -MaximumRedirection 5 | Out-Null
        }

        $warmupUrlFile = Join-Path $PSScriptRoot "warmup-urls.txt"
        $warmupUrls = Get-Content $warmupUrlFile |
            ForEach-Object { $_.Trim() } |
            Where-Object { $_ -and -not $_.StartsWith("#") }
        foreach ($u in $warmupUrls) {
            try {
                Invoke-WebRequest -Uri "http://localhost:18080$u" -WebSession $session -UseBasicParsing -TimeoutSec 30 -MaximumRedirection 5 | Out-Null
            } catch {
                Write-Host ("warmup miss " + $u + ": " + $_.Exception.Message) -ForegroundColor DarkYellow
            }
        }
        Write-Host "=== Warmup complete ===" -ForegroundColor Cyan
    } catch {
        Write-Host ("Warmup failed: " + $_.Exception.Message) -ForegroundColor Yellow
    }

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
