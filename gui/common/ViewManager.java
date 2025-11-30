package gui.common;

import businesslogic.entities.User;
import businesslogic.entities.Customer;
import businesslogic.entities.FlightAgent;
import businesslogic.entities.SystemAdmin;
import businesslogic.services.CustomerService;
import businesslogic.services.FlightService;
import businesslogic.services.ReservationService;
import businesslogic.services.PaymentService;
import businesslogic.services.AdminService;
import javax.swing.*;
import java.awt.*;

/**
 * Manages view navigation using CardLayout and maintains user session.
 * Provides access to service layer through ServiceManager.
 */
public class ViewManager {

    private CardLayout cardLayout;
    private Container container;
    private User currentUser;
    private ServiceManager serviceManager;

    public ViewManager(CardLayout layout, Container container, ServiceManager serviceManager) {
        this.cardLayout = layout;
        this.container = container;
        this.currentUser = null;
        this.serviceManager = serviceManager;
    }

    /**
     * Displays a view by name. Removes any existing view with the same name
     * to ensure fresh data is shown (important after logout/login).
     */
    public void showView(String name, JPanel panel) {
        Component existing = null;
        for (Component comp : container.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                existing = comp;
                break;
            }
        }
        
        if (existing != null) {
            container.remove(existing);
        }
        
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(panel.getBackground());
        scrollPane.setName(name);
        container.add(scrollPane, name);
        
        cardLayout.show(container, name);
        container.revalidate();
        container.repaint();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Customer getCurrentCustomer() {
        if (currentUser instanceof Customer) {
            return (Customer) currentUser;
        }
        return null;
    }

    public FlightAgent getCurrentAgent() {
        if (currentUser instanceof FlightAgent) {
            return (FlightAgent) currentUser;
        }
        return null;
    }

    public SystemAdmin getCurrentAdmin() {
        if (currentUser instanceof SystemAdmin) {
            return (SystemAdmin) currentUser;
        }
        return null;
    }

    public boolean isCustomer() {
        return currentUser instanceof Customer;
    }

    public boolean isAgent() {
        return currentUser instanceof FlightAgent;
    }

    public boolean isAdmin() {
        return currentUser instanceof SystemAdmin;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
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
    
    public Container getContainer() {
        return container;
    }
}
