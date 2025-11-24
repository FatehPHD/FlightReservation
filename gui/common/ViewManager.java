package gui.common;

import businesslogic.entities.Customer;
import javax.swing.*;
import java.awt.*;

public class ViewManager {

    private CardLayout cardLayout;
    private Container container;
    private Customer currentUser;

    public ViewManager(CardLayout layout, Container container) {
        this.cardLayout = layout;
        this.container = container;
        this.currentUser = null;
    }

    public void showView(String name, JPanel panel) {
        // Check if a component with this name already exists in the container
        boolean exists = false;
        for (Component comp : container.getComponents()) {
            if (comp == panel) {
                exists = true;
                break;
            }
        }
        
        // Only add if it's a new panel instance
        if (!exists) {
            container.add(panel, name);
        }
        
        // Show the view
        cardLayout.show(container, name);
    }

    public void setCurrentUser(Customer customer) {
        this.currentUser = customer;
    }

    public Customer getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
