package gui.auth;

import gui.common.ViewManager;
import businesslogic.entities.enums.UserRole;

import javax.swing.*;
import java.awt.*;

/**
 * Role selection view shown at startup.
 * Allows users to select Customer/Guest, Agent, or Admin role before login.
 */
public class RoleSelectionView extends JPanel {

    private ViewManager viewManager;

    public RoleSelectionView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("Flight Reservation System");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        add(title, gbc);

        JLabel subtitle = new JLabel("Select your role to continue");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 20, 30, 20);
        add(subtitle, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton customerBtn = new JButton("Customer / Guest");
        customerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        customerBtn.setPreferredSize(new Dimension(250, 50));
        customerBtn.setMaximumSize(new Dimension(250, 50));
        customerBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        customerBtn.addActionListener(e -> {
            viewManager.showView("LOGIN", new LoginView(viewManager, UserRole.CUSTOMER));
        });
        buttonPanel.add(customerBtn);
        buttonPanel.add(Box.createVerticalStrut(20));

        JButton agentBtn = new JButton("Flight Agent");
        agentBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        agentBtn.setPreferredSize(new Dimension(250, 50));
        agentBtn.setMaximumSize(new Dimension(250, 50));
        agentBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        agentBtn.addActionListener(e -> {
            viewManager.showView("LOGIN", new LoginView(viewManager, UserRole.FLIGHT_AGENT));
        });
        buttonPanel.add(agentBtn);
        buttonPanel.add(Box.createVerticalStrut(20));

        JButton adminBtn = new JButton("System Admin");
        adminBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminBtn.setPreferredSize(new Dimension(250, 50));
        adminBtn.setMaximumSize(new Dimension(250, 50));
        adminBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        adminBtn.addActionListener(e -> {
            viewManager.showView("LOGIN", new LoginView(viewManager, UserRole.SYSTEM_ADMIN));
        });
        buttonPanel.add(adminBtn);

        gbc.gridy = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(buttonPanel, gbc);
    }
}

