import javax.swing.*;

/**
 * This class sets up the Java environment properly before running the app
 * It fixes issues with JFileChooser and other Swing components
 */
public class RunEnvironment {
    /**
     * Sets up the Java environment with appropriate system properties
     */    public static void setup() {        // Set comprehensive UTF-8 encoding for all operations
        // System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("input.encoding", "UTF-8"); 
        System.setProperty("output.encoding", "UTF-8");
        
        // Suppress file chooser warnings
        suppressFileChooserWarnings();
        
        // Set up Swing properties
        configureSwing();

          // Configure UTF-8 for console output
        try {
            // On Windows, the console may need special handling for UTF-8 output
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("windows")) {
                // Use a file output stream first to ensure the console encoding is properly set
                String consoleCP = System.getProperty("sun.stdout.encoding");
                if (consoleCP == null || !consoleCP.equalsIgnoreCase("UTF-8")) {
                    // Try to set the console code page to UTF-8 (65001)
                    try {
                        // Set Windows console to UTF-8 mode using ProcessBuilder
                        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "chcp", "65001");
                        pb.inheritIO(); // Redirect output to our console
                        Process process = pb.start();
                        process.waitFor(); // Wait for the command to complete
                    } catch (Exception e) {
                        // Handle exception if needed
                    }
                }
            }
            
            // Now set up UTF-8 output streams
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
            
        } catch (java.io.UnsupportedEncodingException e) {
            // Handle exception if needed
        }
        
    }
    
    private static void suppressFileChooserWarnings() {
        // These properties help avoid the "Cannot access Desktop" warnings
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.awt.disableMixing", "true");
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
    }
    
    private static void configureSwing() {
        // Use Metal Look and Feel which has fewer issues with file choosers
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("FileChooser.useSystemIcons", Boolean.FALSE);
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        } catch (Exception e) {
            // Handle exception if needed
        }
    }
}
