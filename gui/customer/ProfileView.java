package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import javax.swing.*;
import java.awt.*;

/**
 * User profile view for viewing and editing account information.
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
        JTextField usernameField = new JTextField(user.getUsername());
        usernameField.setEditable(false);
        usernameField.setPreferredSize(new Dimension(300, 30));
        profilePanel.add(usernameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 0;
        profilePanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JTextField emailField = new JTextField(user.getEmail() != null ? user.getEmail() : "");
        emailField.setPreferredSize(new Dimension(300, 30));
        profilePanel.add(emailField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 0;
        profilePanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JTextField roleField = new JTextField(user.getRole().toString());
        roleField.setEditable(false);
        roleField.setPreferredSize(new Dimension(300, 30));
        profilePanel.add(roleField, gbc);
        
        if (user instanceof Customer) {
            Customer customer = (Customer) user;
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("First Name:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JTextField firstNameField = new JTextField(
                customer.getFirstName() != null ? customer.getFirstName() : "");
            firstNameField.setPreferredSize(new Dimension(300, 30));
            profilePanel.add(firstNameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Last Name:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JTextField lastNameField = new JTextField(
                customer.getLastName() != null ? customer.getLastName() : "");
            lastNameField.setPreferredSize(new Dimension(300, 30));
            profilePanel.add(lastNameField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JTextField phoneField = new JTextField(
                customer.getPhone() != null ? customer.getPhone() : "");
            phoneField.setPreferredSize(new Dimension(300, 30));
            profilePanel.add(phoneField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Address:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JTextField addressField = new JTextField(
                customer.getAddress() != null ? customer.getAddress() : "");
            addressField.setPreferredSize(new Dimension(300, 30));
            profilePanel.add(addressField, gbc);
            
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 0;
            profilePanel.add(new JLabel("Membership Status:"), gbc);
            gbc.gridx = 1;
            gbc.ipadx = 200;
            JTextField membershipField = new JTextField(
                customer.getMembershipStatus() != null ? customer.getMembershipStatus().toString() : "N/A");
            membershipField.setEditable(false);
            membershipField.setPreferredSize(new Dimension(300, 30));
            profilePanel.add(membershipField, gbc);
        }
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 0;
        gbc.insets = new Insets(20, 15, 10, 15);
        profilePanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        gbc.ipadx = 200;
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(300, 30));
        profilePanel.add(passwordField, gbc);
        
        JScrollPane scrollPane = new JScrollPane(profilePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setPreferredSize(new Dimension(150, 35));
        saveBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Profile update functionality will be implemented here.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", 
                new CustomerDashboardView(viewManager));
        });
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

