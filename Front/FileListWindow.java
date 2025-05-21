package Front;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import External.*;
import Object.*;
import Back.*;

import java.sql.*;

public class FileListWindow extends JFrame {
    // Disable JFileChooser shell folder warnings
    static {
        // This suppresses the warnings about not being able to access Desktop and
        // Personal folders
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("swing.volatileImageBufferEnabled", "false");
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        
        // Disable Windows shell integration to prevent NullPointerException in JFileChooser
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        // System.setProperty("file.encoding", "UTF-8");
          // CRITICAL FIXES for Win32ShellFolder2 NullPointerExceptions:
        // These completely disable shell folder usage in JFileChooser
        System.setProperty("sun.awt.disableGtkFileDialogs", "true");
        System.setProperty("swing.useSystemFontSettings", "true");
        System.setProperty("javax.swing.JFileChooser.useSystemExtensionHiding", "false");
        System.setProperty("FileChooser.useShellFolder", "false");
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
          // This prevents the parseDisplayName null pointer exception
        System.setProperty("javax.swing.JFileChooser.readOnly", "true");
        // Most important for Win32ShellFolder2.parseDisplayName NullPointerException
        System.setProperty("win.no.shell.folders", "true");
        
        // Additional system properties to ensure shell integration is fully disabled
        System.setProperty("sun.io.useCanonCaches", "false");
        System.setProperty("sun.io.useCanonPrefixCache", "false");
        System.setProperty("awt.file.showHiddenFiles", "false"); 
        System.setProperty("apple.awt.use-file-dialog-packages", "false");
    }    private static final String WINDOW_TITLE = "Danh sách tập tin MP3";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;

    private final JFrame parentWindow;
    private String currentLanguage = "en"; // Default language is English

    private JTable fileTable;
    private DefaultTableModel tableModel;

    private Process ffmpegProcess = null;
    
