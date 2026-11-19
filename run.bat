@echo off
if not exist bin\assignment3\core\MainApplication.class (
    echo Binaries not found. Invoking build.bat first...
    call build.bat
)

if "%1"=="" (
    java -cp bin assignment3.core.MainApplication
) else (
    java -cp bin assignment3.core.MainApplication %*
)
