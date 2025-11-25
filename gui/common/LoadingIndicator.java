package gui.common;

import javax.swing.*;
import java.awt.*;

/**
 * Loading indicator for database operations.
 * Shows a modal dialog with progress bar or spinner.
 */
public class LoadingIndicator extends JDialog {
    
    private JProgressBar progressBar;
    private JLabel messageLabel;
    private boolean indeterminate;
    
    /**
     * Create loading indicator with message.
     * @param parent Parent frame
     * @param message Loading message
     */
    public LoadingIndicator(JFrame parent, String message) {
        this(parent, message, true);
    }
    
    /**
     * Create loading indicator.
     * @param parent Parent frame
     * @param message Loading message
     * @param indeterminate True for spinning indicator, false for progress bar
     */
    public LoadingIndicator(JFrame parent, String message, boolean indeterminate) {
        super(parent, "Loading", true);
        this.indeterminate = indeterminate;
        initComponents(message);
    }
    
    /**
     * Initialize components.
     */
    private void initComponents(String message) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        panel.setBackground(Color.WHITE);
        
        // Message label
        messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(messageLabel, BorderLayout.NORTH);
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(indeterminate);
        progressBar.setPreferredSize(new Dimension(300, 25));
        panel.add(progressBar, BorderLayout.CENTER);
        
        setContentPane(panel);
        pack();
        setLocationRelativeTo(getParent());
    }
    
    /**
     * Show the loading dialog.
     * Call this in a separate thread or use SwingWorker.
     */
    public void showLoading() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
    
    /**
     * Hide the loading dialog.
     */
    public void hideLoading() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }
    
    /**
     * Update the loading message.
     * @param message New message
     */
    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }
    
    /**
     * Set progress value (only works if not indeterminate).
     * @param value Progress value (0-100)
     */
    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            if (!indeterminate) {
                progressBar.setValue(value);
            }
        });
    }
    
    /**
     * Set maximum progress value (only works if not indeterminate).
     * @param max Maximum value
     */
    public void setMaximum(int max) {
        SwingUtilities.invokeLater(() -> {
            if (!indeterminate) {
                progressBar.setMaximum(max);
            }
        });
    }
    
    /**
     * Create and show a simple loading dialog.
     * Returns the dialog so you can hide it later.
     * 
     * @param parent Parent frame
     * @param message Loading message
     * @return LoadingIndicator instance
     */
    public static LoadingIndicator show(JFrame parent, String message) {
        LoadingIndicator indicator = new LoadingIndicator(parent, message);
        
        // Show in separate thread to not block
        new Thread(() -> indicator.showLoading()).start();
        
        return indicator;
    }
    
    /**
     * Execute a task with loading indicator.
     * Automatically shows/hides the indicator.
     * 
     * @param parent Parent frame
     * @param message Loading message
     * @param task Task to execute
     */
    public static void execute(JFrame parent, String message, Runnable task) {
        LoadingIndicator indicator = new LoadingIndicator(parent, message);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                task.run();
                return null;
            }
            
            @Override
            protected void done() {
                indicator.hideLoading();
            }
        };
        
        worker.execute();
        indicator.showLoading();
    }
}
