package AI;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import java.awt.Component;

public class LanguageModelManager {
    private static final String BASE_MODEL_DIR = "vosk-models";
    private static final String DEFAULT_MODEL_DIR = "vosk-model";
    private static Map<String, String> availableLanguages;

    static {
        availableLanguages = new HashMap<>();
        availableLanguages.put("en", "Tiếng Anh");
        availableLanguages.put("ja", "Tiếng Nhật");
        availableLanguages.put("vi", "Tiếng Việt");
    }

    public static void initializeModelDirectories() {
        File baseDir = new File(BASE_MODEL_DIR);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    public static String getModelPath(String language) {
        return BASE_MODEL_DIR + File.separator + language;
    }

    public static boolean modelExists(String language) {
        String path = getModelPath(language);
        return Files.exists(Paths.get(path));
    }

    public static boolean copyModelToLanguageDir(String language) {
        try {
            Path source = Paths.get(DEFAULT_MODEL_DIR);
            Path destination = Paths.get(BASE_MODEL_DIR, language);
            if (!Files.exists(destination)) {
                Files.createDirectories(destination);
            }

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

    public static String showLanguageSelector(Component parent) {
        List<String> langCodes = new ArrayList<>(availableLanguages.keySet());
        List<String> langNames = new ArrayList<>();

        for (String code : langCodes) {
            String name = availableLanguages.get(code);
            if (modelExists(code)) {
                name += " (Đã cài đặt)";
            }
            langNames.add(name);
        }

        Object selected = JOptionPane.showInputDialog(
                parent,
                "Chọn ngôn ngữ cho nhận dạng giọng nói:",
                "Lựa chọn Ngôn ngữ",
                JOptionPane.QUESTION_MESSAGE,
                null,
                langNames.toArray(),
                langNames.get(0));

        if (selected == null) {
            return null;
        }

        int selectedIndex = langNames.indexOf(selected);
        return langCodes.get(selectedIndex);
    }
}
