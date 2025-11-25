package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import javax.swing.*;

/**
 * View and manage customer's reservations.
 * Shows all bookings with cancel/modify options.
 */
public class MyReservationsView extends JPanel {
    
    private ViewManager viewManager;
    private Customer customer;
    
    public MyReservationsView(ViewManager viewManager, Customer customer) {
        this.viewManager = viewManager;
        this.customer = customer;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement reservations table
    }
}

