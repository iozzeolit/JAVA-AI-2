import External.*;
import Object.*;
import Back.*;

public class Init {
    static String serverName = "PC";
    static String databaseName = "DbJava";

    public static void main(String[] args) {
        QDatabase.Init(serverName, databaseName);
        init();
    }

    public static void init() {
        QDatabase.updateServer("IF EXISTS (SELECT * FROM sys.databases WHERE name = '" + databaseName + "') "
                + "DROP DATABASE " + databaseName);
        QDatabase.updateServer("CREATE DATABASE " + databaseName);
        QDatabase.update("CREATE TABLE [RecordFile] ([id] INT PRIMARY KEY, [dir] NVARCHAR(255))");
        QDatabase.update(
                "CREATE TABLE [Sentence] ([id] INT PRIMARY KEY, [recordFileId] INT, [startTime] TIME, [endTime] TIME, [content] NVARCHAR(1500), FOREIGN KEY (recordFileId) REFERENCES RecordFile(id))");
        QDatabase.update("CREATE TABLE [IdCounter] ([tableName] NVARCHAR(255) PRIMARY KEY, [id] INT)");

        IdCounter q = new IdCounter();
        q.tableName = "RecordFile";
        q.id = 1;
        QDatabase.insert("IdCounter", q);

        q = new IdCounter();
        q.tableName = "Sentence";
        q.id = 1;
        QDatabase.insert("IdCounter", q);

        RecordFile f = new RecordFile();
        f.id = IdCounterQuery.getIdThenIncrease( "RecordFile");
        f.dir = "D:\\Users\\generic\\Desktop\\test.mp3";
        QDatabase.insert("RecordFile", f);

        f = new RecordFile();
        f.id = IdCounterQuery.getIdThenIncrease( "RecordFile");
        f.dir = "D:\\Users\\generic\\Desktop\\test2.mp3";
        QDatabase.insert("RecordFile", f);
    }
}