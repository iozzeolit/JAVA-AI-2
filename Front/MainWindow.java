package Front;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {

    private static final String APP_TITLE = "Nhận diện giọng nói bằng Java";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    public MainWindow() {
        setTitle(APP_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel titlePanel = createTitlePanel();

        getContentPane().add(titlePanel);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();

        panel.setBackground(new Color(250, 255, 255));

        JLabel titleLabel = new JLabel("Nhận diện giọng nói bằng Java", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(50, 50, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0));

        JLabel subtitleLabel = new JLabel("Chào mừng bạn đến với ứng dụng nhận diện giọng nói bằng Java",
                JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(100, 100, 100));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton fileListButton = new JButton("Bắt đầu");
        fileListButton.setFont(new Font("Arial", Font.BOLD, 16));
        fileListButton.setBackground(new Color(70, 70, 70));
        fileListButton.setForeground(Color.WHITE);
        fileListButton.setFocusPainted(false);
        fileListButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        fileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileListWindow();
            }
        });

        JButton exitButton = new JButton("Thoát");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setBackground(new Color(220, 50, 50));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.add(fileListButton);
        buttonPanel.add(exitButton);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void openFileListWindow() {
        FileListWindow fileListWindow = new FileListWindow(this);
        this.setVisible(false);
        fileListWindow.display();
    }

    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
