@echo off

REM Create build directory if it doesn't exist
if not exist "build" mkdir build

REM Clean up any existing class files in the build directory
if exist "build\*.class" del /Q "build\*.class"
if exist "build\Front\*.class" del /Q "build\Front\*.class" 
if exist "build\Back\*.class" del /Q "build\Back\*.class"
if exist "build\External\*.class" del /Q "build\External\*.class"
if exist "build\Object\*.class" del /Q "build\Object\*.class"

REM Create subdirectories in build to match package structure
if not exist "build\Front" mkdir "build\Front"
if not exist "build\Back" mkdir "build\Back"
if not exist "build\External" mkdir "build\External"
if not exist "build\Object" mkdir "build\Object"
if not exist "build\AI" mkdir "build\AI"

REM Compile all Java files and put class files in build directory
echo Compiling Java files
javac -d build -cp ".;lib/*" AI\UnicodeHelper.java
javac -d build -cp ".;build;lib/*" External\*.java
javac -d build -cp ".;build;lib/*" AI\SpeechRecognitionService.java AI\LanguageModelManager.java
javac -d build -cp ".;build;lib/*" Object\*.java Back\*.java Front\*.java *.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed with error code %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)

echo Starting application...

java -Xmx1G ^
     -cp "build;.;lib/*" ^
     Main