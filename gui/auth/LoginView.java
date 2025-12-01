package gui.auth;

import gui.common.ViewManager;
import gui.customer.CustomerDashboardView;
import gui.agent.AgentDashboardView;
import gui.admin.AdminDashboardView;
import businesslogic.entities.User;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.impl.UserDAOImpl;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Login screen for all user types (Customer, Agent, Admin).
 * Validates that the user's role matches the selected role from role selection.
 * Admin and Agent can also login as Customer, but not vice versa.
 */
public class LoginView extends JPanel {

    private ViewManager viewManager;
    private UserDAO userDAO;
    private UserRole selectedRole;

    public LoginView(ViewManager viewManager, UserRole selectedRole) {
        this.viewManager = viewManager;
        this.selectedRole = selectedRole;
        this.userDAO = new UserDAOImpl();
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        String roleLabelText = "Login as " + getRoleDisplayName(selectedRole);
        JLabel roleLabel = new JLabel(roleLabelText);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(100, 100, 100));

        JTextField usernameField = new JTextField(25);
        usernameField.setPreferredSize(new Dimension(300, 30));
        JPasswordField passwordField = new JPasswordField(25);
        passwordField.setPreferredSize(new Dimension(300, 30));

        JButton loginBtn = new JButton("Login");
        JButton backBtn = new JButton("Back to Role Selection");
        JButton createAccountBtn = null;
        
        if (selectedRole == UserRole.CUSTOMER) {
            createAccountBtn = new JButton("Create Account");
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(title, gbc);

        gbc.gridy++;
        add(roleLabel, gbc);

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
        
        if (createAccountBtn != null) {
            gbc.gridy++;
            add(createAccountBtn, gbc);
        }
        
        gbc.gridy++;
        add(backBtn, gbc);

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
                User user = userDAO.findByUsername(username);
                
                if (user == null || !password.equals(user.getPassword())) {
                    JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                    passwordField.setText("");
                    return;
                }
                
                // Validate role match
                UserRole userRole = user.getRole();
                boolean roleValid = false;
                
                if (selectedRole == UserRole.CUSTOMER) {
                    // Customer selection: anyone can login (Customer, Agent, Admin)
                    roleValid = true;
                } else if (selectedRole == UserRole.FLIGHT_AGENT) {
                    // Agent selection: only Agent can login
                    if (userRole == UserRole.FLIGHT_AGENT) {
                        roleValid = true;
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "You selected Flight Agent role, but this account is not an Agent.\n" +
                            "Please go back to role selection and choose the correct role.",
                            "Role Mismatch", JOptionPane.ERROR_MESSAGE);
                        viewManager.showView("ROLE_SELECTION", new RoleSelectionView(viewManager));
                        return;
                    }
                } else if (selectedRole == UserRole.SYSTEM_ADMIN) {
                    // Admin selection: only Admin can login
                    if (userRole == UserRole.SYSTEM_ADMIN) {
                        roleValid = true;
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "You selected System Admin role, but this account is not an Admin.\n" +
                            "Please go back to role selection and choose the correct role.",
                            "Role Mismatch", JOptionPane.ERROR_MESSAGE);
                        viewManager.showView("ROLE_SELECTION", new RoleSelectionView(viewManager));
                        return;
                    }
                }
                
                if (roleValid) {
                    viewManager.setCurrentUser(user);
                    
                    // Route to dashboard based on selected role, not user's actual role
                    if (selectedRole == UserRole.CUSTOMER) {
                        // If Customer role selected, always go to Customer Dashboard
                        // (Admin and Agent can access customer features this way)
                        viewManager.showView("CUSTOMER_DASHBOARD", 
                            new CustomerDashboardView(viewManager));
                    } else if (selectedRole == UserRole.FLIGHT_AGENT) {
                        // Agent selected - go to Agent Dashboard
                        viewManager.showView("AGENT_DASHBOARD", 
                            new AgentDashboardView(viewManager));
                    } else if (selectedRole == UserRole.SYSTEM_ADMIN) {
                        // Admin selected - go to Admin Dashboard
                        viewManager.showView("ADMIN_DASHBOARD", 
                            new AdminDashboardView(viewManager));
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        if (createAccountBtn != null) {
            createAccountBtn.addActionListener(e -> {
                viewManager.showView("SIGNUP", new SignupView(viewManager));
            });
        }

        backBtn.addActionListener(e -> {
            viewManager.showView("ROLE_SELECTION", new RoleSelectionView(viewManager));
        });
    }

    private String getRoleDisplayName(UserRole role) {
        switch (role) {
            case CUSTOMER:
                return "Customer / Guest";
            case FLIGHT_AGENT:
                return "Flight Agent";
            case SYSTEM_ADMIN:
                return "System Admin";
            default:
                return role.toString();
        }
    }
}
