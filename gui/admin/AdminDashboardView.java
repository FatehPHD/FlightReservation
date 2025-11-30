package gui.admin;

import gui.common.ViewManager;
import gui.auth.LoginView;
import gui.customer.CustomerDashboardView;
import businesslogic.entities.User;

import javax.swing.*;
import java.awt.*;

/**
 * Admin dashboard - navigation hub for all admin management screens.
 * All database operations happen in the individual management views via AdminService.
 */
public class AdminDashboardView extends JPanel {
    
    private ViewManager viewManager;
    
    public AdminDashboardView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridwidth = 2;
        add(title, gbc);
        gbc.gridwidth = 1;
        
        User currentUser = viewManager.getCurrentUser();
        String welcomeMessage = "Welcome, Administrator!";
        if (currentUser != null && currentUser.getUsername() != null) {
            welcomeMessage = "Welcome, " + currentUser.getUsername() + "!";
        }
        
        JLabel welcomeLabel = new JLabel(welcomeMessage);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 15, 30, 15);
        add(welcomeLabel, gbc);
        
        JLabel optionsLabel = new JLabel("Management Options:");
        optionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(optionsLabel, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton manageFlightsBtn = new JButton("Manage Flights");
        manageFlightsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageFlightsBtn.setPreferredSize(new Dimension(250, 40));
        manageFlightsBtn.setMaximumSize(new Dimension(250, 40));
        manageFlightsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageFlightsBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_FLIGHTS", 
                new ManageFlightsView(viewManager));
        });
        buttonPanel.add(manageFlightsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton manageAircraftBtn = new JButton("Manage Aircraft");
        manageAircraftBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageAircraftBtn.setPreferredSize(new Dimension(250, 40));
        manageAircraftBtn.setMaximumSize(new Dimension(250, 40));
        manageAircraftBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageAircraftBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_AIRCRAFT", 
                new ManageAircraftView(viewManager));
        });
        buttonPanel.add(manageAircraftBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton manageAirlinesBtn = new JButton("Manage Airlines");
        manageAirlinesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageAirlinesBtn.setPreferredSize(new Dimension(250, 40));
        manageAirlinesBtn.setMaximumSize(new Dimension(250, 40));
        manageAirlinesBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageAirlinesBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_AIRLINES", 
                new ManageAirlinesView(viewManager));
        });
        buttonPanel.add(manageAirlinesBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton manageAirportsBtn = new JButton("Manage Airports");
        manageAirportsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageAirportsBtn.setPreferredSize(new Dimension(250, 40));
        manageAirportsBtn.setMaximumSize(new Dimension(250, 40));
        manageAirportsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageAirportsBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_AIRPORTS", 
                new ManageAirportsView(viewManager));
        });
        buttonPanel.add(manageAirportsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton manageRoutesBtn = new JButton("Manage Routes");
        manageRoutesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageRoutesBtn.setPreferredSize(new Dimension(250, 40));
        manageRoutesBtn.setMaximumSize(new Dimension(250, 40));
        manageRoutesBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageRoutesBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_ROUTES", 
                new ManageRoutesView(viewManager));
        });
        buttonPanel.add(manageRoutesBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton reportsBtn = new JButton("Reports");
        reportsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        reportsBtn.setPreferredSize(new Dimension(250, 40));
        reportsBtn.setMaximumSize(new Dimension(250, 40));
        reportsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        reportsBtn.addActionListener(e -> {
            viewManager.showView("REPORTS", 
                new ReportsView(viewManager));
        });
        buttonPanel.add(reportsBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
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
        
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}
