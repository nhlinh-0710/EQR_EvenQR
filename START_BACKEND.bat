@echo off
echo ========================================
echo Starting EventQR Backend
echo ========================================

echo.
echo Checking if port 8080 is in use...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Found process using port 8080: %%a
    echo Killing process...
    taskkill /PID %%a /F
)

cd backend
echo.
echo Building project...
call mvn clean package -DskipTests
echo.
echo Starting Spring Boot...
call mvn spring-boot:run
pause

