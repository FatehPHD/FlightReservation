package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import javax.swing.*;
import java.awt.*;

/**
 * User profile view for viewing account information (read-only).
 * Shows different fields based on user type (Customer has extra fields).
 */
public class ProfileView extends JPanel {
    
    private ViewManager viewManager;
    private User user;
    
    public ProfileView(ViewManager viewManager, User user) {
        this.viewManager = viewManager;
        this.user = user;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JLabel title = new JLabel("Profile");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        profilePanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JLabel usernameLabel = new JLabel(user.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(usernameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 0;
        profilePanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JLabel emailLabel = new JLabel(user.getEmail() != null ? user.getEmail() : "N/A");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(emailLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 0;
        profilePanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JLabel roleLabel = new JLabel(user.getRole().toString());
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePanel.add(roleLabel, gbc);
        
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("First Name:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JLabel firstNameLabel = new JLabel(
                customer.getFirstName() != null ? customer.getFirstName() : "N/A");
            firstNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profilePanel.add(firstNameLabel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Last Name:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JLabel lastNameLabel = new JLabel(
                customer.getLastName() != null ? customer.getLastName() : "N/A");
            lastNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profilePanel.add(lastNameLabel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JLabel phoneLabel = new JLabel(
                customer.getPhone() != null ? customer.getPhone() : "N/A");
            phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profilePanel.add(phoneLabel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Address:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JLabel addressLabel = new JLabel(
                customer.getAddress() != null ? customer.getAddress() : "N/A");
            addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profilePanel.add(addressLabel, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Membership Status:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JLabel membershipLabel = new JLabel(
                customer.getMembershipStatus() != null ? customer.getMembershipStatus().toString() : "N/A");
            membershipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profilePanel.add(membershipLabel, gbc);
        }
        
        JScrollPane scrollPane = new JScrollPane(profilePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", 
                new CustomerDashboardView(viewManager));
        });
        
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

