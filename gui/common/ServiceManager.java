package gui.common;

import businesslogic.services.CustomerService;
import businesslogic.services.FlightService;
import businesslogic.services.ReservationService;
import businesslogic.services.PaymentService;
import businesslogic.services.AdminService;
import datalayer.dao.*;
import datalayer.impl.*;

import java.sql.SQLException;

/**
 * Service Manager - Initializes and provides access to all service classes.
 * This ensures the GUI layer only interacts with services, not DAOs directly.
 */
public class ServiceManager {
    
    private CustomerService customerService;
    private FlightService flightService;
    private ReservationService reservationService;
    private PaymentService paymentService;
    private AdminService adminService;
    
    public ServiceManager() throws SQLException {
        // Initialize all DAOs
        UserDAO userDAO = new UserDAOImpl();
        ReservationDAO reservationDAO = new ReservationDAOImpl();
        FlightDAO flightDAO = new FlightDAOImpl();
        SeatDAO seatDAO = new SeatDAOImpl();
        PaymentDAO paymentDAO = new PaymentDAOImpl();
        AircraftDAO aircraftDAO = new AircraftDAOImpl();
        AirlineDAO airlineDAO = new AirlineDAOImpl();
        AirportDAO airportDAO = new AirportDAOImpl();
        RouteDAO routeDAO = new RouteDAOImpl();
        
        // Initialize services with their dependencies
        this.customerService = new CustomerService(userDAO, reservationDAO);
        this.flightService = new FlightService(flightDAO);
        this.reservationService = new ReservationService(reservationDAO, seatDAO, flightService);
        this.paymentService = new PaymentService(paymentDAO);
        this.adminService = new AdminService(flightDAO, aircraftDAO, airlineDAO, 
                                           airportDAO, routeDAO, userDAO);
    }
    
    public CustomerService getCustomerService() {
        return customerService;
    }
    
    public FlightService getFlightService() {
        return flightService;
    }
    
    public ReservationService getReservationService() {
        return reservationService;
    }
    
    public PaymentService getPaymentService() {
        return paymentService;
    }
    
    public AdminService getAdminService() {
        return adminService;
    }
}

