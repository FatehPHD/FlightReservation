package businesslogic.services;

import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.Reservation;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.dao.ReservationDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles customer account management and membership status.
 */
public class CustomerService {
    
    private final UserDAO userDAO;
    private final ReservationDAO reservationDAO;

    public CustomerService(UserDAO userDAO, ReservationDAO reservationDAO) {
        this.userDAO = userDAO;
        this.reservationDAO = reservationDAO;
    }

    /**
     * Create a new customer account. New customers start with REGULAR membership.
     */
    public Customer createCustomer(String username, String password, String email,
                                   String firstName, String lastName,
                                   String phone, String address, LocalDate dateOfBirth) throws SQLException {

        String trimmedUsername = username != null ? username.trim() : null;
        String trimmedEmail = email != null ? email.trim() : null;
        String trimmedFirstName = firstName != null ? firstName.trim() : null;
        String trimmedLastName = lastName != null ? lastName.trim() : null;
        String trimmedPhone = phone != null ? phone.trim() : null;
        String trimmedAddress = address != null ? address.trim() : null;

        if (trimmedUsername == null || trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (trimmedEmail == null || trimmedEmail.isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (trimmedFirstName == null || trimmedFirstName.isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (trimmedLastName == null || trimmedLastName.isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }

        User existingUser = userDAO.findByUsername(trimmedUsername);
        if (existingUser != null) {
            throw new IllegalStateException("Username already exists: " + trimmedUsername);
        }

        Customer customer = new Customer(
                0,
                trimmedUsername,
                password,
                trimmedEmail,
                UserRole.CUSTOMER,
                trimmedFirstName,
                trimmedLastName,
                trimmedPhone,
                trimmedAddress,
                dateOfBirth,
                MembershipStatus.REGULAR
        );

        return (Customer) userDAO.save(customer);
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        User user = userDAO.findById(customerId);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }

    public Customer getCustomerByUsername(String username) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user instanceof Customer) {
            return (Customer) user;
        }
        return null;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return userDAO.findAllCustomers();
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        if (customer == null || customer.getUserId() <= 0) {
            throw new IllegalArgumentException("Valid customer is required.");
        }

        Customer existing = getCustomerById(customer.getUserId());
        if (existing == null) {
            return false;
        }

        customer.setRole(UserRole.CUSTOMER);
        return userDAO.update(customer);
    }

    public boolean updateCustomerProfile(Customer customer) throws SQLException {
        return updateCustomer(customer);
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        return userDAO.delete(customerId);
    }

    /**
     * Auto-update membership based on booking count:
     * 50+ = PLATINUM, 25+ = GOLD, 10+ = SILVER, else REGULAR
     */
    public Customer updateMembershipStatus(int customerId) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        List<Reservation> reservations = reservationDAO.findByCustomerId(customerId);
        int bookingCount = reservations.size();

        MembershipStatus newStatus;
        if (bookingCount >= 50) {
            newStatus = MembershipStatus.PLATINUM;
        } else if (bookingCount >= 25) {
            newStatus = MembershipStatus.GOLD;
        } else if (bookingCount >= 10) {
            newStatus = MembershipStatus.SILVER;
        } else {
            newStatus = MembershipStatus.REGULAR;
        }

        if (customer.getMembershipStatus() != newStatus) {
            customer.setMembershipStatus(newStatus);
            userDAO.update(customer);
        }

        return customer;
    }

    public boolean updateMembershipStatus(int customerId, MembershipStatus newStatus) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            return false;
        }

        customer.setMembershipStatus(newStatus);
        return userDAO.update(customer);
    }

    public Customer authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return null;
        }

        Customer customer = getCustomerByUsername(username);
        if (customer == null) {
            return null;
        }

        if (password.equals(customer.getPassword())) {
            return customer;
        }

        return null;
    }

    public Customer authenticateCustomer(String username, String password) throws SQLException {
        return authenticate(username, password);
    }

    public boolean changePassword(int customerId, String oldPassword, String newPassword) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        if (!customer.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters.");
        }

        customer.setPassword(newPassword);
        return userDAO.update(customer);
    }

    public List<Reservation> getCustomerReservations(int customerId) throws SQLException {
        return reservationDAO.findByCustomerId(customerId);
    }

    public List<Reservation> getBookingHistory(Customer customer) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        return reservationDAO.findByCustomerId(customer.getUserId());
    }
}
