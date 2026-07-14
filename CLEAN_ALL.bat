@echo off
echo ========================================
echo Clean All Processes and Files
echo ========================================
echo.

echo Killing all Java processes...
taskkill /F /IM java.exe /T 2>nul
echo.

echo Killing processes on port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 2^>nul') do (
    taskkill /PID %%a /F 2>nul
)
echo.

echo Cleaning target directory...
cd backend
if exist target (
    rmdir /s /q target
    echo Target directory removed.
)
echo.

echo Cleaning Maven cache...
call mvn clean

echo.
echo ========================================
echo Done! All cleaned up.
echo ========================================
echo.
pause

