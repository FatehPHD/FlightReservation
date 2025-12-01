package gui.auth;

import gui.common.ViewManager;
import businesslogic.services.CustomerService;
import businesslogic.entities.Customer;
import businesslogic.entities.enums.UserRole;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Customer registration form.
 * Creates new customer accounts via CustomerService.
 */
public class SignupView extends JPanel {

    private ViewManager viewManager;
    private CustomerService customerService;

    public SignupView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.customerService = viewManager.getCustomerService();
        setLayout(new GridBagLayout());

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JButton createBtn = new JButton("Sign Up");
        JButton backBtn = new JButton("Back to Login");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        add(title, gbc);

        gbc.ipadx = 200;

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("Username:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(usernameField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("Email:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(emailField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("First Name:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(firstNameField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("Last Name:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(lastNameField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("Password:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(passwordField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(new JLabel("Confirm Password:"), gbc);

        gbc.gridy++;
        gbc.ipadx = 200;
        add(confirmPasswordField, gbc);

        gbc.gridy++;
        gbc.ipadx = 0;
        add(createBtn, gbc);

        gbc.gridy++;
        add(backBtn, gbc);

        createBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());

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

            try {
                Customer customer = customerService.createCustomer(
                    username, pass, email, firstName, lastName, 
                        null, null, null
                );
                
                JOptionPane.showMessageDialog(this,
                        "Account created successfully! Welcome, " + customer.getFirstName() + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                
                viewManager.showView("LOGIN", new LoginView(viewManager, UserRole.CUSTOMER));
                
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                        "Username already exists. Please choose a different one.",
                        "Error", JOptionPane.ERROR_MESSAGE);
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
            viewManager.showView("LOGIN", new LoginView(viewManager, UserRole.CUSTOMER));
        });
    }
}
