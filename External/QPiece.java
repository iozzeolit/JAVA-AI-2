package External;

import java.sql.*;
import AI.UnicodeHelper;

public class QPiece {
    public static String conv(int val) {
        return String.valueOf(val);
    }

    public static String conv(String val) {
        if (val == null)
            return "NULL";
        val = UnicodeHelper.escapeSqlApostrophes(val);
        return "'" + val + "'";
    }

    public static String convN(String val) {
        if (val == null)
            return "NULL";
        val = UnicodeHelper.escapeSqlApostrophes(val);
        return "N'" + val + "'";
    }

    public static String conv(Time val) {
        if (val == null)
            return "NULL";
        String timeStr = val.toString();
        return "'" + UnicodeHelper.escapeSqlApostrophes(timeStr) + "'";
    }
}
