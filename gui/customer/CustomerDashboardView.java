package gui.customer;

import gui.common.ViewManager;
import gui.auth.LoginView;
import businesslogic.entities.Customer;
import businesslogic.entities.enums.MembershipStatus;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard for customers after login.
 * Shows welcome message and quick action buttons.
 * Acts as a navigation hub connecting customers to all features.
 */
public class CustomerDashboardView extends JPanel {
    
    private ViewManager viewManager;
    
    public CustomerDashboardView(ViewManager viewManager) {
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
        JLabel title = new JLabel("Customer Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridy = 0;
        add(title, gbc);
        
        // Welcome message
        Customer customer = viewManager.getCurrentUser();
        String welcomeMessage;
        if (customer != null && customer.getFirstName() != null) {
            String firstName = customer.getFirstName();
            MembershipStatus membership = customer.getMembershipStatus();
            if (membership != null && membership != MembershipStatus.REGULAR) {
                welcomeMessage = "Welcome, " + firstName + " (" + membership + " Member)";
            } else {
                welcomeMessage = "Welcome, " + firstName + "!";
            }
        } else {
            welcomeMessage = "Welcome!";
        }
        
        JLabel welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 15, 30, 15);
        add(welcomeLabel, gbc);
        
        // Navigation buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Search Flights button
        JButton searchFlightsBtn = new JButton("Search Flights");
        searchFlightsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchFlightsBtn.setPreferredSize(new Dimension(250, 40));
        searchFlightsBtn.setMaximumSize(new Dimension(250, 40));
        searchFlightsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        searchFlightsBtn.addActionListener(e -> {
            viewManager.showView("FLIGHT_SEARCH", new FlightSearchView(viewManager));
        });
        buttonPanel.add(searchFlightsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // My Reservations button
        JButton myReservationsBtn = new JButton("My Reservations");
        myReservationsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        myReservationsBtn.setPreferredSize(new Dimension(250, 40));
        myReservationsBtn.setMaximumSize(new Dimension(250, 40));
        myReservationsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        myReservationsBtn.addActionListener(e -> {
            Customer currentCustomer = viewManager.getCurrentUser();
            if (currentCustomer != null) {
                viewManager.showView("MY_RESERVATIONS", 
                    new MyReservationsView(viewManager, currentCustomer));
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
            Customer currentCustomer = viewManager.getCurrentUser();
            if (currentCustomer != null) {
                viewManager.showView("PROFILE_VIEW", 
                    new ProfileView(viewManager, currentCustomer));
            }
        });
        buttonPanel.add(profileBtn);
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
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}

