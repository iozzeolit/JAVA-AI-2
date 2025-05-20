@echo off
REM Script to package all compiled files, libraries, and resources into a single JAR file

REM Define variables
set JAR_NAME=project.jar
set BUILD_DIR=build
set OUTPUT_DIR=dist
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

REM Create output directory if it doesn't exist
if not exist %OUTPUT_DIR% mkdir %OUTPUT_DIR%

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
jar cfm %OUTPUT_DIR%\%JAR_NAME% %MANIFEST_FILE% -C %BUILD_DIR% .

REM Verify that all required libraries exist
echo Verifying required libraries...
set MISSING_LIBS=0
if not exist %LIB_DIR%\mssql-jdbc-12.10.0.jre11.jar (
    echo ERROR: Missing MSSQL JDBC driver. Download from Microsoft and place in the lib directory.
    set MISSING_LIBS=1
)
if not exist %LIB_DIR%\json-20231013.jar (
    echo ERROR: Missing JSON library. Download from maven repository and place in the lib directory.
    set MISSING_LIBS=1
)
if not exist %LIB_DIR%\jna-5.12.1.jar (
    echo ERROR: Missing JNA library. Download from maven repository and place in the lib directory.
    set MISSING_LIBS=1
)
if not exist %LIB_DIR%\jna-platform-5.12.1.jar (
    echo ERROR: Missing JNA Platform library. Download from maven repository and place in the lib directory.
    set MISSING_LIBS=1
)
if not exist %LIB_DIR%\vosk-0.3.45.jar (
    echo ERROR: Missing VOSK library. Download from VOSK website and place in the lib directory.
    set MISSING_LIBS=1
)

if %MISSING_LIBS%==1 (
    echo Some required libraries are missing. Please install them and try again.
    pause
    exit /b 1
)

REM Include libraries in the JAR, ensuring database driver is included first
echo Including database driver and other libraries...
if not exist %OUTPUT_DIR%\lib mkdir %OUTPUT_DIR%\lib
copy %LIB_DIR%\* %OUTPUT_DIR%\lib\ >nul
jar uf %OUTPUT_DIR%\%JAR_NAME% -C %LIB_DIR% .
echo Adding SQL JDBC driver directly to root of JAR...
jar uf %OUTPUT_DIR%\%JAR_NAME% -C %LIB_DIR% mssql-jdbc-12.10.0.jre11.jar

REM Copy vosk-models directory to output (dist) folder so models are available as real files/folders
if exist %VOSK_MODELS_DIR% (
    echo Copying vosk-models directory to output folder...
    xcopy /E /I /Y %VOSK_MODELS_DIR% %OUTPUT_DIR%\vosk-models >nul
) else (
    echo WARNING: vosk-models directory not found. Please ensure models are available in dist/vosk-models.
)

REM Include config.properties in the JAR file
if exist config.properties (
    echo Including config.properties...
    jar uf %OUTPUT_DIR%\%JAR_NAME% -C . config.properties
) else (
    echo config.properties not found. Ensure it is available for database configuration.
)

REM Copy config.properties to the output (dist) folder
if exist config.properties (
    echo Copying config.properties to output folder...
    copy config.properties %OUTPUT_DIR% >nul
) else (
    echo WARNING: config.properties not found. Please ensure it is available in the dist folder.
)

REM Include DLL files both in the JAR and copy to output directory for runtime access
echo Handling DLL files for database connectivity...
if exist %LIB_DIR%\mssql-jdbc_auth-12.10.0.x64.dll (
    echo Including DLL files in the output directory and in the JAR...
    copy %LIB_DIR%\mssql-jdbc_auth-12.10.0.x64.dll %OUTPUT_DIR% >nul
    copy %LIB_DIR%\mssql-jdbc_auth-12.10.0.x64.dll %OUTPUT_DIR%\lib\ >nul
    jar uf %OUTPUT_DIR%\%JAR_NAME% -C %LIB_DIR% mssql-jdbc_auth-12.10.0.x64.dll
) else (
    echo WARNING: DLL file not found in %LIB_DIR%. The application may not be able to connect to the database.
)

REM Add logging properties to the JAR file
if exist logging.properties (
    echo Including logging.properties...
    jar uf %OUTPUT_DIR%\%JAR_NAME% -C . logging.properties
) else (
    echo logging.properties not found. Consider adding it for proper logging configuration.
)

REM Include any additional configuration files that might be needed
if exist data (
    echo Including data directory...
    jar uf %OUTPUT_DIR%\%JAR_NAME% -C . data
)

REM Copy README template to output directory with UTF-8 encoding preserved
echo Copying README.txt to output folder with proper Vietnamese encoding...
if exist README-template.txt (
    powershell -Command "& {Get-Content 'README-template.txt' -Encoding UTF8 | Set-Content -Encoding UTF8 '%OUTPUT_DIR%\README.txt'}" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo WARNING: PowerShell copy failed, trying regular copy method...
        copy README-template.txt %OUTPUT_DIR%\README.txt >nul
    )
) else (
    echo ERROR: README-template.txt not found. Cannot create README.txt in output folder.
)

REM Copy ffmpeg folder to output (dist) folder so executables are available
if exist ffmpeg (
    echo Copying ffmpeg folder to output folder...
    xcopy /E /I /Y ffmpeg %OUTPUT_DIR%\ffmpeg >nul
) else (
    echo WARNING: ffmpeg folder not found. Please ensure ffmpeg executables are available in dist/ffmpeg.
)

REM Remove any .tmp files in the output (dist) folder
if exist %OUTPUT_DIR%\*.tmp (
    echo Removing .tmp files from output folder...
    del /Q %OUTPUT_DIR%\*.tmp
)

echo JAR file created: %OUTPUT_DIR%\%JAR_NAME%
echo All components have been successfully packaged into %OUTPUT_DIR%