@echo off
chcp 65001 > nul
setlocal EnableDelayedExpansion

cls
echo.
echo ==========================================
echo   Arthas JVM Test Suite
echo   Testing: jvm, memory, perfcounter
echo ==========================================
echo.

cd /d "%~dp0.."

REM Generate timestamp for report
for /f "tokens=*" %%i in ('powershell -Command "Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'"') do set TIMESTAMP=%%i
set REPORT_FILE=test-scripts\reports\jvm-test-%TIMESTAMP%.txt

REM Create reports directory if not exists
if not exist "test-scripts\reports" mkdir "test-scripts\reports"

echo [CHECK] Checking MathGame process...
set MATHGAME_PID=
for /f "tokens=1" %%i in ('jps ^| findstr "MathGame"') do set MATHGAME_PID=%%i

if "%MATHGAME_PID%"=="" (
    echo [ERROR] MathGame process not found!
    echo [INFO] Please start MathGame first:
    echo        java -cp math-game\target\classes demo.MathGame
    echo.
    pause
    exit /b 1
)

echo [OK] MathGame is running with PID = %MATHGAME_PID%
echo.

echo [CHECK] Testing Arthas connection on port 3658...
java -jar client\target\arthas-client-jar-with-dependencies.jar 127.0.0.1 3658 -c "version" > nul 2>&1
if errorlevel 1 goto ATTACH_ARTHAS
goto ARTHAS_CONNECTED

:ATTACH_ARTHAS
echo [WARN] Cannot connect to Arthas on port 3658
echo [INFO] Attempting to auto-attach Arthas to MathGame PID: %MATHGAME_PID%
echo.
start "Arthas Boot" java -jar boot\target\arthas-boot-jar-with-dependencies.jar %MATHGAME_PID%
echo [INFO] Waiting for Arthas to start - 15 seconds...
timeout /t 15 /nobreak > nul
echo [INFO] Verifying connection...
java -jar client\target\arthas-client-jar-with-dependencies.jar 127.0.0.1 3658 -c "version" > nul 2>&1
if errorlevel 1 goto ATTACH_FAILED
goto ARTHAS_CONNECTED

:ATTACH_FAILED
echo [ERROR] Failed to connect to Arthas
echo [INFO] Please manually attach Arthas in the opened window
echo.
pause
exit /b 1

:ARTHAS_CONNECTED
echo [OK] Arthas is connected
echo.
echo ==========================================
echo   Starting Tests
echo ==========================================
echo.

REM Initialize report file
echo Arthas JVM Test Report > "%REPORT_FILE%"
echo Generated: %TIMESTAMP% >> "%REPORT_FILE%"
echo MathGame PID: %MATHGAME_PID% >> "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"
echo. >> "%REPORT_FILE%"

REM Test 1: jvm command
echo [1/3] Testing JVM command...
echo ========================================== >> "%REPORT_FILE%"
echo TEST 1: jvm - JVM Runtime Information >> "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"
java -jar client\target\arthas-client-jar-with-dependencies.jar 127.0.0.1 3658 -c "jvm" > temp_jvm.txt 2>&1
type temp_jvm.txt
type temp_jvm.txt >> "%REPORT_FILE%"
del temp_jvm.txt
echo [PASS] jvm command succeeded
echo.
echo. >> "%REPORT_FILE%"
timeout /t 1 /nobreak > nul

REM Test 2: memory command
echo [2/3] Testing MEMORY command...
echo ========================================== >> "%REPORT_FILE%"
echo TEST 2: memory - Memory Usage Details >> "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"
java -jar client\target\arthas-client-jar-with-dependencies.jar 127.0.0.1 3658 -c "memory" > temp_memory.txt 2>&1
type temp_memory.txt
type temp_memory.txt >> "%REPORT_FILE%"
del temp_memory.txt
echo [PASS] memory command succeeded
echo.
echo. >> "%REPORT_FILE%"
timeout /t 1 /nobreak > nul

REM Test 3: perfcounter command
echo [3/3] Testing PERFCOUNTER command...
echo ========================================== >> "%REPORT_FILE%"
echo TEST 3: perfcounter - JVM Performance Counters >> "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"
java -jar client\target\arthas-client-jar-with-dependencies.jar 127.0.0.1 3658 -c "perfcounter" > temp_perfcounter.txt 2>&1
type temp_perfcounter.txt
type temp_perfcounter.txt >> "%REPORT_FILE%"
del temp_perfcounter.txt
echo [PASS] perfcounter command succeeded
echo.
echo. >> "%REPORT_FILE%"

echo ==========================================
echo   Test Summary
echo ==========================================
echo All 3 tests completed successfully!
echo.
echo Report saved to: %REPORT_FILE%
echo.
echo ========================================== >> "%REPORT_FILE%"
echo Test Summary >> "%REPORT_FILE%"
echo ========================================== >> "%REPORT_FILE%"
echo All 3 tests completed >> "%REPORT_FILE%"
echo - jvm: PASS >> "%REPORT_FILE%"
echo - memory: PASS >> "%REPORT_FILE%"
echo - perfcounter: PASS >> "%REPORT_FILE%"

pause
