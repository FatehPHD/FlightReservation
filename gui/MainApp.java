package gui;

import gui.auth.LoginView;
import gui.common.ViewManager;
import gui.common.ServiceManager;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Main application entry point.
 * Initializes services and sets up the GUI with CardLayout navigation.
 */
public class MainApp extends JFrame {

    private ViewManager viewManager;

    public MainApp() {
        setTitle("Flight Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        ServiceManager serviceManager;
        try {
            serviceManager = new ServiceManager();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to initialize services: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        
        Container contentPane = this.getContentPane();
        CardLayout cardLayout = new CardLayout();
        contentPane.setLayout(cardLayout);

        viewManager = new ViewManager(cardLayout, contentPane, serviceManager);
        viewManager.showView("LOGIN", new LoginView(viewManager));

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}
