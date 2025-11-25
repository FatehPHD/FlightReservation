package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Reservation;
import javax.swing.*;

/**
 * Payment processing form.
 * Collects payment details and processes booking payment.
 */
public class PaymentView extends JPanel {
    
    private ViewManager viewManager;
    private Reservation reservation;
    
    public PaymentView(ViewManager viewManager, Reservation reservation) {
        this.viewManager = viewManager;
        this.reservation = reservation;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement payment form
    }
}

