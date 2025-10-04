@echo off
title Arthas Demo - Complete Workflow
color 0A

echo.
echo ========================================
echo    Arthas Complete Demo Workflow
echo ========================================
echo.

REM Check prerequisites
echo [CHECK] Verifying prerequisites...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found in PATH!
    echo Please install Java and add it to PATH.
    pause
    exit /b 1
)

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found in PATH!
    echo Please install Maven and add it to PATH.
    pause
    exit /b 1
)

echo [OK] Java and Maven are available.
echo.

REM Step 1: Compile
echo [STEP 1] Compiling MathGame...
cd math-game
call mvn compile -q
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed!
    cd ..
    pause
    exit /b 1
)
cd ..
echo [OK] MathGame compiled successfully.
echo.

REM Step 2: Download Arthas if needed
if not exist "arthas-boot.jar" (
    echo [STEP 2] Downloading Arthas boot jar...
    powershell -Command "try { Invoke-WebRequest -Uri 'https://arthas.aliyun.com/arthas-boot.jar' -OutFile 'arthas-boot.jar' -UseBasicParsing } catch { exit 1 }"
    if %errorlevel% neq 0 (
        echo ERROR: Failed to download arthas-boot.jar
        echo Please download manually from: https://arthas.aliyun.com/arthas-boot.jar
        pause
        exit /b 1
    )
    echo [OK] Arthas boot jar downloaded.
) else (
    echo [STEP 2] Arthas boot jar already exists.
)
echo.

REM Step 3: Start MathGame
echo [STEP 3] Starting MathGame application...
start "MathGame-Demo" cmd /k "java -cp math-game\target\classes demo.MathGame"
echo [OK] MathGame started in new window.
echo.

REM Step 4: Wait and show processes
echo [STEP 4] Waiting for application to initialize...
timeout /t 5 /nobreak >nul

echo Current Java processes:
jps -l | findstr /C:"demo.MathGame"
if %errorlevel% neq 0 (
    echo WARNING: MathGame process not found yet. Waiting longer...
    timeout /t 3 /nobreak >nul
    jps -l
)
echo.

REM Step 5: Instructions
echo [STEP 5] Ready to connect Arthas!
echo ========================================
echo.
echo NEXT STEPS:
echo 1. Run this command to connect Arthas:
echo    java -jar arthas-boot.jar
echo.
echo 2. Select the MathGame process from the list
echo.
echo 3. Common commands:
echo    dashboard              # System overview panel
echo    thread                 # Thread information
echo    jvm                    # JVM details
echo    memory                 # Memory information
echo    help                   # All available commands
echo.

echo.
echo ========================================
echo Press any key to launch Arthas automatically...
pause >nul

echo.
echo [LAUNCHING] Starting Arthas...
java -jar arthas-boot.jar