package External;

import java.sql.*;
import java.io.FileInputStream;
import java.util.Properties;

public class QDatabase {
    private static String serverConnString = null;
    private static String defaultConnString = null;   
    public static void Init(String serverName, String databaseName) {
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
      /**
     * Checks if a database exists, and creates it if it doesn't
     * @param databaseName The name of the database to check/create
     * @return true if the database exists or was created successfully, false otherwise
     */    public static boolean ensureDatabaseExists(String databaseName) {
        boolean exists = false;
        Connection conn = null;
        
        try {
            // First check if the database exists using the server connection
            conn = DriverManager.getConnection(serverConnString, "", "");
            ResultSet rs = conn.getMetaData().getCatalogs();
            
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (dbName.equals(databaseName)) {
                    exists = true;
                    break;
                }
            }
            
            // If database doesn't exist, create it
            if (!exists) {
                System.out.println("Cơ sở dữ liệu " + databaseName + " không tồn tại. Đang tạo...");
                Statement stmt = conn.createStatement();
                String sql = "CREATE DATABASE " + databaseName;
                stmt.executeUpdate(sql);
                
                // After creating the database, initialize the tables
                initializeTables();
                
                exists = true;
            }
            
            rs.close();
            
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Lỗi khi kiểm tra/tạo cơ sở dữ liệu: " + e.getMessage(), 
                "Lỗi cơ sở dữ liệu", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Close connection in finally block to ensure it happens even if an exception occurs
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return exists;
    }
      /**
     * Creates the necessary tables in the database
     * Uses the defaultConnString which already contains the database name
     */
    private static void initializeTables() {
        Connection conn = null;
        
        try {
            // Connect to the database using the existing defaultConnString
            conn = DriverManager.getConnection(defaultConnString, "", "");
            Statement stmt = conn.createStatement();
            
            // Create tables
            stmt.executeUpdate("CREATE TABLE [RecordFile] ([id] INT PRIMARY KEY, [dir] NVARCHAR(255))");
            stmt.executeUpdate("CREATE TABLE [Sentence] ([id] INT PRIMARY KEY, [recordFileId] INT, [startTime] TIME, [endTime] TIME, [content] NVARCHAR(1500), FOREIGN KEY (recordFileId) REFERENCES RecordFile(id))");
            stmt.executeUpdate("CREATE TABLE [IdCounter] ([tableName] NVARCHAR(255) PRIMARY KEY, [id] INT)");
            
            // Initialize IdCounter values
            stmt.executeUpdate("INSERT INTO [IdCounter] VALUES ('RecordFile', 1)");
            stmt.executeUpdate("INSERT INTO [IdCounter] VALUES ('Sentence', 1)");
            
            System.out.println("Các bảng cơ sở dữ liệu đã được tạo thành công.");
            
        } catch (SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Lỗi khởi tạo các bảng cơ sở dữ liệu: " + e.getMessage(), 
                "Lỗi cơ sở dữ liệu", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Close connection in a finally block
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}