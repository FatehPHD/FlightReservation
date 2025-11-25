package gui.admin;

import gui.common.ViewManager;
import javax.swing.*;

/**
 * Manage aircraft (CRUD operations).
 * Add, edit, delete, and view all aircraft.
 */
public class ManageAircraftView extends JPanel {
    
    private ViewManager viewManager;
    
    public ManageAircraftView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement aircraft management table
    }
}