    public FileListWindow(JFrame parent) {
        this.parentWindow = parent;

        // Set up window properties
        setTitle(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only close this window, not entire app
        setLocationRelativeTo(parentWindow); // Center relative to parent

        // Load language preference
        loadLanguagePreference();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentWindow.setVisible(true);
                dispose();
            }
        });
        
        // Initialize components
        initComponents();
    }
    
    /**
     * Load saved language preference from config file
     */
    private void loadLanguagePreference() {
        try {
            File configFile = new File("config.properties");
            if (configFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream in = new FileInputStream(configFile)) {
                    props.load(in);
                }
                
                String savedLanguage = props.getProperty("speech.language");
                if (savedLanguage != null && !savedLanguage.isEmpty()) {
                    currentLanguage = savedLanguage;
                }
            }
        } catch (Exception ex) {
            // Ignore errors loading language preference
        }
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = createFileTable();
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Quản lý tệp âm thanh", JLabel.LEFT);
        titleLabel.setFont(new Font("", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel, BorderLayout.WEST);

        // Re-added the instructional label for multi-selection
        JLabel selectionLabel = new JLabel("(Giữ Ctrl/Shift để chọn nhiều tệp để gộp)");
        selectionLabel.setFont(new Font("", Font.ITALIC, 12));
        selectionLabel.setForeground(new Color(100, 100, 100));
        panel.add(selectionLabel, BorderLayout.SOUTH);

        return panel;
    }    private JScrollPane createFileTable() {
        String[] columnNames = {"ID", "Đường dẫn tệp", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all columns non-editable
            }
        };       
         fileTable = new JTable(tableModel);
        fileTable.setRowHeight(30);
        fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);fileTable.getColumnModel().getColumn(0).setPreferredWidth(80); // ID
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(500); // File Path
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Status
        fileTable.setFont(new Font("", Font.PLAIN, 14));
        
        // Setup table selection listener
        setupTableSelectionListener();

        fetchData();

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        return scrollPane;
    }    // Set up table selection listener to allow double-click functionality if needed in the future
    private void setupTableSelectionListener() {
        // Add double-click listener to play the selected file
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    playSelectedFile();
                }
            }
        });
    }    private JPanel createButtonPanel() {
        // Use a panel with BorderLayout to hold the button rows
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create two rows of buttons using GridLayout
        JPanel buttonGrid = new JPanel(new GridLayout(2, 4, 10, 10));
        buttonGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addMp3Button = new JButton("Thêm tệp MP3");
        styleButton(addMp3Button);
        addMp3Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChooser();
            }
        });        JButton analyzeSelectedButton = new JButton("Phân tích đã chọn");
        styleButton(analyzeSelectedButton);
        analyzeSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeSelectedFile();
            }
        });

        JButton editSelectedButton = new JButton("Chỉnh sửa đã chọn");
        styleButton(editSelectedButton);
        editSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedFile();
            }
        });

        JButton deleteSelectedButton = new JButton("Xóa đã chọn");
        styleButton(deleteSelectedButton);
        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedFile();
            }
        });        JButton mergeFilesButton = new JButton("Gộp tệp");
        styleButton(mergeFilesButton);
        mergeFilesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMergeDialog();
            }
        });

        JButton backToMainButton = new JButton("Quay lại trang chính");
        styleButton(backToMainButton);
        backToMainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentWindow.setVisible(true);
                dispose();
            }
        });        // Add "Play Selected" button
        JButton playSelectedButton = new JButton("Phát đã chọn");
        styleButton(playSelectedButton);
        playSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSelectedFile();
            }
        });            // First row
        buttonGrid.add(addMp3Button);
        buttonGrid.add(playSelectedButton);
        buttonGrid.add(editSelectedButton);
        buttonGrid.add(analyzeSelectedButton);
        // Second row
        buttonGrid.add(deleteSelectedButton);
        buttonGrid.add(mergeFilesButton);
        buttonGrid.add(backToMainButton);
        
        // Add the button grid to the main panel
        panel.add(buttonGrid, BorderLayout.CENTER);

        return panel;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("", Font.BOLD, 14));
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
    
    private void showFileChooser() {
        try {
            // Use AWT FileDialog instead of JFileChooser to avoid Windows shell integration
            // AWT FileDialog uses the native OS dialog which doesn't use the problematic
            // Win32ShellFolder2 class under the hood
            FileDialog fileDialog = new FileDialog(this, "Select MP3 File", FileDialog.LOAD);
            
            // Set file filter for MP3 files only
            fileDialog.setFilenameFilter(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp3");
                }
            });
            
            // Show the dialog (this blocks until user makes a selection or cancels)
            fileDialog.setVisible(true);
            
            // Process selected file
            String selectedFile = fileDialog.getFile();
            String selectedDir = fileDialog.getDirectory();
            
            if (selectedFile != null && selectedDir != null) {
                File file = new File(selectedDir, selectedFile);
                if (file.exists()) {
                    addFileToTable(file);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error opening file dialog: " + e.getMessage(),
                    "File Dialog Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

            // Fallback to manual path input if file dialog fails
            String manualPath = JOptionPane.showInputDialog(this,
                    "Please enter the full path to an MP3 file:",
                    "Manual File Selection",
                    JOptionPane.QUESTION_MESSAGE);

            if (manualPath != null && !manualPath.trim().isEmpty()) {
                File manualFile = new File(manualPath.trim());
                if (manualFile.exists() && manualFile.isFile() &&
                        manualFile.getName().toLowerCase().endsWith(".mp3")) {
                    addFileToTable(manualFile);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid file path or not an MP3 file.",
                            "Invalid File",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void addFileToTable(File file) {
        try {
            RecordFile f = new RecordFile();
            f.id = IdCounterQuery.getIdThenIncrease("RecordFile");
            f.dir = file.getPath();
            QDatabase.insert("RecordFile", f);

            fetchData();

            JOptionPane.showMessageDialog(this,
                    "File added successfully with ID: " + f.id,
                    "File Added",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding file to database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void deleteSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tập tin để xóa.",
                    "Chưa chọn tập tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) fileTable.getValueAt(selectedRow, 0);
        deleteFile(id);
    }    private void deleteFile(int id) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa tập tin có ID " + id + " không? Thao tác này cũng sẽ xóa tất cả các câu liên quan.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // First delete all associated sentences
                Query sentenceQuery = new Query();
                sentenceQuery.from("Sentence");
                sentenceQuery.where("recordFileId = " + id);
                QDatabase.delete(sentenceQuery);
                
                // Then delete the file record
                Query fileQuery = new Query();
                fileQuery.from("RecordFile");
                fileQuery.where("id = " + id);
                QDatabase.delete(fileQuery);

                fetchData();

                JOptionPane.showMessageDialog(this,
                        "File and associated sentences deleted successfully.",
                        "File Deleted",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting file from database: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }    public void fetchData() {
        // Clear existing table data
        tableModel.setRowCount(0);

        // Query to get all MP3 files
        Query q = new Query();
        q.select("rf.[id]");
        q.select("rf.[dir]");
        q.select("(SELECT COUNT(*) FROM Sentence s WHERE s.recordFileId = rf.id) AS sentenceCount");
        q.from("RecordFile AS rf");

        var result = new QReaderFunction() {
            @Override
            public void Fetch(ResultSet rs) throws Exception {                
                int pos = 1;
                int id = QReader.getInt(rs, pos++);
                String dir = QReader.getString(rs, pos++);                
                int sentenceCount = QReader.getInt(rs, pos++);
                String status = sentenceCount > 0 ? "Đã phân tích" : "Chưa phân tích";
                Object[] rowData = { id, dir, status };
                tableModel.addRow(rowData);
            }
        };

        try {
            QDatabase.select(q, result);        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi lấy dữ liệu: " + e.getMessage(), "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Update table data from database when showing window
                fetchData();
                setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parentWindow,
                        "Error displaying file list: " + e.getMessage(),
                        "Display Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                dispose(); // Close this window if we can't display it properly
                parentWindow.setVisible(true); // Show the main window again
            }
        });
    }    /**
     * Play an MP3 file using FFmpeg with fallback to Java's built-in capabilities
     * @param filePath Path to the MP3 file to play
     */
    private void playFile(String filePath) {
        try {
            // Check if we already have a playback process running
            if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
                ffmpegProcess.destroy();
                ffmpegProcess = null;
                return;
            }
            
            // Verify file exists
            File file = new File(filePath);            if (!file.exists()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy tập tin: " + filePath,
                        "Lỗi tập tin",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
              // Ask user which player they want to use
            String[] options = {"Trình phát hệ thống", "Trình phát FFmpeg"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Bạn muốn sử dụng trình phát nào?",
                    "Chọn trình phát",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
                    
            if (choice == 0) {
                // User chose system player
                useSystemPlayer(file);
                return;
            }
              // User chose FFmpeg, check if it's available
            boolean ffmpegAvailable = isFFmpegAvailable();
            
            if (!ffmpegAvailable) {
                // Fall back to Java's built-in capabilities
                useJavaFallback(file);
                return;
            }              // Start ffplay process for playback
            ffmpegProcess = new ProcessBuilder("ffmpeg/ffplay.exe", "-autoexit", "-nodisp", filePath)
                    .redirectErrorStream(true)
                    .start();
            
            // Show the audio player window
            AudioPlayerWindow playerWindow = new AudioPlayerWindow(this, filePath);
            playerWindow.setVisible(true);
            
            // Create a thread to monitor the process
            new Thread(() -> {
                try {
                    // Wait for the process to complete
                    ffmpegProcess.waitFor();
                    
                    // Close the player window when playback ends
                    SwingUtilities.invokeLater(() -> {
                        if (playerWindow.isVisible()) {
                            playerWindow.dispose();
                        }
                        
                        // If there was an error, show an error message
                        // if (exitCode != 0) {
                        //     JOptionPane.showMessageDialog(FileListWindow.this,
                        //         "Lỗi khi phát tệp âm thanh với FFmpeg. Mã lỗi: " + exitCode,
                        //         "Lỗi phát lại",
                        //         JOptionPane.ERROR_MESSAGE);
                        // }
                    });
                } catch (Exception _) {
                    // Thread interrupted, do nothing
                }
            }).start();

        } catch (Exception e) {            JOptionPane.showMessageDialog(this,
                    "Lỗi khởi tạo phát lại: " + e.getMessage(),
                    "Lỗi phát lại",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Check if FFmpeg (ffplay) is available on the system
     * @return true if ffplay is available, false otherwise
     */
    private boolean isFFmpegAvailable() {
        try {
            Process process = new ProcessBuilder("ffmpeg/ffplay.exe", "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Use Java's built-in capabilities to open the file with the default system player
     * @param file The file to play
     */    private void useJavaFallback(File file) {
        try {
            // Ask the user if they want to use the system player
            String[] options = {"Mở bằng trình phát hệ thống", "Hủy bỏ"};            int response = JOptionPane.showOptionDialog(this,
                    "Bạn muốn tiếp tục như thế nào?",
                    "Tùy chọn phát lại",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);if (response == 0) {
                // Open with system player
                useSystemPlayer(file);
            }
            // If response is 1 or closed, do nothing
              } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi phát tập tin: " + e.getMessage(),
                    "Lỗi phát lại",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Play the selected file in the table
     */
    private void playSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tập tin để phát.",
                    "Chưa chọn tập tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String filePath = (String) fileTable.getValueAt(selectedRow, 1); // Column 1 is File Path
        playFile(filePath);
    }

    /**
     * Analyze the selected file
     */
    private void analyzeSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tập tin để phân tích.",
                    "Chưa chọn tập tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) fileTable.getValueAt(selectedRow, 0);
        String filePath = (String) fileTable.getValueAt(selectedRow, 1);
          // Create progress dialog
        JDialog progressDialog = new JDialog(this, "Đang phân tích âm thanh", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
          JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel statusLabel = new JLabel("Đang khởi tạo nhận dạng giọng nói...");
        statusLabel.setFont(new Font("", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(panel);        // Ask if user wants to use current language or select a different one
        String[] options = {"Sử dụng ngôn ngữ hiện tại", "Chọn ngôn ngữ khác"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Ngôn ngữ hiện tại là: " + currentLanguage + 
                "\nBạn có muốn sử dụng ngôn ngữ này hoặc chọn một ngôn ngữ khác không?",
                "Lựa chọn ngôn ngữ",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
                
        final String languageCode;
        
        if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {            // User wants to select a different language or closed the dialog
            String[] languageOptions = {"Tiếng Anh", "Tiếng Nhật", "Tiếng Việt"};
            String selectedOption = (String) JOptionPane.showInputDialog(
                    this,
                    "Chọn ngôn ngữ cho nhận dạng giọng nói:",
                    "Lựa chọn ngôn ngữ",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    languageOptions,
                    languageOptions[0]);            // If user canceled language selection
            if (selectedOption == null) {
                return;
            }
            
            if (selectedOption.equals("Tiếng Anh")) {
                languageCode = "en";
            } else if (selectedOption.equals("Tiếng Nhật")) {
                languageCode = "ja";
            } else if (selectedOption.equals("Tiếng Việt")) {
                languageCode = "vi";
            } else {
                // For any other option, use English as default
                languageCode = "en";
            }
            
            // Update current language
            currentLanguage = languageCode;
            
            // Save the preference
            try {
                Properties props = new Properties();
                File configFile = new File("config.properties");
                
                // Load existing properties if file exists
                if (configFile.exists()) {
                    try (FileInputStream in = new FileInputStream(configFile)) {
                        props.load(in);
                    }
                }
                
                // Set the language property
                props.setProperty("speech.language", languageCode);
                
                // Save the properties
                try (FileOutputStream out = new FileOutputStream(configFile)) {
                    props.store(out, "Speech Recognition Settings");
                }
            } catch (Exception ex) {
                // Ignore errors saving language preference
            }
        } else {
            // Use current language
            languageCode = currentLanguage;
        }
        
        // Start analysis in background thread
        new Thread(() -> {
            try {
                // Create service with selected language
                AI.SpeechRecognitionService service = new AI.SpeechRecognitionService(languageCode);
                  // Update status
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Đang kiểm tra mô hình ngôn ngữ...");
                });
                
                // Check if model path exists
                String modelPath = service.getModelPath();
                
                try {
                    service.loadModel(modelPath);
                } catch (IOException e) {
                    // Model doesn't exist, just proceed without loading
                }
                  // If we reach here, model is configured and ready to use
                analyzeWithService(service, id, filePath, statusLabel, progressDialog);
                
            } catch (Exception e) {
                e.printStackTrace();                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(FileListWindow.this,
                            "Lỗi phân tích âm thanh: " + e.getMessage(),
                            "Lỗi phân tích",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
        
        progressDialog.setVisible(true);
    }
      /**
     * Helper method to run analysis with the service
     */
    private void analyzeWithService(AI.SpeechRecognitionService service, int id, String filePath, 
                                  JLabel statusLabel, JDialog progressDialog) {        try {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Đang chuyển đổi định dạng âm thanh...");
            });
              // Check if this file already has sentences in the database and delete them
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Đang kiểm tra các câu hiện có...");
            });
            
            try {
                Query deleteQuery = new Query();
                deleteQuery.from("Sentence");
                deleteQuery.where("recordFileId = " + id);
                QDatabase.delete(deleteQuery);
            } catch (Exception ex) {
                // Ignore errors deleting existing sentences
            }
            
            // Extract sentences
            List<Sentence> sentences = service.extractSentencesFromAudio(filePath, id);
            
            // Save to database
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Đang lưu vào cơ sở dữ liệu...");
            });
            
            for (Sentence sentence : sentences) {
                QDatabase.insert("Sentence", sentence);
            }
            
            // Update UI
            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                fetchData();  // Refresh table
                
                // Show analysis results
                showAnalysisResults(id, sentences);
            });
            
        } catch (Exception e) {
            e.printStackTrace();            SwingUtilities.invokeLater(() -> {
                progressDialog.dispose();
                JOptionPane.showMessageDialog(FileListWindow.this,
                        "Lỗi phân tích âm thanh: " + e.getMessage(),
                        "Lỗi phân tích",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * Show analysis results in a dialog
     */    private void showAnalysisResults(int fileId, List<Sentence> sentences) {
        JDialog resultsDialog = new JDialog(this, "Kết quả phân tích", false);
        resultsDialog.setLayout(new BorderLayout(10, 10));
        resultsDialog.setSize(700, 500);
        resultsDialog.setLocationRelativeTo(this);
          JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Các câu đã trích xuất", JLabel.CENTER);
        titleLabel.setFont(new Font("", Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel countLabel = new JLabel(sentences.size() + " câu đã được trích xuất", JLabel.CENTER);
        countLabel.setFont(new Font("", Font.PLAIN, 14));
        headerPanel.add(countLabel, BorderLayout.SOUTH);
          // Create table with sentences
        String[] columnNames = {"Thời điểm bắt đầu", "Thời điểm kết thúc", "Nội dung"};
        Object[][] data = new Object[sentences.size()][3];
          for (int i = 0; i < sentences.size(); i++) {
            Sentence sentence = sentences.get(i);
            data[i][0] = formatTime(timeToSeconds(sentence.startTime));
            data[i][1] = formatTime(timeToSeconds(sentence.endTime));
            data[i][2] = sentence.content;
        }
        
        JTable sentenceTable = new JTable(data, columnNames);
        // sentenceTable.setFont(new Font("", Font.PLAIN, 14));
        sentenceTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        sentenceTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        sentenceTable.getColumnModel().getColumn(2).setPreferredWidth(500);
        sentenceTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(sentenceTable);
          JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));        
        JButton editButton = new JButton("Chỉnh sửa các câu");
        styleButton(editButton);
        editButton.addActionListener(_ -> openSentenceEditor(fileId));
        
        JButton closeButton = new JButton("Đóng");
        styleButton(closeButton);
        closeButton.addActionListener(_ -> resultsDialog.dispose());
        
        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);
        
        resultsDialog.add(headerPanel, BorderLayout.NORTH);
        resultsDialog.add(scrollPane, BorderLayout.CENTER);
        resultsDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        resultsDialog.setVisible(true);
    }

    /**
     * Convert a Time object to seconds
     */
    private double timeToSeconds(Time time) {
        if (time == null) {
            return 0;
        }
        return time.getTime() / 1000.0;
    }

    /**
     * Format seconds as HH:MM:SS
     */
    private String formatTime(double seconds) {
        int hours = (int)(seconds / 3600);
        int minutes = (int)((seconds % 3600) / 60);
        int secs = (int)(seconds % 60);
        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Open sentence editor dialog
     */
    private void openSentenceEditor(int fileId) {
        SentenceEditorDialog editor = new SentenceEditorDialog(this, fileId);
        editor.display();
        // Refresh our data after editing
        fetchData();
    }
    
    /**
     * Edit the selected file's metadata or content
     */    private void editSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tập tin để chỉnh sửa.",
                    "Chưa chọn tập tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int fileId = (int) fileTable.getValueAt(selectedRow, 0);
        String status = (String) fileTable.getValueAt(selectedRow, 2);        // Check if the file has been analyzed
        if (!status.equalsIgnoreCase("Đã phân tích")) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Tập tin này chưa được phân tích. Bạn có muốn phân tích ngay bây giờ không?",
                    "Tập tin chưa được phân tích",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                    
            if (choice == JOptionPane.YES_OPTION) {
                analyzeSelectedFile();
            }
            return;
        }
        
        // Open the sentence editor dialog
        openSentenceEditor(fileId);
    }

     /**
     * Show the file merge dialog
     */
    private void showMergeDialog() {
        // Get selected rows
        int[] selectedRows = fileTable.getSelectedRows();
        List<Integer> selectedFileIds = new ArrayList<>();
        
        if (selectedRows.length > 0) {
            // Add selected file IDs to the list
            for (int row : selectedRows) {
                int fileId = (int) fileTable.getValueAt(row, 0);
                selectedFileIds.add(fileId);
            }
            
            // Create and show the merge dialog with selected files
            MergeFileDialog mergeDialog = new MergeFileDialog(this, selectedFileIds);
            mergeDialog.display();        } else {
            // If no files are selected, show the merge dialog without pre-selections
            MergeFileDialog mergeDialog = new MergeFileDialog(this);
            mergeDialog.display();
        }
        
        // Note: No need to call fetchData() here as MergeFileDialog will refresh
        // the parent window via its window listener when it's closed
    }
    
    /**
     * Use the system's default player to open the file.
     * @param file The file to play.
     */
    private void useSystemPlayer(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {                JOptionPane.showMessageDialog(this,
                        "Trình phát hệ thống không được hỗ trợ trên nền tảng này.",
                        "Lỗi phát lại",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {            JOptionPane.showMessageDialog(this,
                    "Lỗi khi mở tập tin bằng trình phát hệ thống: " + e.getMessage(),
                    "Lỗi phát lại",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Inner class to represent a simple audio player window
     */
    private class AudioPlayerWindow extends JDialog {
        private final String fileName;
        private final Timer progressTimer;
        private JProgressBar progressBar;
        private JLabel timeLabel;
        private long startTime;
        private int duration = 0;
        
        public AudioPlayerWindow(Frame parent, String filePath) {
            super(parent, "Đang phát âm thanh", false);
            this.fileName = new File(filePath).getName();
            
            // Set up window
            setSize(400, 200);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));
            
            // Create components
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // Now playing label with file name
            JLabel nowPlayingLabel = new JLabel("Đang phát: " + fileName);
            nowPlayingLabel.setFont(new Font("", Font.BOLD, 14));
            nowPlayingLabel.setHorizontalAlignment(JLabel.CENTER);
            mainPanel.add(nowPlayingLabel, BorderLayout.NORTH);
            
            // Create a panel for the progress bar and time
            JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
            
            // Progress bar to show playback progress
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(false);
            progressBar.setValue(0);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            
            // Time label
            timeLabel = new JLabel("00:00 / 00:00");
            timeLabel.setHorizontalAlignment(JLabel.CENTER);
            progressPanel.add(timeLabel, BorderLayout.SOUTH);
            
            mainPanel.add(progressPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            
            // Stop button
            JButton stopButton = new JButton("Dừng phát");
            stopButton.setBackground(new Color(220, 53, 69));
            stopButton.setForeground(Color.WHITE);
            stopButton.setFocusPainted(false);
            stopButton.addActionListener(_ -> stopPlayback());
            buttonPanel.add(stopButton);
            
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(mainPanel);
            
            // Try to get audio duration using ffprobe
            try {
                Process ffprobeProcess = new ProcessBuilder(
                        "ffmpeg/ffprobe.exe", 
                        "-v", "error", 
                        "-show_entries", "format=duration", 
                        "-of", "default=noprint_wrappers=1:nokey=1", 
                        filePath)
                    .redirectErrorStream(true)
                    .start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(ffprobeProcess.getInputStream()));
                String line = reader.readLine();
                if (line != null) {
                    duration = (int)Float.parseFloat(line);
                    timeLabel.setText("00:00 / " + formatDuration(duration));
                    progressBar.setMaximum(duration);
                }
                
                ffprobeProcess.waitFor();
                
            } catch (Exception e) {
                // If duration retrieval fails, set progress bar to indeterminate
                progressBar.setIndeterminate(true);
                e.printStackTrace();
            }
            
            // Add window listener to stop playback when window is closed
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    stopPlayback();
                }
            });
            
            // Start the timer to update the progress bar
            startTime = System.currentTimeMillis();
            progressTimer = new Timer(1000, (_) -> updateProgress());
            progressTimer.start();
        }
        
        private void updateProgress() {
            if (duration > 0) {
                int elapsed = (int)((System.currentTimeMillis() - startTime) / 1000);
                if (elapsed <= duration) {
                    progressBar.setValue(elapsed);
                    timeLabel.setText(formatDuration(elapsed) + " / " + formatDuration(duration));
                }
            }
        }
        
        private String formatDuration(int seconds) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%02d:%02d", minutes, remainingSeconds);
        }
        
        private void stopPlayback() {
            if (progressTimer != null) {
                progressTimer.stop();
            }
            
            if (ffmpegProcess != null && ffmpegProcess.isAlive()) {
                ffmpegProcess.destroy();
                ffmpegProcess = null;
            }
            
            dispose();
        }
    }
}
