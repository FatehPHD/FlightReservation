package gui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Navigation menu component for easy navigation between views.
 * Can be used as a sidebar or top menu.
 */
public class NavigationMenu extends JPanel {
    
    private ViewManager viewManager;
    private ButtonGroup buttonGroup;
    private Color selectedColor = new Color(66, 139, 202);
    private Color hoverColor = new Color(230, 240, 250);
    private Color defaultColor = new Color(248, 249, 250);
    
    /**
     * Create navigation menu.
     * @param viewManager ViewManager for navigation
     */
    public NavigationMenu(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.buttonGroup = new ButtonGroup();
        initComponents();
    }
    
    /**
     * Initialize menu components.
     */
    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(defaultColor);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(200, 0));
    }
    
    /**
     * Add a menu item.
     * @param label Button label
     * @param icon Button icon (can be null)
     * @param action Action to perform when clicked
     */
    public void addMenuItem(String label, Icon icon, ActionListener action) {
        JButton button = createMenuButton(label, icon);
        button.addActionListener(action);
        add(button);
        add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    /**
     * Add a menu separator.
     */
    public void addSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        add(separator);
        add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    /**
     * Add a section header.
     * @param title Section title
     */
    public void addSectionHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setForeground(Color.DARK_GRAY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(header);
    }
    
    /**
     * Add spacing.
     * @param height Height of spacing in pixels
     */
    public void addSpacing(int height) {
        add(Box.createRigidArea(new Dimension(0, height)));
    }
    
    /**
     * Add logout button at bottom.
     * @param action Logout action
     */
    public void addLogoutButton(ActionListener action) {
        add(Box.createVerticalGlue());
        
        addSeparator();
        
        JButton logoutBtn = createMenuButton("Logout", null);
        logoutBtn.setForeground(new Color(220, 53, 69));
        logoutBtn.addActionListener(action);
        add(logoutBtn);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Create a styled menu button.
     */
    private JButton createMenuButton(String label, Icon icon) {
        JButton button = new JButton(label, icon);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(defaultColor);
        button.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(hoverColor);
                }
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(defaultColor);
                }
            }
        });
        
        return button;
    }
    
    /**
     * Build customer menu.
     * @param viewManager ViewManager instance
     * @return NavigationMenu configured for customers
     */
    public static NavigationMenu buildCustomerMenu(ViewManager viewManager) {
        NavigationMenu menu = new NavigationMenu(viewManager);
        
        menu.addSectionHeader("MAIN");
        menu.addMenuItem("Dashboard", null, e -> {
            // Navigate to dashboard
            ErrorDialog.showInfo(menu, "Dashboard - To be implemented");
        });
        menu.addMenuItem("Search Flights", null, e -> {
            ErrorDialog.showInfo(menu, "Search Flights - To be implemented");
        });
        menu.addMenuItem("My Reservations", null, e -> {
            ErrorDialog.showInfo(menu, "My Reservations - To be implemented");
        });
        
        menu.addSeparator();
        menu.addSectionHeader("ACCOUNT");
        menu.addMenuItem("My Profile", null, e -> {
            ErrorDialog.showInfo(menu, "Profile - To be implemented");
        });
        
        menu.addLogoutButton(e -> {
            if (ConfirmDialog.showLogout(menu)) {
                viewManager.logout();
                ErrorDialog.showInfo(menu, "Logged out successfully");
            }
        });
        
        return menu;
    }
    
    /**
     * Build agent menu.
     * @param viewManager ViewManager instance
     * @return NavigationMenu configured for agents
     */
    public static NavigationMenu buildAgentMenu(ViewManager viewManager) {
        NavigationMenu menu = new NavigationMenu(viewManager);
        
        menu.addSectionHeader("MAIN");
        menu.addMenuItem("Dashboard", null, e -> {
            ErrorDialog.showInfo(menu, "Agent Dashboard - To be implemented");
        });
        menu.addMenuItem("Find Customer", null, e -> {
            ErrorDialog.showInfo(menu, "Customer Search - To be implemented");
        });
        menu.addMenuItem("Assist Booking", null, e -> {
            ErrorDialog.showInfo(menu, "Assist Booking - To be implemented");
        });
        menu.addMenuItem("Manage Reservations", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Reservations - To be implemented");
        });
        
        menu.addLogoutButton(e -> {
            if (ConfirmDialog.showLogout(menu)) {
                viewManager.logout();
                ErrorDialog.showInfo(menu, "Logged out successfully");
            }
        });
        
        return menu;
    }
    
    /**
     * Build admin menu.
     * @param viewManager ViewManager instance
     * @return NavigationMenu configured for admins
     */
    public static NavigationMenu buildAdminMenu(ViewManager viewManager) {
        NavigationMenu menu = new NavigationMenu(viewManager);
        
        menu.addSectionHeader("MAIN");
        menu.addMenuItem("Dashboard", null, e -> {
            ErrorDialog.showInfo(menu, "Admin Dashboard - To be implemented");
        });
        
        menu.addSeparator();
        menu.addSectionHeader("MANAGE");
        menu.addMenuItem("Flights", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Flights - To be implemented");
        });
        menu.addMenuItem("Aircraft", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Aircraft - To be implemented");
        });
        menu.addMenuItem("Airlines", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Airlines - To be implemented");
        });
        menu.addMenuItem("Airports", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Airports - To be implemented");
        });
        menu.addMenuItem("Routes", null, e -> {
            ErrorDialog.showInfo(menu, "Manage Routes - To be implemented");
        });
        
        menu.addSeparator();
        menu.addSectionHeader("REPORTS");
        menu.addMenuItem("View Reports", null, e -> {
            ErrorDialog.showInfo(menu, "Reports - To be implemented");
        });
        
        menu.addLogoutButton(e -> {
            if (ConfirmDialog.showLogout(menu)) {
                viewManager.logout();
                ErrorDialog.showInfo(menu, "Logged out successfully");
            }
        });
        
        return menu;
    }
}
