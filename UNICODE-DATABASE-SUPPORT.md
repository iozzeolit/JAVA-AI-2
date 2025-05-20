# Unicode Support in Database and UI

This document provides detailed information about how Unicode (UTF-8) text is handled in the system, especially for Japanese and other non-ASCII languages.

## Overview

The system now properly handles Unicode text throughout the entire pipeline:
1. Text recognition from audio files
2. Text normalization for consistent encoding
3. Storage in the database
4. Display in the user interface

## Implementation Details

### Text Recognition

When the speech recognition system processes audio files, it follows these steps:

1. Convert the audio file to the right format using FFmpeg
2. Process the audio with Vosk speech recognition
3. Extract words and timestamps from the recognition results
4. Group words into sentences
5. Normalize any non-ASCII text to ensure proper Unicode encoding

### Unicode Normalization

For non-ASCII text (Japanese, Chinese, Vietnamese, etc.), additional processing is performed:

```java
// Convert to proper Unicode representation for database storage
String normalizedText = new String(wordText.getBytes("UTF-8"), "UTF-8");
```

This ensures that the text is properly normalized for UTF-8 encoding, which is essential for:
- Consistent storage in the database
- Proper display in the user interface
- Avoiding garbled characters

### Debugging Tools

The application provides detailed debugging output for Unicode text:

1. Unicode codepoint display: Shows the Unicode codepoint for each character
   ```
   Unicode characters: 世(U+4E16) 界(U+754C)
   ```

2. Hex byte representation: Shows the raw UTF-8 bytes for the text
   ```
   ja word hex bytes: E4 B8 96 E7 95 8C
   ```

3. Unicode normalization: Shows any changes made during Unicode normalization
   ```
   Text normalized for ja: [世界] -> [世界]
   ```

## Database Considerations

When working with the database, ensure that:

1. The database is configured to use UTF-8 or UTF-16 for text storage
2. The JDBC connection string includes the proper encoding parameters
3. Any queries handling non-ASCII text use prepared statements with proper parameter binding

## Testing Unicode Support

To test Unicode support:

1. Run the provided UTF-8 test utility:
   ```
   java -Dfile.encoding=UTF-8 UTF8Test
   ```

2. Process audio files in different languages and check the database output

3. Verify text displays correctly in the user interface

## Troubleshooting

If you encounter garbled characters:

1. Check the console output for Unicode debugging information
2. Verify the JVM is running with `-Dfile.encoding=UTF-8`
3. Check the database connection encoding
4. Use the `UnicodeHelper` class to diagnose encoding issues:
   ```java
   UnicodeHelper.displayEncodingInfo();
   UnicodeHelper.testEncodingConversions("世界");
   ```

## Language Models

The application supports the following language models:

- English (en) - Default
- Japanese (ja)
- Chinese (zh)
- Vietnamese (vi)
- Russian (ru)
- French (fr)
- German (de)
- Spanish (es)

Each language model requires downloading the appropriate Vosk model files.
