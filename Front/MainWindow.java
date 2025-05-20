package Front;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main window class for the Java-AI application
 * Displays the title page of the application
 */
public class MainWindow extends JFrame {
    
    // Title constants
    private static final String APP_TITLE = "Java-AI Application";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    /**
     * Constructor for the MainWindow
     * Sets up the main frame and components
     */
    public MainWindow() {
        // Set up the frame properties
        setTitle(APP_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        
        // Create the title panel
        JPanel titlePanel = createTitlePanel();
        
        // Add panel to the frame
        getContentPane().add(titlePanel);
    }
      /**
     * Creates the title panel with welcome text and styling
     * @return JPanel containing the title components
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(240, 240, 255)); // Light blue background
        
        // Create title label
        JLabel titleLabel = new JLabel("Nhận diện giọng nói", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(50, 50, 150)); // Dark blue text
        titleLabel.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0)); // Add some top padding
        
        // Create subtitle
        JLabel subtitleLabel = new JLabel("Chào mừng bạn đến với ứng dụng nhận diện giọng nói", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(100, 100, 100)); // Gray text
        
        // Create a panel for the text components
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false); // Make it transparent
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Add spacing
        textPanel.add(subtitleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 40))); // Add more spacing
        
        // Create file list button
        JButton fileListButton = new JButton("Quản lý tệp MP3");
        fileListButton.setFont(new Font("Arial", Font.BOLD, 16));
        fileListButton.setBackground(new Color(70, 130, 180)); // Steel blue
        fileListButton.setForeground(Color.WHITE);
        fileListButton.setFocusPainted(false);
        fileListButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));        // Add action listener to open file list window
        fileListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileListWindow();
            }
        });
        
        // Create exit button
        JButton exitButton = new JButton("Thoát");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setBackground(new Color(220, 50, 50)); // Red
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Exit the application
            }
        });
        
        // Create a panel for the action buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 40, 40, 40));
        buttonPanel.setBackground(new Color(240, 240, 255));
        buttonPanel.add(fileListButton);
        buttonPanel.add(exitButton);

        // Add components to main panel
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Opens the file list window
     */
    private void openFileListWindow() {
        FileListWindow fileListWindow = new FileListWindow(this);
        fileListWindow.display();
    }
    
    /**
     * Display the window
     */
    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
