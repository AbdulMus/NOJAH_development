@echo off
setlocal enabledelayedexpansion

echo Cleaning and building the project...
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Build failed!
    exit /b %ERRORLEVEL%
)

echo Build successful!

:: Find the generated JAR file (excluding "original-*.jar")
for /F "delims=" %%F in ('dir /b /s target\*.jar ^| findstr /v "original"') do set JAR_FILE=%%F

if not defined JAR_FILE (
    echo Error: JAR file not found!
    exit /b 1
)

echo JAR file found: %JAR_FILE%

:: Start the application
echo Starting the application...
start "" java -jar "%JAR_FILE%"

:: Wait for the server to start (check every 2 seconds)
echo Waiting for the server to start at http://localhost:8080...
for /l %%i in (1,1,30) do (
    curl -s http://localhost:8080 >nul
    if %ERRORLEVEL% equ 0 (
        echo Server started successfully!
        echo Opening application in browser...
        start "" "http://localhost:8080"
        exit /b
    )
    timeout /t 2 >nul
)

echo Server failed to start within the timeout period.
exit /b
