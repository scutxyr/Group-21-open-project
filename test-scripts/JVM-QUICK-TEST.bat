@echo off
REM Use UTF-8 encoding for display
chcp 65001 > nul
setlocal EnableDelayedExpansion

cls
echo.
echo ==========================================
echo   JVM Info Quick Test
echo   (jvm, perfcounter, memory)
echo ==========================================
echo.

REM Change to project root directory
cd /d "%~dp0.."
set "PROJECT_ROOT=%CD%"
set "ARTHAS_CLIENT=%PROJECT_ROOT%\client\target\arthas-client-jar-with-dependencies.jar"
set "ARTHAS_BOOT=%PROJECT_ROOT%\boot\target\arthas-boot-jar-with-dependencies.jar"

REM Check Java
where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found in PATH
    pause
    exit /b 1
)

echo Project Root: %PROJECT_ROOT%
echo.

REM Check required files
if not exist "%ARTHAS_CLIENT%" (
    echo [ERROR] Arthas Client not found
    echo Path: %ARTHAS_CLIENT%
    echo Please build the project first: mvn clean package -DskipTests
    pause
    exit /b 1
)

if not exist "%ARTHAS_BOOT%" (
    echo [ERROR] Arthas Boot not found  
    echo Path: %ARTHAS_BOOT%
    echo Please build the project first: mvn clean package -DskipTests
    pause
    exit /b 1
)

REM [1/4] Clean up old processes
echo [1/4] Cleaning up old processes...
taskkill /F /FI "WindowTitle eq MathGame*" > nul 2>&1
java -jar "%ARTHAS_CLIENT%" 127.0.0.1 3658 -c "stop" > nul 2>&1
timeout /t 2 /nobreak > nul
echo    [OK] Done
echo.

REM [2/4] Start MathGame
echo [2/4] Starting MathGame...
start /MIN "MathGame-Demo" java --add-opens java.base/jdk.internal.perf=ALL-UNNAMED --add-exports java.base/jdk.internal.perf=ALL-UNNAMED --add-opens java.management/sun.management.counter.perf=ALL-UNNAMED --add-opens java.management/sun.management.counter=ALL-UNNAMED -cp "%PROJECT_ROOT%\math-game\target\classes" demo.MathGame

timeout /t 5 /nobreak > nul

REM Get PID
set PID=
for /f "tokens=1" %%i in ('jps ^| findstr "MathGame"') do set PID=%%i
if "%PID%"=="" (
    echo    [ERROR] MathGame process not found
    pause
    exit /b 1
)
echo    [OK] PID = %PID%
echo.

REM [3/4] Start Arthas
echo [3/4] Starting Arthas and attaching to process...
echo %PID% > "%TEMP%\arthas_select.txt"
start /MIN "Arthas-Server" cmd /c "type %TEMP%\arthas_select.txt | java -jar "%ARTHAS_BOOT%" > nul 2>&1"
timeout /t 10 /nobreak > nul
echo    [OK] Done
echo.

REM [4/4] Run tests
echo [4/4] Running JVM info tests...
echo.
echo ==========================================
echo Test 1: jvm - JVM Runtime Info
echo ==========================================
java -jar "%ARTHAS_CLIENT%" 127.0.0.1 3658 -c "jvm"
echo.
timeout /t 2 /nobreak > nul

echo ==========================================
echo Test 2: perfcounter - JVM Performance Counters
echo ==========================================
java -jar "%ARTHAS_CLIENT%" 127.0.0.1 3658 -c "perfcounter"
echo.
timeout /t 2 /nobreak > nul

echo ==========================================
echo Test 3: memory - Memory Usage Details
echo ==========================================
java -jar "%ARTHAS_CLIENT%" 127.0.0.1 3658 -c "memory"
echo.
timeout /t 2 /nobreak > nul

REM Cleanup
echo ==========================================
echo Cleaning up processes...
echo ==========================================
java -jar "%ARTHAS_CLIENT%" 127.0.0.1 3658 -c "stop" > nul 2>&1
timeout /t 2 /nobreak > nul
taskkill /F /FI "WindowTitle eq MathGame*" > nul 2>&1
echo [OK] Test completed
echo.
pause
