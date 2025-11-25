package gui.auth;

import gui.common.ViewManager;
import gui.customer.CustomerDashboardView;
import gui.admin.AdminDashboardView;
import gui.agent.AgentDashboardView;
import businesslogic.entities.User;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.impl.UserDAOImpl;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginView extends JPanel {

    private ViewManager viewManager;
    private UserDAO userDAO;

    public LoginView(ViewManager viewManager) {
        this.viewManager = viewManager;
        
        // Get UserDAO to authenticate any user type (Customer, FlightAgent, SystemAdmin)
        this.userDAO = new UserDAOImpl();
        
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
                // Get user by username (works for all user types)
                User user = userDAO.findByUsername(username);
                
                if (user != null && password.equals(user.getPassword())) {
                    // Store logged-in user in ViewManager (works for all user types)
                    viewManager.setCurrentUser(user);
                    
                    // Navigate to appropriate dashboard based on role
                    UserRole role = user.getRole();
                    if (role == UserRole.CUSTOMER) {
                        viewManager.showView("CUSTOMER_DASHBOARD", 
                            new CustomerDashboardView(viewManager));
                    } else if (role == UserRole.SYSTEM_ADMIN) {
                        viewManager.showView("ADMIN_DASHBOARD", 
                            new AdminDashboardView(viewManager));
                    } else if (role == UserRole.FLIGHT_AGENT) {
                        viewManager.showView("AGENT_DASHBOARD", 
                            new AgentDashboardView(viewManager));
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Unknown user role. Please contact support.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
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
