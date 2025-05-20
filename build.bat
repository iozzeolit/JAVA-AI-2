@echo off
REM Script to package all compiled files, libraries, and resources into a single JAR file

REM Define variables
set JAR_NAME=RunMe.jar
set BUILD_DIR=build
set MANIFEST_FILE=MANIFEST.MF
set LIB_DIR=lib
set VOSK_MODELS_DIR=vosk-models

REM Clean and prepare the build directory
if exist "%BUILD_DIR%" rmdir /S /Q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"

REM Compile all Java files
javac -d %BUILD_DIR% -cp ".;%LIB_DIR%/*" AI\UnicodeHelper.java
javac -d %BUILD_DIR% -cp ".;%BUILD_DIR%;%LIB_DIR%/*" External\*.java
javac -d %BUILD_DIR% -cp ".;%BUILD_DIR%;%LIB_DIR%/*" AI\SpeechRecognitionService.java AI\LanguageModelManager.java
javac -d %BUILD_DIR% -cp ".;%BUILD_DIR%;%LIB_DIR%/*" Object\*.java Back\*.java Front\*.java *.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)

REM Create comprehensive MANIFEST.MF with all necessary configuration and JVM options
echo Creating comprehensive MANIFEST.MF with Main-Class, JVM options, and all required libraries...
(
    echo Manifest-Version: 1.0
    (echo Main-Class: Main)
    echo Class-Path: . lib/* mssql-jdbc-12.10.0.jre11.jar json-20231013.jar jna-5.12.1.jar jna-platform-5.12.1.jar vosk-0.3.45.jar ./lib/mssql-jdbc-12.10.0.jre11.jar ./lib/json-20231013.jar ./lib/jna-5.12.1.jar ./lib/jna-platform-5.12.1.jar ./lib/vosk-0.3.45.jar
    echo Add-Opens: java.base/java.lang java.base/java.util
    echo Add-Exports: java.base/java.lang java.base/java.util
) > %MANIFEST_FILE%

REM Create JAR file with all components
echo Creating JAR file with all components...
jar cfm %JAR_NAME% %MANIFEST_FILE% -C %BUILD_DIR% .


REM Remove any .tmp files in the output (dist) folder
if exist *.tmp (
    echo Removing .tmp files from output folder...
    del /Q *.tmp
)

echo JAR file created: %JAR_NAME%