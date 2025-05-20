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

          // Configure UTF-8 for console output
        try {          
            // Now set up UTF-8 output streams
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
            
        } catch (java.io.UnsupportedEncodingException e) {
            // Handle exception if needed
        }
        
    }
    
    private static void suppressFileChooserWarnings() {
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.awt.disableMixing", "true");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        System.setProperty("awt.useSystemAAFontSettings", "on");
    }
}
