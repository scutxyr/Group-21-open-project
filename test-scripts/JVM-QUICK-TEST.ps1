# JVM Quick Test - PowerShell Version
# Run this script: .\test-scripts\JVM-QUICK-TEST.ps1

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# Colors
function Write-Success { Write-Host $args -ForegroundColor Green }
function Write-Info { Write-Host $args -ForegroundColor Cyan }
function Write-Warn { Write-Host $args -ForegroundColor Yellow }
function Write-Fail { Write-Host $args -ForegroundColor Red }

Clear-Host
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  JVM Info Quick Test (PowerShell)" -ForegroundColor Cyan
Write-Host "  (jvm, perfcounter, memory)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root
Set-Location (Split-Path -Parent $PSScriptRoot)
$ProjectRoot = Get-Location
$ArthasClient = "$ProjectRoot\client\target\arthas-client-jar-with-dependencies.jar"
$ArthasBoot = "$ProjectRoot\boot\target\arthas-boot-jar-with-dependencies.jar"

Write-Info "Project Root: $ProjectRoot"
Write-Host ""

# Check Java
try {
    $null = Get-Command java -ErrorAction Stop
    Write-Success "[OK] Java found"
} catch {
    Write-Fail "[ERROR] Java not found in PATH"
    pause
    exit 1
}

# Check required files
if (-not (Test-Path $ArthasClient)) {
    Write-Fail "[ERROR] Arthas Client not found"
    Write-Host "  Path: $ArthasClient"
    Write-Host "  Please build the project first: mvn clean package -DskipTests"
    pause
    exit 1
}

if (-not (Test-Path $ArthasBoot)) {
    Write-Fail "[ERROR] Arthas Boot not found"
    Write-Host "  Path: $ArthasBoot"
    Write-Host "  Please build the project first: mvn clean package -DskipTests"
    pause
    exit 1
}

Write-Success "[OK] All required files found"
Write-Host ""

# [1/4] Clean up old processes
Write-Info "[1/4] Cleaning up old processes..."
Get-Process | Where-Object {$_.MainWindowTitle -like "*MathGame*"} | Stop-Process -Force -ErrorAction SilentlyContinue
try {
    java -jar $ArthasClient 127.0.0.1 3658 -c "stop" 2>$null | Out-Null
} catch {
    # Ignore connection errors during cleanup
}
Start-Sleep -Seconds 2
Write-Success "   [OK] Done"
Write-Host ""

# [2/4] Start MathGame
Write-Info "[2/4] Starting MathGame..."
$javaArgs = @(
    "--add-opens", "java.base/jdk.internal.perf=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.perf=ALL-UNNAMED",
    "--add-opens", "java.management/sun.management.counter.perf=ALL-UNNAMED",
    "--add-opens", "java.management/sun.management.counter=ALL-UNNAMED",
    "-cp", "$ProjectRoot\math-game\target\classes",
    "demo.MathGame"
)
$null = Start-Process -FilePath "java" -ArgumentList $javaArgs -WindowStyle Hidden -PassThru

Start-Sleep -Seconds 5

# Get PID using jps
$jpsOutput = & jps | Select-String "MathGame"
if ($jpsOutput) {
    $MathGamePID = ($jpsOutput -split '\s+')[0]
    Write-Success "   [OK] PID = $MathGamePID"
} else {
    Write-Fail "   [ERROR] MathGame process not found"
    pause
    exit 1
}
Write-Host ""

# [3/4] Start Arthas
Write-Info "[3/4] Starting Arthas and attaching to process..."
$MathGamePID | Out-File "$env:TEMP\arthas_select.txt" -Encoding ASCII
$ArthasProcess = Start-Process java -ArgumentList "-jar", $ArthasBoot -RedirectStandardInput "$env:TEMP\arthas_select.txt" -WindowStyle Minimized -PassThru
Start-Sleep -Seconds 10
Write-Success "   [OK] Done"
Write-Host ""

# [4/4] Run tests
Write-Info "[4/4] Running JVM info tests..."
Write-Host ""

Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "Test 1: jvm - JVM Runtime Info" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
java -jar $ArthasClient 127.0.0.1 3658 -c "jvm"
Write-Host ""
Start-Sleep -Seconds 2

Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "Test 2: perfcounter - JVM Performance Counters" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
java -jar $ArthasClient 127.0.0.1 3658 -c "perfcounter"
Write-Host ""
Start-Sleep -Seconds 2

Write-Host "==========================================" -ForegroundColor Yellow
Write-Host "Test 3: memory - Memory Usage Details" -ForegroundColor Yellow
Write-Host "==========================================" -ForegroundColor Yellow
java -jar $ArthasClient 127.0.0.1 3658 -c "memory"
Write-Host ""
Start-Sleep -Seconds 2

# Cleanup
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Cleaning up processes..." -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
try {
    java -jar $ArthasClient 127.0.0.1 3658 -c "stop" 2>$null | Out-Null
} catch {
    # Ignore errors
}
Start-Sleep -Seconds 2
Get-Process | Where-Object {$_.MainWindowTitle -like "*MathGame*"} | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Success "[OK] Test completed"
Write-Host ""

pause
