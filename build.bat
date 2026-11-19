@echo off
echo ===========================================================================
echo   Building Assignment 3 Core Application Suite
echo ===========================================================================

if not exist bin mkdir bin

echo Compiling Java source files...
javac -d bin -sourcepath src src\assignment3\core\MainApplication.java src\assignment3\core\atm\*.java src\assignment3\core\atm\exceptions\*.java src\assignment3\core\exam\*.java src\assignment3\core\exam\controller\*.java src\assignment3\core\exam\service\*.java src\assignment3\core\exam\storage\*.java src\assignment3\core\exam\exceptions\*.java src\assignment3\core\sales\*.java src\assignment3\core\scanner\*.java src\assignment3\core\cinema\*.java src\assignment3\core\cinema\booking\*.java src\assignment3\core\wallet\*.java src\assignment3\core\wallet\transfer\*.java src\assignment3\core\wallet\mess\*.java src\assignment3\core\results\*.java src\assignment3\core\util\*.java

if %ERRORLEVEL% equ 0 (
    echo [BUILD SUCCESS] All classes compiled to bin\
) else (
    echo [BUILD ERROR] Compilation failed with error code %ERRORLEVEL%
)
