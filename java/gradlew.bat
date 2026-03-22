@rem Gradle wrapper for Windows
@rem Usage: .\gradlew.bat clean test

@if "%DEBUG%"=="" @echo off
setlocal enabledelayedexpansion

set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "REAL_JAR_URL=https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar"

@rem ── Auto-download real wrapper JAR if stub is present ─────────────────────
if exist "%WRAPPER_JAR%" (
    for %%A in ("%WRAPPER_JAR%") do set "JAR_SIZE=%%~zA"
    if !JAR_SIZE! LSS 10000 (
        echo Gradle wrapper JAR is a stub. Downloading real jar...
        powershell -NoProfile -Command ^
            "[Net.ServicePointManager]::SecurityProtocol='Tls12';" ^
            "(New-Object Net.WebClient).DownloadFile('%REAL_JAR_URL%','%WRAPPER_JAR%')"
        if !errorlevel! neq 0 (
            echo Download failed. Run: powershell -ExecutionPolicy Bypass -File setup-gradle-wrapper.ps1
            exit /b 1
        )
        echo Downloaded OK.
    )
)

@rem ── Find java.exe — check JAVA_HOME, then PATH, then common install dirs ──
set "JAVA_EXE="

@rem 1. Use JAVA_HOME if it points to a real JDK
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    )
)

@rem 2. Fall back to java.exe on PATH
if not defined JAVA_EXE (
    where java.exe >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "delims=" %%i in ('where java.exe') do (
            if not defined JAVA_EXE set "JAVA_EXE=%%i"
        )
    )
)

@rem 3. Scan common install locations
if not defined JAVA_EXE (
    for %%D in (
        "%ProgramFiles%\Eclipse Adoptium"
        "%ProgramFiles%\Microsoft"
        "%ProgramFiles%\Java"
        "%ProgramFiles%\BellSoft"
        "%ProgramFiles%\Amazon Corretto"
    ) do (
        if not defined JAVA_EXE (
            for /d %%J in ("%%~D\jdk*") do (
                if not defined JAVA_EXE (
                    if exist "%%~J\bin\java.exe" (
                        set "JAVA_EXE=%%~J\bin\java.exe"
                        set "JAVA_HOME=%%~J"
                    )
                )
            )
        )
    )
)

if not defined JAVA_EXE (
    echo.
    echo ERROR: Cannot find java.exe.
    echo.
    echo Fix options:
    echo   1. Set JAVA_HOME:  $env:JAVA_HOME = "C:\Path\To\Your\JDK"
    echo   2. Add java to PATH: $env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
    echo   3. Download JDK 22 from: https://adoptium.net
    echo.
    exit /b 1
)

echo Using Java: %JAVA_EXE%
"%JAVA_EXE%" "-Dorg.gradle.appname=gradlew" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*