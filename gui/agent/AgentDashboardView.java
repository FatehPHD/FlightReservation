package gui.agent;

import gui.common.ViewManager;
import gui.customer.CustomerDashboardView;
import businesslogic.entities.User;

import javax.swing.*;
import java.awt.*;

/**
 * Agent dashboard - navigation to customer and reservation management.
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
        
        JLabel title = new JLabel("Agent Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, gbc);
        
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
        
        JLabel toolsLabel = new JLabel("Agent Tools:");
        toolsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(toolsLabel, gbc);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton manageCustomersBtn = new JButton("Manage Customers");
        manageCustomersBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageCustomersBtn.setPreferredSize(new Dimension(250, 40));
        manageCustomersBtn.setMaximumSize(new Dimension(250, 40));
        manageCustomersBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageCustomersBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_CUSTOMERS", 
                new ManageCustomersView(viewManager));
        });
        buttonPanel.add(manageCustomersBtn);
        buttonPanel.add(Box.createVerticalStrut(15));
        
        JButton manageReservationsBtn = new JButton("Manage Reservations");
        manageReservationsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageReservationsBtn.setPreferredSize(new Dimension(250, 40));
        manageReservationsBtn.setMaximumSize(new Dimension(250, 40));
        manageReservationsBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        manageReservationsBtn.addActionListener(e -> {
            viewManager.showView("MANAGE_RESERVATIONS", 
                new ManageReservationsView(viewManager));
        });
        buttonPanel.add(manageReservationsBtn);
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
        
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 15, 15, 15);
        add(buttonPanel, gbc);
    }
}
