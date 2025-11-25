package gui.common;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * User-friendly error dialog for displaying errors consistently.
 * Provides various error types with appropriate icons and messages.
 */
public class ErrorDialog {
    
    /**
     * Show basic error message.
     * @param parent Parent component
     * @param message Error message
     */
    public static void show(Component parent, String message) {
        show(parent, "Error", message);
    }
    
    /**
     * Show error with custom title.
     * @param parent Parent component
     * @param title Dialog title
     * @param message Error message
     */
    public static void show(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show error with exception details.
     * @param parent Parent component
     * @param message User-friendly message
     * @param ex Exception that occurred
     */
    public static void show(Component parent, String message, Exception ex) {
        String fullMessage = message + "\n\nDetails: " + ex.getMessage();
        JOptionPane.showMessageDialog(
            parent,
            fullMessage,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show error with expandable exception details.
     * @param parent Parent component
     * @param message User-friendly message
     * @param ex Exception that occurred
     */
    public static void showDetailed(Component parent, String message, Exception ex) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Main message
        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(messageLabel, BorderLayout.NORTH);
        
        // Exception details in collapsible section
        JTextArea detailsArea = new JTextArea(getStackTrace(ex), 10, 50);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        detailsArea.setBackground(new Color(245, 245, 245));
        detailsArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Technical Details"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(
            parent,
            panel,
            "Error Details",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show database error (common in this application).
     * @param parent Parent component
     * @param operation What operation failed (e.g., "loading flights")
     * @param ex Exception that occurred
     */
    public static void showDatabaseError(Component parent, String operation, Exception ex) {
        String message = "Database error while " + operation + ".\n" +
                        "Please check your connection and try again.\n\n" +
                        "Error: " + ex.getMessage();
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show validation error (for form input validation).
     * @param parent Parent component
     * @param message Validation message
     */
    public static void showValidation(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show authentication error.
     * @param parent Parent component
     */
    public static void showAuthError(Component parent) {
        String message = "Invalid username or password.\n" +
                        "Please try again.";
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Authentication Failed",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show permission denied error.
     * @param parent Parent component
     * @param action What action was denied
     */
    public static void showPermissionDenied(Component parent, String action) {
        String message = "You do not have permission to " + action + ".\n" +
                        "Please contact an administrator.";
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Permission Denied",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show "not found" error.
     * @param parent Parent component
     * @param itemType Type of item (e.g., "Flight")
     * @param identifier Item identifier
     */
    public static void showNotFound(Component parent, String itemType, String identifier) {
        String message = itemType + " '" + identifier + "' was not found.\n" +
                        "It may have been deleted or does not exist.";
        JOptionPane.showMessageDialog(
            parent,
            message,
            itemType + " Not Found",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show "no seats available" error.
     * @param parent Parent component
     * @param flightNumber Flight number
     */
    public static void showNoSeatsAvailable(Component parent, String flightNumber) {
        String message = "No seats available on flight " + flightNumber + ".\n" +
                        "Please choose a different flight.";
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Flight Full",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show payment failure error.
     * @param parent Parent component
     * @param reason Reason for failure
     */
    public static void showPaymentFailed(Component parent, String reason) {
        String message = "Payment failed: " + reason + "\n" +
                        "Please check your payment details and try again.";
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Payment Failed",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show generic warning message.
     * @param parent Parent component
     * @param message Warning message
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Warning",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Show information message (not really an error, but useful utility).
     * @param parent Parent component
     * @param message Information message
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Information",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Show success message (positive feedback).
     * @param parent Parent component
     * @param message Success message
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Get stack trace as string from exception.
     */
    private static String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
