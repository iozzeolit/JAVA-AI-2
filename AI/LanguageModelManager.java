package AI;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import java.awt.Component;

/**
 * Utility class to manage multiple language models for Vosk speech recognition
 */
public class LanguageModelManager {
    private static final String BASE_MODEL_DIR = "vosk-models";
    private static final String DEFAULT_MODEL_DIR = "vosk-model"; // For backward compatibility
    private static Map<String, String> availableLanguages;
    
    // Restrict available languages to English, Japanese, and Vietnamese
    static {
        availableLanguages = new HashMap<>();
        availableLanguages.put("en", "Tiếng Anh");
        availableLanguages.put("ja", "Tiếng Nhật");
        availableLanguages.put("vi", "Tiếng Việt");
    }
    
    /**
     * Create the base directory for multiple language models
     */
    public static void initializeModelDirectories() {
        File baseDir = new File(BASE_MODEL_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }
      /**
     * Get the model path for a specific language
     * @param language Language code
     * @return Path to the language model directory
     */
    public static String getModelPath(String language) {
        // Default option has been removed, all languages use their specific directories
        return BASE_MODEL_DIR + File.separator + language;
    }
    
    /**
     * Check if a model exists for the specified language
     * @param language Language code
     * @return True if the model exists
     */
    public static boolean modelExists(String language) {
        String path = getModelPath(language);
        return Files.exists(Paths.get(path));
    }
    
    /**
     * Copy a model from the default directory to the language-specific directory
     * @param language Language code
     * @return True if successful
     */
    public static boolean copyModelToLanguageDir(String language) {
        try {
            // Source is the current default model
            Path source = Paths.get(DEFAULT_MODEL_DIR);
            
            // Destination is the language-specific directory
            Path destination = Paths.get(BASE_MODEL_DIR, language);
            
            // Create destination directory if it doesn't exist
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }
            
            // Copy all files and directories
            Files.walk(source).forEach(sourcePath -> {
                try {
                    Path targetPath = destination.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectories(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Show a dialog to select a language for speech recognition
     * @param parent Parent component
     * @return Selected language code or null if cancelled
     */
    public static String showLanguageSelector(Component parent) {
        // Get available languages
        List<String> langCodes = new ArrayList<>(availableLanguages.keySet());
        List<String> langNames = new ArrayList<>();
        
        // Check which models are installed
        for (String code : langCodes) {
            String name = availableLanguages.get(code);
            if (modelExists(code)) {
                name += " (Đã cài đặt)";
            }
            langNames.add(name);
        }
        
        // Show selection dialog
        Object selected = JOptionPane.showInputDialog(
            parent,
            "Chọn ngôn ngữ cho nhận dạng giọng nói:",
            "Lựa chọn Ngôn ngữ",
            JOptionPane.QUESTION_MESSAGE,
            null,
            langNames.toArray(),
            langNames.get(0)
        );
        
        if (selected == null) {
            return null; // User cancelled
        }
        
        // Find the language code for the selected language name
        int selectedIndex = langNames.indexOf(selected);
        return langCodes.get(selectedIndex);
    }
}
