package businesslogic.services;

import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.dao.ReservationDAO;
import businesslogic.entities.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class for customer-related business logic.
 * Handles customer profile management and operations.
 */
public class CustomerService {
    
    private UserDAO userDAO;
    private ReservationDAO reservationDAO;
    
    public CustomerService(UserDAO userDAO, ReservationDAO reservationDAO) {
        this.userDAO = userDAO;
        this.reservationDAO = reservationDAO;
    }
    
    /**
     * Create a new customer account.
     * @param username Username (must be unique)
     * @param password Password
     * @param email Email (must be unique)
     * @param firstName First name
     * @param lastName Last name
     * @param phone Phone number
     * @param address Address
     * @param dateOfBirth Date of birth
     * @return Created customer
     */
    public Customer createCustomer(String username, String password, String email,
                                  String firstName, String lastName, String phone,
                                  String address, LocalDate dateOfBirth) throws SQLException {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Check if username already exists
        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalStateException("Username already exists");
        }
        
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(password); // In production, this should be hashed
        customer.setEmail(email);
        customer.setRole(UserRole.CUSTOMER);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setDateOfBirth(dateOfBirth);
        customer.setMembershipStatus(MembershipStatus.REGULAR); // Default to REGULAR
        
        return (Customer) userDAO.save(customer);
    }
    
    /**
     * Get customer by username.
     * @param username Username
     * @return Customer or null if not found
     */
    public Customer getCustomerByUsername(String username) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }
    
    /**
     * Get customer by ID.
     * @param customerId Customer ID
     * @return Customer or null if not found
     */
    public Customer getCustomerById(int customerId) throws SQLException {
        User user = userDAO.findById(customerId);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }
    
    /**
     * Update customer profile information.
     * @param customer Customer to update
     * @return true if update successful
     */
    public boolean updateCustomerProfile(Customer customer) throws SQLException {
        if (customer == null || customer.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid customer is required");
        }
        
        // Verify customer exists
        Customer existing = getCustomerById(customer.getUserId());
        if (existing == null) {
            return false;
        }
        
        // Ensure role is still CUSTOMER
        customer.setRole(UserRole.CUSTOMER);
        
        return userDAO.update(customer);
    }
    
    /**
     * Update customer membership status.
     * @param customerId Customer ID
     * @param newStatus New membership status
     * @return true if update successful
     */
    public boolean updateMembershipStatus(int customerId, MembershipStatus newStatus) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            return false;
        }
        
        customer.setMembershipStatus(newStatus);
        return userDAO.update(customer);
    }
    
    /**
     * Get customer's booking history.
     * @param customer Customer
     * @return List of customer's reservations
     */
    public List<Reservation> getBookingHistory(Customer customer) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        
        return reservationDAO.findByCustomerId(customer.getUserId());
    }
    
    /**
     * Authenticate customer (login).
     * @param username Username
     * @param password Password
     * @return Customer if authentication successful, null otherwise
     */
    public Customer authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }
        
        Customer customer = getCustomerByUsername(username);
        if (customer == null) {
            return null;
        }
        
        // Simple password check (in production, use hashed passwords)
        if (password.equals(customer.getPassword())) {
            return customer;
        }
        
        return null;
    }
    
    /**
     * Get all customers.
     * @return List of all customers
     */
    public List<Customer> getAllCustomers() throws SQLException {
        return userDAO.findAllCustomers();
    }
}
