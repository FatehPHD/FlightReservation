package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Reservation;
import javax.swing.*;

/**
 * Booking confirmation screen.
 * Shows booking details and confirmation number.
 */
public class ConfirmationView extends JPanel {
    
    private ViewManager viewManager;
    private Reservation reservation;
    
    public ConfirmationView(ViewManager viewManager, Reservation reservation) {
        this.viewManager = viewManager;
        this.reservation = reservation;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement confirmation display
    }
}

