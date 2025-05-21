package AI;

public class UnicodeHelper {
    public static String escapeSqlApostrophes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text.replace("'", "''");
    }
}
