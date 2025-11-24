package gui.auth;

import gui.common.ViewManager;
import businesslogic.services.CustomerService;
import businesslogic.entities.Customer;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginView extends JPanel {

    private ViewManager viewManager;
    private CustomerService customerService;

    public LoginView(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        // Get CustomerService from ViewManager (which gets it from ServiceManager)
        this.customerService = viewManager.getCustomerService();
        
        setLayout(new GridBagLayout());

        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField usernameField = new JTextField(25);
        usernameField.setPreferredSize(new Dimension(300, 30));
        JPasswordField passwordField = new JPasswordField(25);
        passwordField.setPreferredSize(new Dimension(300, 30));

        JButton loginBtn = new JButton("Login");
        JButton createAccountBtn = new JButton("Create Account");

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(title, gbc);

        gbc.gridy++;
        add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        add(usernameField, gbc);

        gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridy++;
        add(passwordField, gbc);

        gbc.gridy++;
        add(loginBtn, gbc);
        gbc.gridy++;
        add(createAccountBtn, gbc);

        // Login button action
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Customer customer = customerService.authenticate(username, password);
                if (customer != null) {
                    // Store logged-in customer in ViewManager for later use
                    viewManager.setCurrentUser(customer);
                    
                    JOptionPane.showMessageDialog(this,
                        "Login successful! Welcome, " + customer.getFirstName() + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // TODO: Navigate to customer dashboard
                    // For now, just show success message
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                    passwordField.setText("");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        createAccountBtn.addActionListener(e -> {
            viewManager.showView("SIGNUP", new SignupView(viewManager));
        });
    }
}
