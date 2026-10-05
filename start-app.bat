@echo off
setlocal

title Library Management System

cd /d "%~dp0"

echo ========================================
echo      LIBRARY MANAGEMENT SYSTEM
echo ========================================
echo.
echo Starting application...
echo.

call library-management-backend\mvnw.cmd ^
    -f library-management-backend\pom.xml ^
    org.codehaus.mojo:exec-maven-plugin:3.5.0:java ^
    -Dexec.mainClass=com.library.management.Main ^
    -Dexec.classpathScope=runtime

echo.
echo ========================================
echo      APPLICATION CLOSED
echo ========================================
echo.

endlocal