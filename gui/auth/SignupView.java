package gui.auth;

import gui.common.ViewManager;
import businesslogic.services.CustomerService;
import businesslogic.entities.Customer;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class SignupView extends JPanel {

    private ViewManager viewManager;
    private CustomerService customerService;

    public SignupView(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        // Get CustomerService from ViewManager (which gets it from ServiceManager)
        this.customerService = viewManager.getCustomerService();
        
        setLayout(new GridBagLayout());

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField usernameField = new JTextField(25);
        usernameField.setPreferredSize(new Dimension(300, 30));
        JTextField emailField = new JTextField(25);
        emailField.setPreferredSize(new Dimension(300, 30));
        JTextField firstNameField = new JTextField(25);
        firstNameField.setPreferredSize(new Dimension(300, 30));
        JTextField lastNameField = new JTextField(25);
        lastNameField.setPreferredSize(new Dimension(300, 30));
        JPasswordField passwordField = new JPasswordField(25);
        passwordField.setPreferredSize(new Dimension(300, 30));
        JPasswordField confirmPasswordField = new JPasswordField(25);
        confirmPasswordField.setPreferredSize(new Dimension(300, 30));

        JButton createBtn = new JButton("Sign Up");
        JButton backBtn = new JButton("Back to Login");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;

        add(title, gbc);

        gbc.gridy++;
        add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        add(usernameField, gbc);

        gbc.gridy++;
        add(new JLabel("Email:"), gbc);
        gbc.gridy++;
        add(emailField, gbc);

        gbc.gridy++;
        add(new JLabel("First Name:"), gbc);
        gbc.gridy++;
        add(firstNameField, gbc);

        gbc.gridy++;
        add(new JLabel("Last Name:"), gbc);
        gbc.gridy++;
        add(lastNameField, gbc);

        gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridy++;
        add(passwordField, gbc);

        gbc.gridy++;
        add(new JLabel("Confirm Password:"), gbc);
        gbc.gridy++;
        add(confirmPasswordField, gbc);

        gbc.gridy++;
        add(createBtn, gbc);

        gbc.gridy++;
        add(backBtn, gbc);

        // Sign up button action
        createBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());

            // Validation
            if (username.isEmpty() || email.isEmpty() || firstName.isEmpty() || 
                lastName.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please fill all required fields.", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                    "Passwords do not match!", "Error",
                    JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                confirmPasswordField.setText("");
                return;
            }

            if (pass.length() < 4) {
                JOptionPane.showMessageDialog(this,
                    "Password must be at least 4 characters long.", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create customer account
            try {
                Customer customer = customerService.createCustomer(
                    username, pass, email, firstName, lastName,
                    null, // phone (optional)
                    null, // address (optional)
                    null  // dateOfBirth (optional)
                );
                
                JOptionPane.showMessageDialog(this,
                    "Account created successfully! Welcome, " + customer.getFirstName() + ".",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                usernameField.setText("");
                emailField.setText("");
                firstNameField.setText("");
                lastNameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
                
                // Navigate back to login
                viewManager.showView("LOGIN", new LoginView(viewManager));
                
            } catch (IllegalStateException ex) {
                // Username already exists
                JOptionPane.showMessageDialog(this,
                    "Username already exists. Please choose a different username.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                usernameField.setText("");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> {
            viewManager.showView("LOGIN", new LoginView(viewManager));
        });
    }
}
