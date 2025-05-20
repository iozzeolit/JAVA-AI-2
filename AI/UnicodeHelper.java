package AI;

/**
 * Helper class for Unicode text processing and debugging.
 * This class provides methods to help with UTF-8 encoding/decoding and
 * Unicode text visualization for troubleshooting.
 */
public class UnicodeHelper {
 public static String escapeSqlApostrophes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return text.replace("'", "''");
    }
}
