package gui.common;

import businesslogic.services.CustomerService;
import businesslogic.services.FlightService;
import businesslogic.services.ReservationService;
import businesslogic.services.PaymentService;
import businesslogic.services.AdminService;
import businesslogic.services.PromotionService;
import datalayer.dao.*;
import datalayer.impl.*;

import java.sql.SQLException;

/**
 * Initializes and provides access to all service classes.
 * GUI layer only interacts with services, never DAOs directly.
 */
public class ServiceManager {
    
    private CustomerService customerService;
    private FlightService flightService;
    private ReservationService reservationService;
    private PaymentService paymentService;
    private AdminService adminService;
    private PromotionService promotionService;
    
    public ServiceManager() throws SQLException {
        UserDAO userDAO = new UserDAOImpl();
        ReservationDAO reservationDAO = new ReservationDAOImpl();
        FlightDAO flightDAO = new FlightDAOImpl();
        SeatDAO seatDAO = new SeatDAOImpl();
        PaymentDAO paymentDAO = new PaymentDAOImpl();
        AircraftDAO aircraftDAO = new AircraftDAOImpl();
        AirlineDAO airlineDAO = new AirlineDAOImpl();
        AirportDAO airportDAO = new AirportDAOImpl();
        RouteDAO routeDAO = new RouteDAOImpl();
        PromotionDAO promotionDAO = new PromotionDAOImpl();
        
        this.customerService = new CustomerService(userDAO, reservationDAO);
        this.flightService = new FlightService(flightDAO, airportDAO);
        this.reservationService = new ReservationService(reservationDAO, seatDAO, flightService);
        this.paymentService = new PaymentService(paymentDAO);
        this.adminService = new AdminService(flightDAO, aircraftDAO, airlineDAO, 
                                           airportDAO, routeDAO, userDAO, seatDAO, reservationDAO, paymentDAO);
        this.promotionService = new PromotionService(promotionDAO, userDAO);
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
    
    public PromotionService getPromotionService() {
        return promotionService;
    }
}