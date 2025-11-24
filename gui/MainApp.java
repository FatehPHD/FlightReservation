package gui;

import gui.auth.LoginView;
import gui.common.ViewManager;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private ViewManager viewManager;

    public MainApp() {
        setTitle("Flight Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set CardLayout on the content pane (not the JFrame itself)
        Container contentPane = this.getContentPane();
        CardLayout cardLayout = new CardLayout();
        contentPane.setLayout(cardLayout);

        // View Manager handles swapping JPanels
        viewManager = new ViewManager(cardLayout, contentPane);

        // Load first screen
        viewManager.showView("LOGIN", new LoginView(viewManager));

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::new);
    }
}
