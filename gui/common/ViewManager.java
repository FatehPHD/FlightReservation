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
