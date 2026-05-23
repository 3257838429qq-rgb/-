@echo off
echo ========================================
echo   Hotel Management System - Starting...
echo ========================================

echo.
echo [1/2] Starting Backend (Spring Boot)...

start "Hotel-Backend" cmd /c "cd /d %~dp0backend && mvn spring-boot:run"

echo Waiting for backend to be ready...
:wait
timeout /t 3 /nobreak >nul
netstat -ano 2>nul | findstr ":8080" | findstr "LISTENING" >nul
if %errorlevel% neq 0 goto wait

echo Backend is ready!

echo.
echo [2/2] Starting Frontend (Vite)...

start "Hotel-Frontend" cmd /c "cd /d %~dp0frontend && npm run dev"

echo.
echo ========================================
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:3000
echo   Login:    admin / 123456
echo ========================================
echo.
echo Close the two new windows to stop services.
pause
