import External.*;
import Object.*;
import Back.*;
import Front.MainWindow;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up the environment with proper UTF-8 encoding and other configurations
            RunEnvironment.setup();

            try {
                // Initialize the database using configuration file
                QDatabase.InitFromConfig();
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, "Database Initialization Error: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }

            // Launch the title page
            launchGUI();

            try {
                // For testing purposes, you can comment this out once the GUI is working
                // test();
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null, "Database Operation Error: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Launch the GUI application
     */
    public static void launchGUI() {
        // Use SwingUtilities to ensure thread safety
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