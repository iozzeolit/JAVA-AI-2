# Vosk Speech Recognition Integration

This document explains how to set up and use the speech recognition functionality in your MP3 File Management application.

## Required Libraries

The speech recognition is built using the following libraries:

1. **Vosk** (vosk-0.3.45.jar) - For speech recognition
2. **JSON** (org.json) - For processing Vosk output
3. **JNA** (jna-5.12.1.jar and jna-platform-5.12.1.jar) - Java Native Access required by Vosk
4. **FFmpeg** - For audio conversion and processing

## Setup Instructions

### 1. Libraries

Make sure the following JAR files are in your `lib` directory:
- vosk-0.3.45.jar
- json-20231013.jar
- jna-5.12.1.jar
- jna-platform-5.12.1.jar

If any of these libraries are missing, you can run:
- `download-json-lib.bat` to download the JSON library
- `download-jna-lib.bat` to download the JNA libraries

### 2. FFmpeg Installation

FFmpeg must be installed on your system and available in the PATH.
- The application will attempt to automatically download and set up FFmpeg if not found
- If automatic setup fails, follow the manual installation instructions provided in the application

### 3. Speech Models

On first use, the application will:
- Detect that no model is installed
- Ask permission to download a speech recognition model (~40MB)
- Download and extract the model to the `vosk-model` directory

#### Using Multiple Language Models

The application now supports using multiple language models for transcription in different languages:

1. **Language Selection**:
   - Click the "Select Language" button in the file list window
   - Choose from available languages in the dropdown
   - If a model for the selected language isn't installed, the application will offer to download it

2. **Manual Model Installation**:
   - Create a `vosk-models` directory in the application folder
   - Inside this directory, create subdirectories for each language using their language code:
     - English: `en`
     - Japanese: `ja`
     - Vietnamese: `vi`
     - French: `fr`
     - German: `de`
     - Spanish: `es`
     - Chinese: `zh`
     - And others...
   - Download language models from https://alphacephei.com/vosk/models/
   - Extract the language model files into the corresponding language subdirectory
   - For example:
     - For English: `vosk-models/en/` containing the model files
     - For Japanese: `vosk-models/ja/` containing the model files

3. **Language Preference**:
   - Your selected language preference is saved in `config.properties`
   - The application will remember your last used language
   - When analyzing new files, you can choose to use the current language or select a different one

4. **Supported Languages**:
   The application supports many languages including but not limited to:
   - English (en)
   - Japanese (ja)
   - Vietnamese (vi)
   - French (fr)
   - German (de)
   - Spanish (es)
   - Russian (ru)
   - Chinese (zh)
   - Portuguese (pt)
   - Italian (it)
   - Dutch (nl)
   
   Note: The availability and quality of models may vary by language.
   
When using the application, you can now specify which language model to use:
- Use the `LanguageModelManager.showLanguageSelector()` to select a language
- Or programmatically set the language: `speechRecognitionService.setLanguage("ja")`

## Using Speech Recognition

1. In the file list window, select an MP3 file
2. Click "Analyze Selected"
3. You'll be prompted to use the current language or select a different one
4. The application will:
   - Process the audio file
   - Extract sentences with timestamps
   - Save them to the database
   - Display results in a table

4. After analysis, you can:
   - View all extracted sentences
   - Click "Edit Sentences" to modify text and timestamps
   - Add new sentences manually
   - Delete incorrect sentences

## Troubleshooting

If you encounter issues:

1. **Model download fails**:
   - Download the model manually from https://alphacephei.com/vosk/models/
   - For single language usage, extract it to a folder named "vosk-model" in the application directory
   - For multiple languages, extract to "vosk-models/[language-code]/" (e.g., "vosk-models/en/")
   - Ensure proper permissions for the application to write to these directories

2. **Language selection issues**:
   - If language selection isn't working, check that config.properties is writable
   - Verify the language model directory exists with the correct structure
   - Make sure the language model files are correctly extracted (should include conf/model files)

3. **Audio conversion fails**:
   - Ensure FFmpeg is installed and in your PATH
   - Try converting the MP3 file to WAV format manually before analysis
   - Check if the MP3 file is not corrupted by playing it outside the application

4. **Recognition quality is poor**:
   - Try using a different language model (some models perform better than others)
   - Make sure you're using the correct language model for the audio content
   - Ensure audio quality is good (minimal background noise)
   - Use the sentence editor to correct mistakes

5. **Memory issues during analysis**:
   - Some language models require more memory
   - Try running with the -Xmx2g parameter to allow more memory (edit run.bat)
   - Close other applications to free up system memory

## Note on Performance

- Processing large audio files may take time
- The application processes audio at approximately 2-3x real-time speed
- For very long files, please be patient during analysis
