package Front;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import External.*;
import Object.*;
import Back.*;
import AI.UnicodeHelper;

public class SentenceEditorDialog extends JDialog {
    private JTable sentenceTable;
    private DefaultTableModel tableModel;
    private final int fileId;
    private List<Sentence> sentences;

    public SentenceEditorDialog(JFrame parent, int fileId) {
        super(parent, "Edit Sentences", true);
        this.fileId = fileId;

        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Chỉnh sửa các câu", JLabel.CENTER);
        titleLabel.setFont(new Font("", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        String[] columnNames = { "ID", "Bắt đầu", "Kết thúc", "Nội dung" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };

        sentenceTable = new JTable(tableModel);
        sentenceTable.setFont(new Font("", Font.PLAIN, 14));
        sentenceTable.setRowHeight(30);
        sentenceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        sentenceTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        sentenceTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        sentenceTable.getColumnModel().getColumn(3).setPreferredWidth(500);

        JScrollPane scrollPane = new JScrollPane(sentenceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton addButton = new JButton("Thêm câu");
        styleButton(addButton);
        addButton.addActionListener(_ -> addSentence());

        JButton deleteButton = new JButton("Xóa câu đã chọn");
        styleButton(deleteButton);
        deleteButton.addActionListener(_ -> deleteSentence());

        JButton saveButton = new JButton("Lưu thay đổi");
        styleButton(saveButton);
        saveButton.addActionListener(_ -> saveChanges());

        JButton closeButton = new JButton("Đóng");
        styleButton(closeButton);
        closeButton.addActionListener(_ -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);

        loadSentences();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("", Font.BOLD, 14));
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void loadSentences() {
        sentences = new ArrayList<>();
        tableModel.setRowCount(0);
        Query q = new Query();
        q.select("*");
        q.from("Sentence");
        q.where("recordFileId = " + fileId);
        q.orderBy("startTime ASC");

        try {
            QDatabase.select(q, new QReaderFunction() {
                @Override
                public void Fetch(java.sql.ResultSet rs) throws Exception {
                    Sentence sentence = new Sentence();
                    sentence.id = QReader.getInt(rs, 1);
                    sentence.recordFileId = QReader.getInt(rs, 2);
                    sentence.startTime = QReader.getTime(rs, 3);
                    sentence.endTime = QReader.getTime(rs, 4);
                    sentence.content = QReader.getString(rs, 5);

                    sentences.add(sentence);

                    Object[] rowData = {
                            sentence.id,
                            formatTime(timeToSeconds(sentence.startTime)),
                            formatTime(timeToSeconds(sentence.endTime)),
                            sentence.content
                    };
                    tableModel.addRow(rowData);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading sentences: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d:%02d", hours, minutes, secs);
    }

    private double parseTime(String timeStr) {
        String[] parts = timeStr.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private void addSentence() {

        Sentence sentence = new Sentence();
        sentence.id = IdCounterQuery.getIdThenIncrease("Sentence");
        sentence.recordFileId = fileId;
        sentence.startTime = secondsToTime(0);
        sentence.endTime = secondsToTime(0);
        sentence.content = "";

        sentences.add(sentence);
        Object[] rowData = {
                sentence.id,
                formatTime(0),
                formatTime(0),
                sentence.content
        };
        tableModel.addRow(rowData);

        int newRow = tableModel.getRowCount() - 1;
        sentenceTable.setRowSelectionInterval(newRow, newRow);

        sentenceTable.editCellAt(newRow, 3);
        sentenceTable.getEditorComponent().requestFocus();
    }

    private void deleteSentence() {
        int selectedRow = sentenceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một câu để xóa.",
                    "Không có lựa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sentenceId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa câu ID " + sentenceId + "?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {

                Query deleteQuery = new Query();
                deleteQuery.from("Sentence");
                deleteQuery.where("id = " + sentenceId);
                QDatabase.delete(deleteQuery);

                for (int i = 0; i < sentences.size(); i++) {
                    if (sentences.get(i).id == sentenceId) {
                        sentences.remove(i);
                        break;
                    }
                }

                tableModel.removeRow(selectedRow);

                JOptionPane.showMessageDialog(this,
                        "Câu đã được xóa thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi xóa câu: " + e.getMessage(),
                        "Lỗi cơ sở dữ liệu",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void saveChanges() {
        try {

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int id = (int) tableModel.getValueAt(i, 0);
                String startTimeStr = (String) tableModel.getValueAt(i, 1);
                String endTimeStr = (String) tableModel.getValueAt(i, 2);
                String content = (String) tableModel.getValueAt(i, 3);

                for (Sentence sentence : sentences) {
                    if (sentence.id == id) {

                        double startSeconds = parseTime(startTimeStr);
                        double endSeconds = parseTime(endTimeStr);
                        Query q = new Query();
                        q.update("Sentence");
                        q.set("startTime = '" + secondsToTime(startSeconds) + "'");
                        q.set("endTime = '" + secondsToTime(endSeconds) + "'");
                        String escapedContent = UnicodeHelper.escapeSqlApostrophes(content);

                        q.set("content = N'" + escapedContent + "'");

                        q.where("id = " + id);
                        QDatabase.update(q);
                        break;
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Đã lưu thay đổi.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi lưu thay đổi: " + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private double timeToSeconds(Time time) {
        if (time == null) {
            return 0;
        }
        return time.getTime() / 1000.0;
    }

    private Time secondsToTime(double seconds) {
        return new Time((long) (seconds * 1000));
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
