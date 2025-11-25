package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Customer;
import javax.swing.*;

/**
 * Customer profile management.
 * Edit profile details and change password.
 */
public class ProfileView extends JPanel {
    
    private ViewManager viewManager;
    private Customer customer;
    
    public ProfileView(ViewManager viewManager, Customer customer) {
        this.viewManager = viewManager;
        this.customer = customer;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement profile form
    }
}

