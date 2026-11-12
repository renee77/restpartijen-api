@echo off
rem Measures test coverage of the shared module (the app) and renders the JaCoCo HTML report.
rem The Kotlin Toolchain runs Maven plugins only for JVM modules, so the server's JaCoCo setup
rem (coverage.bat) does not work for shared, a kmp/lib module. Instead this script attaches the
rem JaCoCo agent through `kotlin.bat test --jvm-args` and renders the report with the JaCoCo CLI.
rem Windows version of coverage-shared.sh; keep both in step.
setlocal
cd /d "%~dp0"

set JACOCO_VERSION=0.8.15
set TOOLS=build\coverage-tools
set EXEC=build\coverage\shared.exec
set REPORT=build\coverage\shared

call :fetch org.jacoco.agent runtime || exit /b 1
call :fetch org.jacoco.cli nodeps || exit /b 1

rem The agent appends to the exec file, so remove the old one; otherwise earlier runs would count too.
if exist %EXEC% del %EXEC%
rem Absolute paths: the test JVM does not start in the project directory.
call kotlin.bat test -m shared --jvm-args="-javaagent:%CD%\%TOOLS%\org.jacoco.agent.jar=destfile=%CD%\%EXEC%"
if errorlevel 1 exit /b 1

java -jar %TOOLS%\org.jacoco.cli.jar report %EXEC% ^
    --classfiles build\tasks\_shared_jarAndroidDebug\shared-jvm.jar ^
    --sourcefiles shared\src ^
    --sourcefiles shared\src@android ^
    --html %REPORT%
if errorlevel 1 exit /b 1
echo Coverage report: %REPORT%\index.html
exit /b 0

rem Downloads a JaCoCo jar from Maven Central once and checks it against the published SHA-1.
rem curl.exe (not the PowerShell alias curl) and certutil ship with Windows 10 and later.
:fetch
set URL=https://repo1.maven.org/maven2/org/jacoco/%1/%JACOCO_VERSION%/%1-%JACOCO_VERSION%-%2.jar
set TARGET=%TOOLS%\%1.jar
if exist %TARGET% exit /b 0
if not exist %TOOLS% mkdir %TOOLS%
curl.exe -sSfL -o %TARGET% %URL%
if errorlevel 1 exit /b 1
set EXPECTED=
for /f %%h in ('curl.exe -sSfL %URL%.sha1') do set EXPECTED=%%h
rem certutil prints a header line, the hash and a status line; only the hash line has no colon.
set ACTUAL=
for /f %%h in ('certutil -hashfile %TARGET% SHA1 ^| findstr /v ":"') do set ACTUAL=%%h
rem /i: certutil's hex case differs between Windows versions.
if /i not "%EXPECTED%"=="%ACTUAL%" (
    del %TARGET%
    echo Checksum mismatch for %1 1>&2
    exit /b 1
)
exit /b 0
