package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.enums.MembershipStatus;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard shown after login when Customer role is selected.
 * All users can access flight search, reservations, profile, and promotions.
 * Admin and Agent users can access their respective dashboards by selecting
 * their role before logging in.
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
        
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, gbc);
        
        User currentUser = viewManager.getCurrentUser();
        String welcomeMessage = "Welcome!";
        if (currentUser instanceof Customer) {
            Customer customer = (Customer) currentUser;
            if (customer.getFirstName() != null) {
                String firstName = customer.getFirstName();
                MembershipStatus membership = customer.getMembershipStatus();
                if (membership != null && membership != MembershipStatus.REGULAR) {
                    welcomeMessage = "Welcome, " + firstName + " (" + membership + " Member)!";
                } else {
                    welcomeMessage = "Welcome, " + firstName + "!";
                }
            }
        } else if (currentUser != null && currentUser.getUsername() != null) {
            welcomeMessage = "Welcome, " + currentUser.getUsername() + "!";
        }
        
        JLabel welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 15, 30, 15);
        add(welcomeLabel, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
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
        
        JButton myReservationsBtn = new JButton("My Reservations");
        myReservationsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        myReservationsBtn.setPreferredSize(new Dimension(250, 40));
        myReservationsBtn.setMaximumSize(new Dimension(250, 40));
        myReservationsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        myReservationsBtn.addActionListener(e -> {
            if (currentUser != null) {
                viewManager.showView("MY_RESERVATIONS", 
                    new MyReservationsView(viewManager, currentUser));
            }
        });
        buttonPanel.add(myReservationsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        // Monthly Promotions button
        JButton promotionsBtn = new JButton("Monthly Promotions");
        promotionsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        promotionsBtn.setPreferredSize(new Dimension(250, 40));
        promotionsBtn.setMaximumSize(new Dimension(250, 40));
        promotionsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        promotionsBtn.addActionListener(e -> {
            viewManager.showView("PROMOTION_NEWS", 
                new PromotionNewsView(viewManager));
        });
        buttonPanel.add(promotionsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton profileBtn = new JButton("Profile");
        profileBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileBtn.setPreferredSize(new Dimension(250, 40));
        profileBtn.setMaximumSize(new Dimension(250, 40));
        profileBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        profileBtn.addActionListener(e -> {
            if (currentUser != null) {
                viewManager.showView("PROFILE_VIEW", 
                    new ProfileView(viewManager, currentUser));
            }
        });
        buttonPanel.add(profileBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setPreferredSize(new Dimension(250, 40));
        logoutBtn.setMaximumSize(new Dimension(250, 40));
        logoutBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        logoutBtn.addActionListener(e -> {
            viewManager.logout();
            viewManager.showView("ROLE_SELECTION", new gui.auth.RoleSelectionView(viewManager));
        });
        buttonPanel.add(logoutBtn);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}