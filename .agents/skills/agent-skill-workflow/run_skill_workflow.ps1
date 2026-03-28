param(
    [string]$RunId = (Get-Date -Format yyyyMMddHHmmss),
    [string]$TestScope = "changed", # full|changed|module:<name>|smoke
    [string]$ArtifactDir = "docs\\reports",
    [string]$MavenCmd = "mvn"
)

# Prepare artifact paths
$runDir = Join-Path $ArtifactDir $RunId
New-Item -ItemType Directory -Path $runDir -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $runDir "reports") -Force | Out-Null

# Metadata
$meta = [PSCustomObject]@{
    run_id = $RunId
    test_scope = $TestScope
    branch = (git rev-parse --abbrev-ref HEAD 2>$null)
    commit = (git rev-parse HEAD 2>$null)
    started_at = (Get-Date).ToString("o")
}
$meta | ConvertTo-Json | Out-File -FilePath (Join-Path $runDir "meta.json") -Encoding utf8

# Create backup branch
$backupBranch = "skill/$RunId-backup"
Write-Output "Creating backup branch $backupBranch"
git branch $backupBranch
git rev-parse HEAD | Out-File -FilePath (Join-Path $runDir "commit.txt") -Encoding utf8
git diff > (Join-Path $runDir "change.patch")

# Helper to run mvn and capture logs
function Run-Maven($cmd) {
    $logPath = Join-Path $runDir "mvn.log"
    Write-Output "Running: $cmd"
    & cmd /c "$cmd" *> $logPath 2>&1
    return $logPath
}

if ($TestScope -eq 'full') {
    Run-Maven "$MavenCmd clean test"
} elseif ($TestScope -eq 'smoke') {
    Run-Maven "$MavenCmd -Dtest=*Smoke* test"
} elseif ($TestScope -like 'module:*') {
    $module = $TestScope.Split(':')[1]
    Run-Maven "$MavenCmd -pl $module -am test"
} else {
    # changed: detect changed files against origin/main
    Write-Output "Detecting changed files against origin/main"
    git fetch origin main --quiet
    $changed = git diff --name-only origin/main...HEAD
    if ([string]::IsNullOrWhiteSpace($changed)) {
        Write-Output "No changed files detected; falling back to smoke tests"
        Run-Maven "$MavenCmd -Dtest=*Smoke* test"
    } else {
        # naive mapping: assume first path segment is module
        $modules = $changed -split "`n" | ForEach-Object { ($_ -split "\\/|\\\\")[0] } | Select-Object -Unique
        foreach ($m in $modules) {
            if (-not [string]::IsNullOrWhiteSpace($m)) {
                Run-Maven "$MavenCmd -pl $m -am test"
            }
        }
    }
}

# Collect surefire/failsafe reports
Get-ChildItem -Path . -Filter "surefire-report*.xml" -Recurse -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item $_.FullName -Destination (Join-Path $runDir "reports") -Force
}
Get-ChildItem -Path . -Filter "*.xml" -Recurse -Include "surefire-reports","failsafe-reports" -ErrorAction SilentlyContinue | ForEach-Object {
    Copy-Item $_.FullName -Destination (Join-Path $runDir "reports") -Force
}

# Update meta with finished time
$meta.finished_at = (Get-Date).ToString("o")
$meta | ConvertTo-Json | Out-File -FilePath (Join-Path $runDir "meta.json") -Encoding utf8

Write-Output "Artifacts stored at: $runDir" 
