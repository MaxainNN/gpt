@echo off
REM GPT Programming Assistant - Deployment Script (Windows)
REM Script for automatic application deployment

setlocal enabledelayedexpansion

REM Use text labels
set "INFO=[INFO]"
set "SUCCESS=[SUCCESS]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"

REM Directories
set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."

REM Default command
if "%1"=="" (
    set "COMMAND=deploy"
) else (
    set "COMMAND=%1"
)

REM Command processing
if "%COMMAND%"=="deploy" goto :deploy
if "%COMMAND%"=="start" goto :start
if "%COMMAND%"=="stop" goto :stop
if "%COMMAND%"=="build" goto :build
if "%COMMAND%"=="docker" goto :docker
if "%COMMAND%"=="status" goto :status
if "%COMMAND%"=="help" goto :help
if "%COMMAND%"=="--help" goto :help
if "%COMMAND%"=="-h" goto :help

echo %ERROR% Unknown command: %COMMAND%
goto :help

:deploy
echo %INFO% Starting full deployment...
call :check_dependencies
if errorlevel 1 exit /b 1
call :check_env
if errorlevel 1 exit /b 1
call :start_docker
call :build_project
if errorlevel 1 exit /b 1
call :start_app
goto :eof

:start
echo %INFO% Starting application...
call :check_env
if errorlevel 1 exit /b 1
call :start_docker
call :start_app
goto :eof

:stop
echo %INFO% Stopping all services...
cd /d "%PROJECT_DIR%"
docker-compose down 2>nul
taskkill /F /IM java.exe 2>nul
echo %SUCCESS% All services stopped
goto :eof

:build
call :build_project
goto :eof

:docker
call :start_docker
goto :eof

:status
echo %INFO% Checking services status...
echo.
echo Docker containers:
cd /d "%PROJECT_DIR%"
docker-compose ps 2>nul || echo Docker Compose not running
echo.
echo Application health:
curl -s http://localhost:8080/actuator/health 2>nul || echo Application not running
echo.
goto :eof

:help
echo GPT Programming Assistant - Deployment Script (Windows)
echo.
echo Usage: %~nx0 [command]
echo.
echo Commands:
echo   deploy    - Full deployment (docker + build + run)
echo   start     - Start application (assumes already built)
echo   stop      - Stop all services
echo   build     - Build project only
echo   docker    - Start Docker containers only
echo   status    - Check services status
echo   help      - Show this help
echo.
goto :eof

REM === Helper Functions ===

:check_dependencies
echo %INFO% Checking dependencies...

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Java is not installed. Please install JDK 21+
    exit /b 1
)
echo %SUCCESS% Java found

REM Check Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Docker is not installed. Please install Docker Desktop
    exit /b 1
)
echo %SUCCESS% Docker found

REM Check Docker Compose
docker-compose --version >nul 2>&1
if errorlevel 1 (
    docker compose version >nul 2>&1
    if errorlevel 1 (
        echo %ERROR% Docker Compose is not installed
        exit /b 1
    )
)
echo %SUCCESS% Docker Compose found
exit /b 0

:check_env
echo %INFO% Checking environment configuration...

if not exist "%PROJECT_DIR%\.env" (
    echo %WARNING% .env file not found. Creating template...
    (
        echo # OpenAI API Key ^(required^)
        echo OPENAI_API_KEY=sk-proj-your-api-key-here
        echo.
        echo # API Key Authentication ^(optional^)
        echo API_KEY_ENABLED=false
        echo API_KEY=your-api-key-here
        echo.
        echo # Rate Limiting ^(optional^)
        echo RATE_LIMIT_RPM=60
    ) > "%PROJECT_DIR%\.env"
    echo %WARNING% Please edit .env file and add your OPENAI_API_KEY
    exit /b 1
)

findstr /C:"sk-proj-your-api-key-here" "%PROJECT_DIR%\.env" >nul 2>&1
if not errorlevel 1 (
    echo %ERROR% Please set your OPENAI_API_KEY in .env file
    exit /b 1
)

echo %SUCCESS% Environment configured
exit /b 0

:start_docker
echo %INFO% Starting Docker containers...
cd /d "%PROJECT_DIR%"
docker-compose up -d

echo %INFO% Waiting for ChromaDB to be ready...
timeout /t 5 /nobreak >nul

curl -s http://localhost:8000/api/v1/heartbeat >nul 2>&1
if not errorlevel 1 (
    echo %SUCCESS% ChromaDB is running
) else (
    echo %WARNING% ChromaDB may not be ready yet. Continuing...
)
exit /b 0

:build_project
echo %INFO% Building project...
cd /d "%PROJECT_DIR%"

if exist "mvnw.cmd" (
    call mvnw.cmd clean package -DskipTests
) else (
    mvn clean package -DskipTests
)

if errorlevel 1 (
    echo %ERROR% Build failed
    exit /b 1
)

echo %SUCCESS% Project built successfully
exit /b 0

:start_app
echo %INFO% Starting application...
cd /d "%PROJECT_DIR%"

REM Найти JAR файл
for /f "delims=" %%i in ('dir /b /s target\*.jar 2^>nul ^| findstr /v sources ^| findstr /v original') do set "JAR_FILE=%%i"

if not defined JAR_FILE (
    echo %ERROR% JAR file not found. Run build first.
    exit /b 1
)

echo %INFO% Starting %JAR_FILE%...
start "GPT Assistant" java -jar "%JAR_FILE%"

echo %INFO% Waiting for application to start...
timeout /t 10 /nobreak >nul

curl -s http://localhost:8080/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo %SUCCESS% Application is running on http://localhost:8080
    echo %INFO% Swagger UI: http://localhost:8080/swagger-ui.html
    echo %INFO% Health: http://localhost:8080/actuator/health
    echo %INFO% Metrics: http://localhost:8080/actuator/prometheus
) else (
    echo %WARNING% Application may still be starting...
)

echo.
echo Application started in a new window.
echo Close that window or run '%~nx0 stop' to stop the application.
exit /b 0
