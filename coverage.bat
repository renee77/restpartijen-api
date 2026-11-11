@echo off
rem Measures test coverage of the server (NFR-03) and renders the JaCoCo HTML report.
rem The JaCoCo agent appends to jacoco.exec on every test run, so the old file is removed
rem first; otherwise the report would also count earlier runs.
setlocal
cd /d "%~dp0"
if exist build\maven-target\jacoco.exec del build\maven-target\jacoco.exec
rem The report task runs the server tests itself first.
call kotlin.bat task :server:jacoco-maven-plugin.report
if errorlevel 1 exit /b 1
echo Coverage report: build\maven-target\reports\jacoco\index.html
