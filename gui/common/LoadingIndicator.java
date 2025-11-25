package gui.common;

import javax.swing.*;

/**
 * Loading indicator component.
 * Shows progress during database operations.
 */
public class LoadingIndicator extends JDialog {
    
    private JProgressBar progressBar;
    
    public LoadingIndicator(JFrame parent, String message) {
        super(parent, "Loading", true);
        initComponents(message);
    }
    
    private void initComponents(String message) {
        // TODO: Implement loading indicator
    }
    
    public void showLoading() {
        // TODO: Show loading dialog
    }
    
    public void hideLoading() {
        // TODO: Hide loading dialog
    }
}

