# Arthas JVM Test Suite - PowerShell Version
# Tests: jvm, memory, perfcounter commands

$ErrorActionPreference = "Continue"

Clear-Host
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Arthas JVM Test Suite" -ForegroundColor Cyan
Write-Host "  Testing: jvm, memory, perfcounter" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root
Set-Location (Split-Path -Parent $PSScriptRoot)
$ProjectRoot = Get-Location

# Generate timestamp for report
$Timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$ReportFile = "$ProjectRoot\test-scripts\reports\jvm-test-$Timestamp.txt"

# Create reports directory if not exists
$ReportsDir = "$ProjectRoot\test-scripts\reports"
if (-not (Test-Path $ReportsDir)) {
    New-Item -ItemType Directory -Path $ReportsDir | Out-Null
}

# Check MathGame process
Write-Host "[CHECK] Checking MathGame process..." -ForegroundColor Yellow
$jpsOutput = & jps | Select-String "MathGame"
if (-not $jpsOutput) {
    Write-Host "[ERROR] MathGame process not found!" -ForegroundColor Red
    Write-Host "[INFO] Please start MathGame first:" -ForegroundColor Yellow
    Write-Host "       java -cp math-game\target\classes demo.MathGame" -ForegroundColor Green
    Write-Host ""
    pause
    exit 1
}

$MathGamePID = ($jpsOutput -split '\s+')[0]
Write-Host "[OK] MathGame is running with PID = $MathGamePID" -ForegroundColor Green
Write-Host ""

# Set Arthas paths
$ArthasClient = "$ProjectRoot\client\target\arthas-client-jar-with-dependencies.jar"

# Check Arthas connection
Write-Host "[CHECK] Testing Arthas connection (port 3658)..." -ForegroundColor Yellow
try {
    $null = java -jar $ArthasClient 127.0.0.1 3658 -c "version" 2>$null
    Write-Host "[OK] Arthas is connected" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Cannot connect to Arthas on port 3658" -ForegroundColor Red
    Write-Host "[INFO] Please attach Arthas to MathGame first:" -ForegroundColor Yellow
    Write-Host "       java -jar boot\target\arthas-boot-jar-with-dependencies.jar $MathGamePID" -ForegroundColor Green
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Starting Tests" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Initialize report file
@"
Arthas JVM Test Report
Generated: $Timestamp
MathGame PID: $MathGamePID
==========================================

"@ | Out-File -FilePath $ReportFile -Encoding UTF8

# Test results
$TestResults = @()

# Test 1: jvm command
Write-Host "[1/3] Testing JVM command..." -ForegroundColor Yellow
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"TEST 1: jvm - JVM Runtime Information" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
try {
    $output = java -jar $ArthasClient 127.0.0.1 3658 -c "jvm" 2>&1
    $output | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    Write-Host $output
    Write-Host "[PASS] jvm command succeeded" -ForegroundColor Green
    $TestResults += @{Name="jvm"; Status="PASS"}
} catch {
    Write-Host "[FAIL] jvm command failed" -ForegroundColor Red
    "[FAIL] jvm command failed" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    $TestResults += @{Name="jvm"; Status="FAIL"}
}
Write-Host ""
"" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
Start-Sleep -Seconds 1

# Test 2: memory command
Write-Host "[2/3] Testing MEMORY command..." -ForegroundColor Yellow
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"TEST 2: memory - Memory Usage Details" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
try {
    $output = java -jar $ArthasClient 127.0.0.1 3658 -c "memory" 2>&1
    $output | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    Write-Host $output
    Write-Host "[PASS] memory command succeeded" -ForegroundColor Green
    $TestResults += @{Name="memory"; Status="PASS"}
} catch {
    Write-Host "[FAIL] memory command failed" -ForegroundColor Red
    "[FAIL] memory command failed" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    $TestResults += @{Name="memory"; Status="FAIL"}
}
Write-Host ""
"" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
Start-Sleep -Seconds 1

# Test 3: perfcounter command
Write-Host "[3/3] Testing PERFCOUNTER command..." -ForegroundColor Yellow
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"TEST 3: perfcounter - JVM Performance Counters" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
"==========================================" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
try {
    $output = java -jar $ArthasClient 127.0.0.1 3658 -c "perfcounter" 2>&1
    $output | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    Write-Host $output
    Write-Host "[PASS] perfcounter command succeeded" -ForegroundColor Green
    $TestResults += @{Name="perfcounter"; Status="PASS"}
} catch {
    Write-Host "[FAIL] perfcounter command failed" -ForegroundColor Red
    "[FAIL] perfcounter command failed" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
    $TestResults += @{Name="perfcounter"; Status="FAIL"}
}
Write-Host ""
"" | Out-File -FilePath $ReportFile -Append -Encoding UTF8

# Test Summary
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
$PassCount = ($TestResults | Where-Object { $_.Status -eq "PASS" }).Count
$FailCount = ($TestResults | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "Total Tests: 3" -ForegroundColor White
Write-Host "Passed: $PassCount" -ForegroundColor Green
Write-Host "Failed: $FailCount" -ForegroundColor $(if ($FailCount -eq 0) { "Green" } else { "Red" })
Write-Host ""
Write-Host "Report saved to: $ReportFile" -ForegroundColor Yellow
Write-Host ""

# Write summary to report
@"
==========================================
Test Summary
==========================================
Total Tests: 3
Passed: $PassCount
Failed: $FailCount

Test Details:
"@ | Out-File -FilePath $ReportFile -Append -Encoding UTF8

foreach ($result in $TestResults) {
    "- $($result.Name): $($result.Status)" | Out-File -FilePath $ReportFile -Append -Encoding UTF8
}

pause
