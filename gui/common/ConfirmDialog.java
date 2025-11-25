package gui.common;

import javax.swing.*;
import java.awt.*;

/**
 * Confirmation dialog for destructive or important actions.
 * Provides various types of confirmation dialogs with consistent styling.
 */
public class ConfirmDialog {
    
    /**
     * Show a basic confirmation dialog with Yes/No buttons.
     * @param parent Parent component
     * @param message Confirmation message
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean show(Component parent, String message) {
        return show(parent, "Confirm", message);
    }
    
    /**
     * Show confirmation dialog with custom title.
     * @param parent Parent component
     * @param title Dialog title
     * @param message Confirmation message
     * @return true if user clicked Yes, false otherwise
     */
    public static boolean show(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation for deletion action.
     * @param parent Parent component
     * @param itemName Name of item being deleted
     * @return true if user confirmed deletion
     */
    public static boolean showDelete(Component parent, String itemName) {
        String message = "Are you sure you want to delete '" + itemName + "'?\n" +
                        "This action cannot be undone.";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation for cancellation action.
     * @param parent Parent component
     * @param itemName Name of item being cancelled
     * @return true if user confirmed cancellation
     */
    public static boolean showCancel(Component parent, String itemName) {
        String message = "Are you sure you want to cancel '" + itemName + "'?\n" +
                        "You may be eligible for a refund.";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation with Yes/No/Cancel options.
     * @param parent Parent component
     * @param title Dialog title
     * @param message Confirmation message
     * @return 0 for Yes, 1 for No, 2 for Cancel
     */
    public static int showYesNoCancel(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            title,
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) return 0;
        if (result == JOptionPane.NO_OPTION) return 1;
        return 2; // Cancel or closed
    }
    
    /**
     * Show confirmation for logout action.
     * @param parent Parent component
     * @return true if user confirmed logout
     */
    public static boolean showLogout(Component parent) {
        String message = "Are you sure you want to logout?";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation for exit application.
     * @param parent Parent component
     * @return true if user confirmed exit
     */
    public static boolean showExit(Component parent) {
        String message = "Are you sure you want to exit the application?";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Exit Application",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation with detailed information.
     * @param parent Parent component
     * @param title Dialog title
     * @param message Main message
     * @param details Additional details
     * @return true if user confirmed
     */
    public static boolean showWithDetails(Component parent, String title, 
                                         String message, String details) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Main message
        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(messageLabel, BorderLayout.NORTH);
        
        // Details in scrollable text area
        JTextArea detailsArea = new JTextArea(details, 5, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        detailsArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            panel,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation for overwrite action.
     * @param parent Parent component
     * @param itemName Name of item being overwritten
     * @return true if user confirmed overwrite
     */
    public static boolean showOverwrite(Component parent, String itemName) {
        String message = "'" + itemName + "' already exists.\n" +
                        "Do you want to overwrite it?";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm Overwrite",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation for payment action.
     * @param parent Parent component
     * @param amount Payment amount
     * @param currency Currency symbol (e.g., "$")
     * @return true if user confirmed payment
     */
    public static boolean showPayment(Component parent, double amount, String currency) {
        String message = String.format(
            "You are about to make a payment of %s%.2f\n" +
            "Do you want to proceed?",
            currency, amount
        );
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation with custom buttons.
     * @param parent Parent component
     * @param title Dialog title
     * @param message Confirmation message
     * @param option1 First button text
     * @param option2 Second button text
     * @return true if first option selected, false if second option
     */
    public static boolean showCustom(Component parent, String title, String message,
                                    String option1, String option2) {
        Object[] options = {option1, option2};
        int result = JOptionPane.showOptionDialog(
            parent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        return result == 0; // First option selected
    }
}
