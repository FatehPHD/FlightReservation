package gui.agent;

import gui.common.ViewManager;
import gui.auth.LoginView;
import gui.customer.CustomerDashboardView;
import businesslogic.entities.User;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard for flight agents.
 * Shows agent tools and quick actions.
 * Agents can perform all customer functions plus agent-specific tasks.
 */
public class AgentDashboardView extends JPanel {
    
    private ViewManager viewManager;
    
    public AgentDashboardView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Title
        JLabel title = new JLabel("Agent Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridy = 0;
        add(title, gbc);
        
        // Welcome message
        User currentUser = viewManager.getCurrentUser();
        String welcomeMessage = "Welcome, Agent!";
        if (currentUser != null && currentUser.getUsername() != null) {
            welcomeMessage = "Welcome, " + currentUser.getUsername() + "!";
        }
        
        JLabel welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 15, 30, 15);
        add(welcomeLabel, gbc);
        
        // Agent tools label
        JLabel toolsLabel = new JLabel("Agent Tools:");
        toolsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(toolsLabel, gbc);
        
        // Navigation buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Search Flights button (agents can search and book for customers)
        JButton searchFlightsBtn = new JButton("Search Flights");
        searchFlightsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchFlightsBtn.setPreferredSize(new Dimension(250, 40));
        searchFlightsBtn.setMaximumSize(new Dimension(250, 40));
        searchFlightsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        searchFlightsBtn.addActionListener(e -> {
            viewManager.showView("FLIGHT_SEARCH", 
                new gui.customer.FlightSearchView(viewManager));
        });
        buttonPanel.add(searchFlightsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // My Reservations button (agents can view their own reservations)
        JButton myReservationsBtn = new JButton("My Reservations");
        myReservationsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        myReservationsBtn.setPreferredSize(new Dimension(250, 40));
        myReservationsBtn.setMaximumSize(new Dimension(250, 40));
        myReservationsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        myReservationsBtn.addActionListener(e -> {
            if (currentUser != null) {
                viewManager.showView("MY_RESERVATIONS", 
                    new gui.customer.MyReservationsView(viewManager, currentUser));
            }
        });
        buttonPanel.add(myReservationsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // Profile button
        JButton profileBtn = new JButton("Profile");
        profileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileBtn.setPreferredSize(new Dimension(250, 40));
        profileBtn.setMaximumSize(new Dimension(250, 40));
        profileBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        profileBtn.addActionListener(e -> {
            if (currentUser != null) {
                viewManager.showView("PROFILE_VIEW", 
                    new gui.customer.ProfileView(viewManager, currentUser));
            }
        });
        buttonPanel.add(profileBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // Back to Main Dashboard button
        JButton backToMainBtn = new JButton("Back to Main Dashboard");
        backToMainBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToMainBtn.setPreferredSize(new Dimension(250, 40));
        backToMainBtn.setMaximumSize(new Dimension(250, 40));
        backToMainBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        backToMainBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", 
                new CustomerDashboardView(viewManager));
        });
        buttonPanel.add(backToMainBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setPreferredSize(new Dimension(250, 40));
        logoutBtn.setMaximumSize(new Dimension(250, 40));
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        logoutBtn.addActionListener(e -> {
            // Clear session data
            viewManager.logout();
            // Navigate to login
            viewManager.showView("LOGIN", new LoginView(viewManager));
        });
        buttonPanel.add(logoutBtn);
        
        // Add button panel to main layout
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}
