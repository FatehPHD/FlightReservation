package gui;

import gui.auth.LoginView;
import gui.common.ViewManager;
import gui.common.ServiceManager;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainApp extends JFrame {

    private ViewManager viewManager;

    public MainApp() {
        setTitle("Flight Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize service manager (creates all services)
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
        
        // Set CardLayout on the content pane (not the JFrame itself)
        Container contentPane = this.getContentPane();
        CardLayout cardLayout = new CardLayout();
        contentPane.setLayout(cardLayout);

        // View Manager handles swapping JPanels and provides access to services
        viewManager = new ViewManager(cardLayout, contentPane, serviceManager);

        // Load first screen
        viewManager.showView("LOGIN", new LoginView(viewManager));

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}
