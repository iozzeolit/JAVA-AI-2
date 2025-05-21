import External.*;
import Object.*;
import Back.*;
import Front.MainWindow;

public class Main {
    public static void main(String[] args) {
        try {
            RunEnvironment.setup();
            try {
                QDatabase.InitFromConfig();

                java.util.Properties props = new java.util.Properties();
                props.load(new java.io.FileInputStream("config.properties"));
                String databaseName = props.getProperty("databaseName");
                QDatabase.ensureDatabaseExists(databaseName);
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, "Lỗi khởi tạo cơ sở dữ liệu: " + e.getMessage(), "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

            launchGUI();
            try {
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, "Lỗi thao tác cơ sở dữ liệu: " + e.getMessage(), "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launchGUI() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.display();
        });
    }

    public static void test() {
        RecordFile rf = new RecordFile();
        rf.id = IdCounterQuery.getIdThenIncrease("RecordFile");
        rf.dir = "C:\\Users\\User\\Documents\\record.wav";
        QDatabase.insert("RecordFile", rf);
    }
}