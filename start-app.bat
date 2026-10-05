@echo off
setlocal

set "APP_EXE=%~dp0dist\LibraryManagement\LibraryManagement.exe"

if not exist "%APP_EXE%" (
    echo LibraryManagement.exe was not found.
    echo Rebuild the application package before starting it.
    exit /b 1
)

start "" "%APP_EXE%"

endlocal
