@echo off
REM Script to download and install FFmpeg into the ffmpeg/ folder

REM Define variables
set FFMPEG_URL=https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
set FFMPEG_ZIP=ffmpeg-release-essentials.zip
set FFMPEG_DIR=ffmpeg

REM Create ffmpeg directory if it doesn't exist
if not exist %FFMPEG_DIR% mkdir %FFMPEG_DIR%

REM Download FFmpeg zip file
if exist %FFMPEG_ZIP% del %FFMPEG_ZIP%
echo Downloading FFmpeg...
powershell -Command "Invoke-WebRequest -Uri %FFMPEG_URL% -OutFile %FFMPEG_ZIP%"

REM Check if download was successful
if not exist %FFMPEG_ZIP% (
    echo ERROR: Failed to download FFmpeg.
    exit /b 1
)

REM Extract FFmpeg zip file
echo Extracting FFmpeg...
powershell -Command "Expand-Archive -Path %FFMPEG_ZIP% -DestinationPath %FFMPEG_DIR% -Force"

REM Move FFmpeg executables to the ffmpeg/ folder
for /d %%D in (%FFMPEG_DIR%\ffmpeg-*) do move "%%D\bin\*" %FFMPEG_DIR% >nul

REM Clean up
if exist %FFMPEG_ZIP% del %FFMPEG_ZIP%
for /d %%D in (%FFMPEG_DIR%\ffmpeg-*) do rmdir /S /Q "%%D"

echo FFmpeg installation completed successfully.
