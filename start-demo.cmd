@echo off
echo ====================================================
echo Spring Boot Execution Flow Demo - Startup Script
echo ====================================================
echo.
echo This script attempts to run your Spring Boot application.
echo Make sure you have Maven installed and added to your PATH.
echo.
echo If Maven is not installed, you can:
echo 1. Install Maven from: https://maven.apache.org/download.cgi
echo 2. Or use your IDE (IntelliJ/Eclipse/VS Code) to run the FlowApplication.java
echo 3. Or use Spring Boot CLI: spring run FlowApplication.java
echo.
echo ====================================================

REM Check if Maven is available
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven 'mvn' command not found in PATH
    echo Please install Maven or use your IDE to run the application
    echo.
    echo Alternative: Run FlowApplication.java from your IDE
    pause
    exit /b 1
)

echo Maven found! Starting the application...
echo.

REM Run the Spring Boot application
cd /d "%~dp0"
mvn clean spring-boot:run

pause