@echo off
echo ========================================
echo Killing Process on Port 8080
echo ========================================
echo.

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Found process: %%a
    taskkill /PID %%a /F
    echo Process killed!
)

echo.
echo Done! Port 8080 is now free.
echo.
pause

