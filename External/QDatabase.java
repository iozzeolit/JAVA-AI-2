package External;

import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class QDatabase {
    private static String serverConnString = null;
    private static String defaultConnString = null;    public static void Init(String serverName, String databaseName) {
        serverConnString = "jdbc:sqlserver://" + serverName + ";integratedSecurity=true;trustServerCertificate=true;loginTimeOut=10";
        defaultConnString = "jdbc:sqlserver://" + serverName + ";databaseName=" + databaseName
                + ";integratedSecurity=true;trustServerCertificate=true;loginTimeOut=10";
    }
    
    public static void InitFromConfig() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("config.properties"));
            String serverName = props.getProperty("serverName");
            String databaseName = props.getProperty("databaseName");
            Init(serverName, databaseName);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Error loading config.properties: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    public static void ExecUpdateGeneral(String connString, String query)
    {
        try {
            Connection conn = DriverManager.getConnection(connString, "", "");
            var statement = conn.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Database Query Error: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void ExecQueryGeneral(String connString, String query, QReaderFunction re)
    {
        try {
            Connection conn = DriverManager.getConnection(connString, "", "");
            var statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                re.Fetch(rs);
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Database Query Error: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void updateServer(String query)
    {
        ExecUpdateGeneral(serverConnString, query);
    }

    public static void update(String query)
    {
        ExecUpdateGeneral(defaultConnString, query);
    }

    public static void ExecQuery(String query, QReaderFunction re)
    {
        ExecQueryGeneral(defaultConnString, query, re);
    }
    public static void ExecQueryServer(String query, QReaderFunction re)
    {
        ExecQueryGeneral(serverConnString, query, re);
    }

    public static void select(Query q, QReaderFunction re)
    {
        QDatabase.ExecQuery(q.selectQuery(), re);
    }

    public static void insert(Query q)
    {
        QDatabase.update(q.insertQuery());
    }
    public static void update(Query q)
    {
        QDatabase.update(q.updateQuery());
    }
    public static void delete(Query q)
    {
        QDatabase.update(q.deleteQuery());
    }    public static void insert(String table, QDataObj obj) {
        String query = "INSERT INTO " + table + " VALUES (" + obj.toString() + ")";
        QDatabase.update(query);
    }
}