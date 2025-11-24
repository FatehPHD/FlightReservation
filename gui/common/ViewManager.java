package gui.common;

import businesslogic.entities.Customer;
import businesslogic.services.CustomerService;
import businesslogic.services.FlightService;
import businesslogic.services.ReservationService;
import businesslogic.services.PaymentService;
import businesslogic.services.AdminService;
import javax.swing.*;
import java.awt.*;

public class ViewManager {

    private CardLayout cardLayout;
    private Container container;
    private Customer currentUser;
    private ServiceManager serviceManager;

    public ViewManager(CardLayout layout, Container container, ServiceManager serviceManager) {
        this.cardLayout = layout;
        this.container = container;
        this.currentUser = null;
        this.serviceManager = serviceManager;
    }

    public void showView(String name, JPanel panel) {
        // Check if a component with this name already exists in the container
        boolean exists = false;
        for (Component comp : container.getComponents()) {
            // Check if this component is already in the container with this name
            // We'll check by trying to find it in the layout
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                // If the view inside matches, we've already added this panel
                if (scrollPane.getViewport().getView() == panel) {
                    exists = true;
                    break;
                }
            }
        }
        
        // If it doesn't exist, wrap the panel in a scroll pane and add it
        if (!exists) {
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null); // Remove border for cleaner look
            scrollPane.getViewport().setBackground(panel.getBackground()); // Match background
            container.add(scrollPane, name);
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

    public CustomerService getCustomerService() {
        return serviceManager.getCustomerService();
    }

    public FlightService getFlightService() {
        return serviceManager.getFlightService();
    }

    public ReservationService getReservationService() {
        return serviceManager.getReservationService();
    }

    public PaymentService getPaymentService() {
        return serviceManager.getPaymentService();
    }

    public AdminService getAdminService() {
        return serviceManager.getAdminService();
    }
}
