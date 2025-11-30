package gui.customer;

import gui.common.ViewManager;
import gui.auth.LoginView;
import gui.admin.AdminDashboardView;
import gui.agent.AgentDashboardView;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.UserRole;

import javax.swing.*;
import java.awt.*;

/**
 * Main dashboard shown after login.
 * Displays role-specific navigation buttons (Admin/Agent dashboards for those roles).
 * All users can access flight search, reservations, and profile.
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
        
        UserRole userRole = currentUser != null ? currentUser.getRole() : null;
        
        if (userRole == UserRole.SYSTEM_ADMIN) {
            JButton adminDashboardBtn = new JButton("Admin Dashboard");
            adminDashboardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            adminDashboardBtn.setPreferredSize(new Dimension(250, 40));
            adminDashboardBtn.setMaximumSize(new Dimension(250, 40));
            adminDashboardBtn.setFont(new Font("Arial", Font.PLAIN, 16));
            adminDashboardBtn.addActionListener(e -> {
                viewManager.showView("ADMIN_DASHBOARD", 
                    new AdminDashboardView(viewManager));
            });
            buttonPanel.add(adminDashboardBtn);
            buttonPanel.add(Box.createVerticalStrut(15));
        }
        
        if (userRole == UserRole.FLIGHT_AGENT) {
            JButton agentDashboardBtn = new JButton("Agent Dashboard");
            agentDashboardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            agentDashboardBtn.setPreferredSize(new Dimension(250, 40));
            agentDashboardBtn.setMaximumSize(new Dimension(250, 40));
            agentDashboardBtn.setFont(new Font("Arial", Font.PLAIN, 16));
            agentDashboardBtn.addActionListener(e -> {
                viewManager.showView("AGENT_DASHBOARD", 
                    new AgentDashboardView(viewManager));
            });
            buttonPanel.add(agentDashboardBtn);
            buttonPanel.add(Box.createVerticalStrut(15));
        }
        
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
            viewManager.showView("LOGIN", new LoginView(viewManager));
        });
        buttonPanel.add(logoutBtn);
        
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}

