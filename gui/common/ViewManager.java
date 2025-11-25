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
 * ViewManager - Manages navigation between views and provides access to services.
 * Handles CardLayout switching and maintains current user session.
 */
public class ViewManager {

    private CardLayout cardLayout;
    private Container container;
    private User currentUser;  // Can be Customer, FlightAgent, or SystemAdmin
    private ServiceManager serviceManager;

    public ViewManager(CardLayout layout, Container container, ServiceManager serviceManager) {
        this.cardLayout = layout;
        this.container = container;
        this.currentUser = null;
        this.serviceManager = serviceManager;
    }

    /**
     * Show a view by name. Automatically wraps in JScrollPane if needed.
     * @param name Unique identifier for the view
     * @param panel JPanel to display
     */
    public void showView(String name, JPanel panel) {
        // Check if a component with this name already exists in the container
        boolean exists = false;
        for (Component comp : container.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                exists = true;
                break;
            }
        }
        
        // If it doesn't exist, wrap the panel in a scroll pane and add it
        if (!exists) {
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null); // Remove border for cleaner look
            scrollPane.getViewport().setBackground(panel.getBackground()); // Match background
            scrollPane.setName(name); // Set name for identification
            container.add(scrollPane, name);
        }
        
        // Show the view
        cardLayout.show(container, name);
    }

    /**
     * Set the currently logged-in user.
     * @param user User object (Customer, FlightAgent, or SystemAdmin)
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the currently logged-in user.
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get current user as Customer (convenience method).
     * @return Customer or null if not a customer or not logged in
     */
    public Customer getCurrentCustomer() {
        if (currentUser instanceof Customer) {
            return (Customer) currentUser;
        }
        return null;
    }

    /**
     * Get current user as FlightAgent (convenience method).
     * @return FlightAgent or null if not an agent or not logged in
     */
    public FlightAgent getCurrentAgent() {
        if (currentUser instanceof FlightAgent) {
            return (FlightAgent) currentUser;
        }
        return null;
    }

    /**
     * Get current user as SystemAdmin (convenience method).
     * @return SystemAdmin or null if not an admin or not logged in
     */
    public SystemAdmin getCurrentAdmin() {
        if (currentUser instanceof SystemAdmin) {
            return (SystemAdmin) currentUser;
        }
        return null;
    }

    /**
     * Check if current user is a customer.
     */
    public boolean isCustomer() {
        return currentUser instanceof Customer;
    }

    /**
     * Check if current user is a flight agent.
     */
    public boolean isAgent() {
        return currentUser instanceof FlightAgent;
    }

    /**
     * Check if current user is a system admin.
     */
    public boolean isAdmin() {
        return currentUser instanceof SystemAdmin;
    }

    /**
     * Logout current user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Check if any user is logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get the service manager (provides access to all services).
     */
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    // Convenience methods for accessing services directly
    
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
    
    /**
     * Get the container (useful for dialogs that need parent component).
     */
    public Container getContainer() {
        return container;
    }
}
