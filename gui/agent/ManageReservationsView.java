package gui.agent;

import gui.common.ViewManager;
import javax.swing.*;

/**
 * Manage all customer reservations.
 * Allows agents to modify or cancel any booking.
 */
public class ManageReservationsView extends JPanel {
    
    private ViewManager viewManager;
    
    public ManageReservationsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement reservation management
    }
}

