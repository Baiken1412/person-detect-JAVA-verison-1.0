@echo off
cls
echo =====================================
echo    License Management Tool
echo =====================================
echo.
echo Please select:
echo [1] Get Machine Code
echo [2] Generate License
echo [3] Validate License
echo [0] Exit
echo.
set /p choice=Enter your choice (0-3):

if "%choice%"=="1" goto GetCode
if "%choice%"=="2" goto GenLicense
if "%choice%"=="3" goto ValidateLicense
if "%choice%"=="0" goto End
goto Menu

:GetCode
echo.
echo =====================================
echo Getting Machine Code...
echo =====================================
mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.GetMachineCode" -Dexec.cleanupDaemonThreads=false -q
echo.
pause
goto Menu

:GenLicense
echo.
echo =====================================
echo Generating License...
echo =====================================
mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.LicenseGenerator" -Dexec.cleanupDaemonThreads=false -q
echo.
pause
goto Menu

:ValidateLicense
echo.
echo =====================================
echo Validating License...
echo =====================================
mvn exec:java -Dexec.mainClass="com.ruoyi.common.license.LicenseValidator" -Dexec.cleanupDaemonThreads=false -q
echo.
pause
goto Menu

:Menu
cls
goto :eof

:End
echo Goodbye!
