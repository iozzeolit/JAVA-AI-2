package Front;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import External.*;
import Object.*;
import Back.*;

/**
 * Dialog for merging audio files
 */
public class MergeFileDialog extends JDialog {
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JTextField outputFileField;
    private JFrame parentFrame;
    
    // List to store merge entries
    private List<MergeEntry> mergeEntries = new ArrayList<>();
      public MergeFileDialog(JFrame parent) {
        super(parent, "Gộp tệp âm thanh", true);
        this.parentFrame = parent;
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Add window listener to refresh the parent window when dialog is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Refresh parent window if it's a FileListWindow
                if (parentFrame instanceof FileListWindow) {
                    ((FileListWindow) parentFrame).fetchData();
                }
            }        });          initComponents();
        // Don't load all files by default
        // Just initialize the empty table
        mergeEntries.clear();
        tableModel.setRowCount(0);
        setupKeyboardShortcuts(); // Setup keyboard shortcuts
    }
      /**
     * Constructor for creating merge dialog with pre-selected files
     * @param parent Parent frame
     * @param selectedFileIds List of selected file IDs to add to the merge list
     */    public MergeFileDialog(JFrame parent, List<Integer> selectedFileIds) {
        super(parent, "Gộp tệp âm thanh", true);
        this.parentFrame = parent;
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Add window listener to refresh the parent window when dialog is closed
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Refresh parent window if it's a FileListWindow
                if (parentFrame instanceof FileListWindow) {
                    ((FileListWindow) parentFrame).fetchData();
                }
            }
        });        
        initComponents();
        setupKeyboardShortcuts();
          // Add pre-selected files to the merge list
        if (selectedFileIds != null && !selectedFileIds.isEmpty()) {
            loadSelectedFiles(selectedFileIds);
        } else {
            // Don't load all files by default
            mergeEntries.clear();
            tableModel.setRowCount(0);
        }
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Gộp tệp âm thanh", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        // JLabel instructionsLabel = new JLabel("Hướng dẫn");
        JTextArea instructionsArea = new JTextArea(
            "Chọn các tệp MP3 để gộp.\n" +
            "Bạn có thể thay đổi thứ tự các tệp bằng cách kéo thả.\n" +
            "Nhấn 'Gộp tệp' để bắt đầu quá trình.\n" +
            "Kết quả sẽ được lưu thành một tệp MP3 mới.\n");
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionsArea.setBackground(new Color(240, 240, 240));
        instructionsPanel.add(instructionsArea, BorderLayout.CENTER);
          // File selection table
        String[] columnNames = {"Đường dẫn tệp", "Thời gian bắt đầu (HH:MM:SS)", "Thời gian kết thúc (HH:MM:SS)", "Xóa"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing start, end times and the Remove button
                return column > 0;
            }
        };
        
        fileTable = new JTable(tableModel);
        fileTable.setRowHeight(30);
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        
        // Setup button renderer and editor for the Remove column
        fileTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        fileTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor());
        
        // Table model listener to validate time inputs
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    
                    if (column == 1 || column == 2) { // Start or end time
                        String timeStr = (String) tableModel.getValueAt(row, column);
                        
                        if (!isValidTimeFormat(timeStr)) {
                            JOptionPane.showMessageDialog(
                                MergeFileDialog.this,
                                "Vui lòng nhập thời gian theo định dạng HH:MM:SS",
                                "Định dạng thời gian không hợp lệ",
                                JOptionPane.ERROR_MESSAGE
                            );
                            tableModel.setValueAt("00:00:00", row, column);
                        }
                    }
                }
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(fileTable);        // Control panel (buttons for adding and removing files)
        JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
          // Create dropdown for file selection
        JPanel fileSelectionPanel = new JPanel(new BorderLayout(10, 0));
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fileLabel = new JLabel("Thêm tệp nhanh:");
        fileLabel.setFont(new Font("Arial", Font.BOLD, 14));
        labelPanel.add(fileLabel);
        fileSelectionPanel.add(labelPanel, BorderLayout.WEST);
          // Populate dropdown with available files
        List<AvailableFile> availableFiles = getAvailableFiles();
        
        // Add a prompt item at the beginning
        AvailableFile promptItem = new AvailableFile();
        promptItem.id = -1;
        promptItem.path = "";
        promptItem.filename = "-- Chọn một tệp âm thanh --";
        availableFiles.add(0, promptItem);        JComboBox<AvailableFile> fileDropdown = new JComboBox<>(availableFiles.toArray(new AvailableFile[0]));
        fileDropdown.setPreferredSize(new Dimension(400, 30));
        fileDropdown.setFont(new Font("Arial", Font.PLAIN, 14));
        fileDropdown.setBackground(Color.WHITE);
        fileDropdown.setForeground(Color.BLACK);
        fileDropdown.setMaximumRowCount(15); // Show more items in dropdown
        fileDropdown.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), // Match button color
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        fileDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AvailableFile) {
                    AvailableFile file = (AvailableFile) value;
                    if (file.id == -1) {
                        // Format the prompt item differently
                        setText(file.filename);
                        setForeground(Color.GRAY);
                        setFont(getFont().deriveFont(Font.ITALIC));
                    } else {                        // Show ID, filename and truncated path
                        String path = file.path;
                        if (path.length() > 40) {
                            path = "..." + path.substring(path.length() - 40);
                        }
                        setText("ID: " + file.id + " - " + file.filename + " (" + path + ")");
                    }
                }
                return this;
            }
        });          JButton quickAddButton = new JButton("Thêm tệp");
        styleButton(quickAddButton);
        quickAddButton.setFont(new Font("Arial", Font.BOLD, 14));
        quickAddButton.setPreferredSize(new Dimension(120, 30));        quickAddButton.addActionListener(_ -> {
            AvailableFile selectedFile = (AvailableFile) fileDropdown.getSelectedItem();
            if (selectedFile != null && selectedFile.id != -1) {
                addFileFromDropdown(selectedFile);
                // Reset the dropdown to the prompt item after adding
                fileDropdown.setSelectedIndex(0);
            } else if (selectedFile != null && selectedFile.id == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng chọn một tệp âm thanh từ danh sách", 
                    "Chưa chọn tệp", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
          // Create a panel for the dropdown and add button
        JPanel dropdownPanel = new JPanel(new BorderLayout(5, 0));
        dropdownPanel.add(fileDropdown, BorderLayout.CENTER);
        dropdownPanel.add(quickAddButton, BorderLayout.EAST);
        
        // Add the dropdown panel to the selection panel
        fileSelectionPanel.add(dropdownPanel, BorderLayout.CENTER);
        
        // Button panel for other controls
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JButton addButton = new JButton("Duyệt tệp");
        styleButton(addButton);
        addButton.addActionListener(_ -> showFileSelector());
          JButton removeButton = new JButton("Xóa tệp đã chọn");
        styleButton(removeButton);
        removeButton.addActionListener(_ -> removeSelectedFile());
          JButton moveUpButton = new JButton("Di chuyển lên");
        styleButton(moveUpButton);
        moveUpButton.addActionListener(_ -> moveFileUp());
        
        JButton moveDownButton = new JButton("Di chuyển xuống");
        styleButton(moveDownButton);
        moveDownButton.addActionListener(_ -> moveFileDown());
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);
        
        // Add panels to control panel
        controlPanel.setLayout(new BorderLayout(5, 10));
        controlPanel.add(fileSelectionPanel, BorderLayout.NORTH);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Output file panel
        JPanel outputPanel = new JPanel(new BorderLayout(10, 10));
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel outputLabel = new JLabel("Tên tệp đầu ra:");
        outputFileField = new JTextField("merged_output.mp3");
        
        JButton browseButton = new JButton("Duyệt...");        browseButton.addActionListener(_ -> browseForOutputFile());
        
        JPanel outputFieldPanel = new JPanel(new BorderLayout(5, 0));
        outputFieldPanel.add(outputFileField, BorderLayout.CENTER);
        outputFieldPanel.add(browseButton, BorderLayout.EAST);
          outputPanel.add(outputLabel, BorderLayout.WEST);
        outputPanel.add(outputFieldPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
          JButton mergeButton = new JButton("Gộp tệp");
        styleButton(mergeButton);
        mergeButton.addActionListener(_ -> mergeFiles());
        
        JButton cancelButton = new JButton("Hủy");
        styleButton(cancelButton);
        cancelButton.addActionListener(_ -> dispose());
        
        actionButtonPanel.add(mergeButton);
        actionButtonPanel.add(cancelButton);          // Add all panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Create a bottom container with proper spacing
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 10));
        bottomContainer.add(controlPanel, BorderLayout.NORTH);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(outputPanel, BorderLayout.NORTH);
        bottomPanel.add(actionButtonPanel, BorderLayout.SOUTH);
          bottomContainer.add(bottomPanel, BorderLayout.CENTER);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        
        getContentPane().add(mainPanel);
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }
    
    private void loadSelectedFiles(List<Integer> selectedFileIds) {
        // Clear the existing list
        mergeEntries.clear();
        tableModel.setRowCount(0);
        
        // Get selected files from the database
        Query q = new Query();
        q.select("rf.[id]");
        q.select("rf.[dir]");
        q.from("RecordFile AS rf");
        q.where("rf.[id] IN (" + String.join(",", selectedFileIds.stream().map(String::valueOf).toArray(String[]::new)) + ")");
        
        try {
            QDatabase.select(q, new QReaderFunction() {
                @Override
                public void Fetch(ResultSet rs) throws Exception {
                    int id = rs.getInt(1);
                    String filePath = rs.getString(2);
                    
                    MergeEntry entry = new MergeEntry();
                    entry.fileId = id;
                    entry.filePath = filePath;
                    entry.startTime = "00:00:00";
                    
                    // Get the duration of the file to set the default end time
                    String duration = getFileDuration(filePath);
                    entry.endTime = duration;
                    
                    mergeEntries.add(entry);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải các tệp đã chọn: " + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        // Update table with loaded entries
        updateTableFromEntries();
    }
    
    private void showFileSelector() {
        // Create a dialog to select files from the database
        JDialog selectorDialog = new JDialog(this, "Chọn tệp âm thanh", true);
        selectorDialog.setSize(600, 400);
        selectorDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create a table to display available files
        DefaultTableModel availableFilesModel = new DefaultTableModel(
    new String[]{"ID", "Đường dẫn tệp", "Trạng thái"}, 0);
        
        JTable availableFilesTable = new JTable(availableFilesModel);
        availableFilesTable.setRowHeight(30);
        availableFilesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        availableFilesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        availableFilesTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(availableFilesTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton selectButton = new JButton("Chọn");
        styleButton(selectButton);
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = availableFilesTable.getSelectedRow();
                if (selectedRow != -1) {
                    int id = (int) availableFilesTable.getValueAt(selectedRow, 0);
                    String filePath = (String) availableFilesTable.getValueAt(selectedRow, 1);
                    // String status = (String) availableFilesTable.getValueAt(selectedRow, 2);
                      // Allow adding any file, regardless of analysis status
                    // Add to mergeEntries
                    MergeEntry entry = new MergeEntry();
                    entry.fileId = id;
                    entry.filePath = filePath;
                    entry.startTime = "00:00:00";
                        
                    // Get the duration of the file to set the default end time
                    String duration = getFileDuration(filePath);
                    entry.endTime = duration;
                        
                    mergeEntries.add(entry);
                    updateTableFromEntries();
                        
                    selectorDialog.dispose();
                }
            }
        });
        
        JButton cancelButton = new JButton("Hủy");
        styleButton(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectorDialog.dispose();
            }
        });
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(new JLabel("Chọn một tệp âm thanh để thêm:"), BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        selectorDialog.getContentPane().add(mainPanel);
          // Fetch all available files without restriction
        Query q = new Query();
        q.select("rf.[id]");
        q.select("rf.[dir]");
        q.select("(SELECT COUNT(*) FROM Sentence s WHERE s.recordFileId = rf.id) AS sentenceCount");
        q.from("RecordFile AS rf");
        q.orderBy("rf.[id] DESC");
        
        try {
            QDatabase.select(q, new QReaderFunction() {
                @Override
                public void Fetch(ResultSet rs) throws Exception {
                    int id = rs.getInt(1);
                    String filePath = rs.getString(2);
                    int sentenceCount = rs.getInt(3);
                    
                    String status = sentenceCount > 0 ? "Đã phân tích" : "Chưa phân tích";
                    availableFilesModel.addRow(new Object[]{id, filePath, status});
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lấy tệp: " + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        selectorDialog.setVisible(true);
    }
    
    private void removeSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1) {
            mergeEntries.remove(selectedRow);
            updateTableFromEntries();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một tệp để xóa.",
                    "Không có lựa chọn",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void moveFileUp() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow > 0) {
            MergeEntry entry = mergeEntries.remove(selectedRow);
            mergeEntries.add(selectedRow - 1, entry);
            updateTableFromEntries();
            fileTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        }
    }
    
    private void moveFileDown() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < mergeEntries.size() - 1) {
            MergeEntry entry = mergeEntries.remove(selectedRow);
            mergeEntries.add(selectedRow + 1, entry);
            updateTableFromEntries();
            fileTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }
      private void updateTableFromEntries() {
        tableModel.setRowCount(0);
        
        for (MergeEntry entry : mergeEntries) {
            tableModel.addRow(new Object[]{
                entry.filePath,
                entry.startTime,
                entry.endTime,
                "Xóa"
            });
        }
    }
    
    private void updateEntriesFromTable() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            MergeEntry entry = mergeEntries.get(i);
            entry.startTime = (String) tableModel.getValueAt(i, 1);
            entry.endTime = (String) tableModel.getValueAt(i, 2);
        }
    }
    
    private void browseForOutputFile() {
        FileDialog fileDialog = new FileDialog(this, "Lưu tệp đã gộp", FileDialog.SAVE);
        fileDialog.setFile("*.mp3");
        fileDialog.setVisible(true);
        
        String selectedFile = fileDialog.getFile();
        String selectedDir = fileDialog.getDirectory();
        
        if (selectedFile != null) {
            // Make sure it has .mp3 extension
            if (!selectedFile.toLowerCase().endsWith(".mp3")) {
                selectedFile += ".mp3";
            }
            
            // Set the full path to the output file field
            outputFileField.setText(new File(selectedDir, selectedFile).getAbsolutePath());
        }
    }
    
    private void mergeFiles() {
        if (mergeEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng thêm ít nhất một tệp để gộp.",
                    "Chưa chọn tệp",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        updateEntriesFromTable();
        
        String outputFileName = outputFileField.getText();
        if (outputFileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập tên tệp đầu ra.",
                    "Chưa có tệp đầu ra",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create progress dialog
        JDialog progressDialog = new JDialog(this, "Đang gộp tệp", true);
        progressDialog.setLayout(new BorderLayout());
        progressDialog.setSize(400, 150);
        progressDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel statusLabel = new JLabel("Chuẩn bị gộp các tệp...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        progressDialog.add(panel);
        
        // Start merge in a separate thread so the dialog can be shown
        new Thread(() -> {
            try {
                // Create FFmpeg command
                List<String> command = new ArrayList<>();
                command.add("ffmpeg/ffmpeg.exe");
                command.add("-y"); // Overwrite existing files without asking
                
                StringBuilder filterComplex = new StringBuilder();
                StringBuilder concat = new StringBuilder("concat=n=" + mergeEntries.size() + ":v=0:a=1");
                
                // Add input segments and create filter_complex for each file
                for (int i = 0; i < mergeEntries.size(); i++) {
                    MergeEntry entry = mergeEntries.get(i);
                    
                    // Add input file
                    command.add("-i");
                    command.add(entry.filePath);
                    
                    // Add filter for trimming
                    filterComplex.append("[").append(i).append("]");
                    filterComplex.append("atrim=start=").append(timeToSeconds(entry.startTime));
                    filterComplex.append(":end=").append(timeToSeconds(entry.endTime));
                    filterComplex.append("[a").append(i).append("];");
                }
                
                // Add all trimmed segments to the concat filter
                for (int i = 0; i < mergeEntries.size(); i++) {
                    concat.insert(0, "[a" + i + "]");
                }
                filterComplex.append(concat);
                filterComplex.append("[aout]");
                
                // Add filter_complex and output mapping
                command.add("-filter_complex");
                command.add(filterComplex.toString());
                command.add("-map");
                command.add("[aout]");
                
                // Set output format options
                command.add("-c:a");
                command.add("libmp3lame");
                command.add("-b:a");
                command.add("192k");
                
                // Add output file path
                command.add(outputFileName);
                
                // Update status
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Đang chạy FFmpeg...");
                });
                
                // Start FFmpeg process
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                // Wait for process to complete
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    // Success, save to database
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Đang lưu vào cơ sở dữ liệu...");
                    });
                    
                    // Create a new RecordFile object
                    File outputFile = new File(outputFileName);
                    if (outputFile.exists()) {
                        saveToDatabase(outputFile.getAbsolutePath());
                        
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(MergeFileDialog.this,
                                    "Gộp tệp thành công và đã lưu vào cơ sở dữ liệu.",
                                    "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(MergeFileDialog.this,
                                    "Lỗi: Tệp đầu ra không được tìm thấy sau khi gộp.",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } else {
                    // Error
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(MergeFileDialog.this,
                                "Lỗi khi gộp tệp. FFmpeg trả về mã lỗi: " + exitCode,
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    JOptionPane.showMessageDialog(MergeFileDialog.this,
                            "Lỗi khi gộp tệp: " + e.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
        
        progressDialog.setVisible(true);
    }
    
    private void saveToDatabase(String filePath) throws Exception {
        // Create a new RecordFile object
        RecordFile recordFile = new RecordFile();
        recordFile.id = IdCounterQuery.getIdThenIncrease("RecordFile");
        recordFile.dir = filePath;
        
        // Insert the record file
        QDatabase.insert("RecordFile", recordFile);
    }
    
    /**
     * Convert a time string (HH:MM:SS) to seconds
     */
    private double timeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
    
    /**
     * Get the duration of a file using FFmpeg
     */
    private String getFileDuration(String filePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add("ffprobe");
            command.add("-i");
            command.add(filePath);
            command.add("-show_entries");
            command.add("format=duration");
            command.add("-v");
            command.add("quiet");
            command.add("-of");
            command.add("csv=p=0");
            
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();
            
            // Get the output
            java.util.Scanner scanner = new java.util.Scanner(process.getInputStream());
            String result = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "0";
            
            // Convert to HH:MM:SS
            double seconds = Double.parseDouble(result.trim());
            int hours = (int)(seconds / 3600);
            int minutes = (int)((seconds % 3600) / 60);
            int secs = (int)(seconds % 60);
            
            scanner.close();
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } catch (Exception e) {
            e.printStackTrace();
            return "00:05:00"; // Default to 5 minutes
        }
    }
    
    /**
     * Check if a string is a valid time format (HH:MM:SS)
     */
    private boolean isValidTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return false;
        }
        
        try {
            String[] parts = timeStr.split(":");
            if (parts.length != 3) {
                return false;
            }
            
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            
            return hours >= 0 && minutes >= 0 && minutes < 60 && seconds >= 0 && seconds < 60;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Class to represent a merge entry
     */
    private class MergeEntry {
        public int fileId;
        public String filePath;
        public String startTime; // Format: HH:MM:SS
        public String endTime;   // Format: HH:MM:SS
    }
    
    /**
     * Custom renderer for button columns in the table
     */    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(220, 53, 69));
            setFont(new Font("Arial", Font.BOLD, 12));
            setBorder(BorderFactory.createRaisedBevelBorder());
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            
            // Style adjustments for button appearance
            if (isSelected) {
                setBackground(new Color(200, 33, 49));
            } else {
                setBackground(new Color(220, 53, 69));
            }
            
            // Make the button look more clickable
            setForeground(Color.WHITE);
            setFocusPainted(false);
            return this;
        }
    }
    
    /**
     * Custom editor for button columns in the table
     */
    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;        public ButtonEditor() {
            super(new JTextField());
            setClickCountToStart(1);
            
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(220, 53, 69));
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setBorder(BorderFactory.createRaisedBevelBorder());
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            
            button.addActionListener(_ -> fireEditingStopped());
            
            // Add visual feedback on mouse hover
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(200, 33, 49));
                }
                
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(220, 53, 69));
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                   boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Handle the button click
                SwingUtilities.invokeLater(() -> removeFileAtIndex(row));
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    /**
     * Remove a file from the merge list at the specified index
     * @param index Index of the file to remove
     */
    private void removeFileAtIndex(int index) {
        if (index < 0 || index >= mergeEntries.size()) {
            return;
        }
        
        String fileName = new File(mergeEntries.get(index).filePath).getName();
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Xóa tệp '" + fileName + "' khỏi danh sách gộp?",
            "Xóa tệp",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            mergeEntries.remove(index);
            updateTableFromEntries();
        }
    }
    
    /**
     * Show the dialog
     */
    public void display() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
    
    /**
     * Get all analyzed files from the database
     * @return List of available files for merging
     */
    private List<AvailableFile> getAvailableFiles() {
        List<AvailableFile> files = new ArrayList<>();
        
        Query q = new Query();
        q.select("rf.[id]");
        q.select("rf.[dir]");
        // q.select("(SELECT COUNT(*) FROM Sentence s WHERE s.recordFileId = rf.id) AS sentenceCount");
        q.from("RecordFile AS rf");
        q.orderBy("rf.[id] DESC");
        
        try {
            QDatabase.select(q, new QReaderFunction() {
                @Override
                public void Fetch(ResultSet rs) throws Exception {
                    int id = rs.getInt(1);
                    String filePath = rs.getString(2);
                    // int sentenceCount = rs.getInt(3);
                      // Include all files, regardless of analysis status
                    AvailableFile file = new AvailableFile();
                    file.id = id;
                    file.path = filePath;
                    file.filename = new File(filePath).getName();
                    files.add(file);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lấy tệp: " + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
        
        return files;
    }
    
    /**
     * Simple class to hold available file info
     */
    private class AvailableFile {
        public int id;
        public String path;
        public String filename;
          @Override
        public String toString() {
            // For the prompt item just return the filename
            if (id == -1) {
                return filename;
            }
            // For real files, include the ID
            return "ID: " + id + " - " + filename;
        }
    }
    
    /**
     * Add a file to the merge list from the dropdown selection
     * @param file The selected file to add
     */    private void addFileFromDropdown(AvailableFile file) {
        // Allow adding the same file multiple times
        // Add the file to the merge list
        MergeEntry entry = new MergeEntry();
        entry.fileId = file.id;
        entry.filePath = file.path;
        entry.startTime = "00:00:00";
        
        // Get the duration of the file to set the default end time
        String duration = getFileDuration(file.path);
        entry.endTime = duration;
          mergeEntries.add(entry);
        updateTableFromEntries();
        
        // Highlight the newly added row for better visual feedback
        int newRowIndex = mergeEntries.size() - 1;
        if (newRowIndex >= 0) {
            fileTable.setRowSelectionInterval(newRowIndex, newRowIndex);
            // Ensure the row is visible
            Rectangle rect = fileTable.getCellRect(newRowIndex, 0, true);
            fileTable.scrollRectToVisible(rect);
        }
    }
    
    /**
     * Set up keyboard shortcuts for common operations
     */
    private void setupKeyboardShortcuts() {
        // Create keyboard shortcuts using InputMap and ActionMap
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(); // Ctrl on Windows/Linux, Command on Mac
        
        // Define key bindings
        KeyStroke addFileKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, mask);
        KeyStroke removeFileKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        KeyStroke moveUpKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, mask);
        KeyStroke moveDownKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, mask);
        KeyStroke mergeKey = KeyStroke.getKeyStroke(KeyEvent.VK_M, mask);
        
        // Get the input and action maps for the content pane
        JRootPane rootPane = getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        // Add shortcut to show file selector
        inputMap.put(addFileKey, "addFile");
        actionMap.put("addFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileSelector();
            }
        });
        
        // Add shortcut to remove selected file
        inputMap.put(removeFileKey, "removeFile");
        actionMap.put("removeFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFile();
            }
        });
        
        // Add shortcut to move file up
        inputMap.put(moveUpKey, "moveUp");
        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFileUp();
            }
        });
        
        // Add shortcut to move file down
        inputMap.put(moveDownKey, "moveDown");
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveFileDown();
            }
        });
        
        // Add shortcut to merge files
        inputMap.put(mergeKey, "merge");
        actionMap.put("merge", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mergeFiles();
            }
        });
    }
}