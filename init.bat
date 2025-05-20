@echo off

javac -cp ".;lib/*" *.java

IF %ERRORLEVEL% NEQ 0 (
    exit /b %ERRORLEVEL%
)

java -Djava.library.path=lib -cp ".;lib/*" Init