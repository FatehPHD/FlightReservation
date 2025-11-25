package gui.admin;

import gui.common.ViewManager;
import javax.swing.*;

/**
 * Manage flights (CRUD operations).
 * Add, edit, delete, and view all flights.
 */
public class ManageFlightsView extends JPanel {
    
    private ViewManager viewManager;
    
    public ManageFlightsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement flight management table
    }
}

