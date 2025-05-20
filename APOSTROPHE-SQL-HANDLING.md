# SQL Apostrophe Handling

This document explains the handling of apostrophes in SQL strings and how we've fixed related issues.

## Problem Description

When text containing apostrophes (single quotes) is inserted into SQL queries without proper escaping,
it can cause SQL syntax errors. This happens because apostrophes are used to delimit string literals 
in SQL queries. Speech recognition results often contain contractions and quoted speech with apostrophes, 
which need special handling for SQL operations.

For example, if we have a sentence like:
```
It's not working properly
```

When used in an SQL query without escaping:
```sql
INSERT INTO Sentences VALUES (1, 'It's not working properly')
```

This will result in a SQL syntax error because the apostrophe in "It's" is interpreted as the end of the string.

## Solution

### 1. QPiece Class Modification

The `QPiece` class has been modified to automatically escape apostrophes by doubling them:

```java
public static String conv(String val) {
    if (val == null) return "NULL";
    // Escape apostrophes by doubling them for SQL
    String escaped = val.replace("'", "''");
    return "'" + escaped + "'";
}

public static String convN(String val) {
    if (val == null) return "NULL";
    // Escape apostrophes by doubling them for SQL
    String escaped = val.replace("'", "''");
    return "N'" + escaped + "'";
}
```

This ensures that any apostrophes in text content are properly escaped when converted to SQL strings.

### 2. UnicodeHelper Class Addition

Added a utility method to the `UnicodeHelper` class to handle apostrophes:

```java
/**
 * Escape apostrophes (single quotes) for SQL by doubling them
 * @param text Input text which may contain apostrophes
 * @return Text with apostrophes escaped for SQL
 */
public static String escapeSqlApostrophes(String text) {
    if (text == null || text.isEmpty()) {
        return text;
    }
    
    return text.replace("'", "''");
}
```

### 3. Speech Recognition Service Updates

Modified the `SpeechRecognitionService` class to detect and log text containing apostrophes:

- Added apostrophe detection in `processResult()` method
- Added apostrophe detection in `extractSentencesFromAudio()` method
- Improved logging to show when apostrophes are detected

## Testing

Created test classes to verify the apostrophe handling:

1. **ApostropheTest.java**: Tests the basic apostrophe escaping functionality
   ```java
   String normalText = "Hello world";
   String textWithApostrophe = "It's a test";
   String textWithMultipleApostrophes = "She said, 'It's not working.'";
   ```

2. **TestApostropheSpeechRecognition.java**: Tests apostrophe handling in speech recognition context
   ```java
   // Test creating Sentence objects with apostrophes
   Sentence sentence = new Sentence();
   sentence.id = 1;
   sentence.recordFileId = 1;
   sentence.content = "It's working properly now.";
   System.out.println("toString(): " + sentence.toString());
   ```

These tests verify that apostrophes are properly escaped in SQL strings.

## Build Process Updates

Modified the `run.bat` script to compile files in the correct order, ensuring that dependencies are properly resolved:

```bat
REM First compile External classes (like QPiece) that other files depend on
javac -encoding UTF-8 -d build -cp ".;lib/*" External\*.java
REM Then compile AI classes (like UnicodeHelper) that provide utility functions
javac -encoding UTF-8 -d build -cp ".;build;lib/*" AI\*.java
REM Finally compile the rest of the application
javac -encoding UTF-8 -d build -cp ".;build;lib/*" Object\*.java Back\*.java Front\*.java *.java
```

This ensures that the apostrophe handling code is compiled correctly before any files that depend on it.

## Impact

This fix ensures that text with apostrophes can be properly stored in the database without causing SQL syntax errors. This is particularly important for the speech recognition feature where recognized text often contains contractions and quoted speech with apostrophes.
