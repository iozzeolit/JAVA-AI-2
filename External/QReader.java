package External;

import java.sql.*;

public class QReader {
    public static int getInt(ResultSet rs, int pos) throws Exception {
        return rs.getInt(pos);
    }    public static String getString(ResultSet rs, int pos) throws Exception {
        return rs.getString(pos);
    }
    
    public static double getDouble(ResultSet rs, int pos) throws Exception {
        return rs.getDouble(pos);
    }
    
    public static Time getTime(ResultSet rs, int pos) throws Exception {
        return rs.getTime(pos);
    }
}