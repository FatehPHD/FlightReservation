package gui.agent;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import javax.swing.*;

/**
 * Book flights on behalf of customers.
 * Similar to customer booking but with agent assistance.
 */
public class AssistBookingView extends JPanel {
    
    private ViewManager viewManager;
    private Customer customer;
    
    public AssistBookingView(ViewManager viewManager, Customer customer) {
        this.viewManager = viewManager;
        this.customer = customer;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement assisted booking
    }
}

