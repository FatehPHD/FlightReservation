package gui.customer;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.User;
import businesslogic.entities.Reservation;
import businesslogic.entities.Flight;
import businesslogic.entities.Route;
import businesslogic.entities.Airport;
import businesslogic.entities.Seat;
import businesslogic.services.ReservationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Displays user reservations in a table format.
 * Shows reservation details including flight information, status, and seats.
 */
public class MyReservationsView extends JPanel {
    
    private ViewManager viewManager;
    private User user;
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private ReservationService reservationService;
    
    public MyReservationsView(ViewManager viewManager, User user) {
        this.viewManager = viewManager;
        this.user = user;
        this.reservationService = viewManager.getReservationService();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JLabel title = new JLabel("My Reservations");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(title, BorderLayout.NORTH);
        
        // Table setup
        String[] columnNames = {
            "Reservation ID", "Flight", "Route", "Departure", "Arrival", 
            "Status", "Total Price", "Booking Date", "Seats"
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reservationsTable = new JTable(tableModel);
        reservationsTable.setRowHeight(25);
        reservationsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        reservationsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        reservationsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        reservationsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        reservationsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        reservationsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        reservationsTable.getColumnModel().getColumn(4).setPreferredWidth(150);
        reservationsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        reservationsTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        reservationsTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        reservationsTable.getColumnModel().getColumn(8).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        scrollPane.setPreferredSize(new Dimension(1200, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadReservations());
        
        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", 
                new CustomerDashboardView(viewManager));
        });
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load reservations
        loadReservations();
    }
    
    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.getUserReservations(user);
            displayReservations(reservations);
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading reservations: " + e.getMessage(), e);
        }
    }
    
    private void displayReservations(List<Reservation> reservations) {
        tableModel.setRowCount(0);
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        if (reservations.isEmpty()) {
            // Show message in center if no reservations
            JLabel noReservations = new JLabel("You have no reservations yet.");
            noReservations.setFont(new Font("Arial", Font.PLAIN, 16));
            noReservations.setHorizontalAlignment(SwingConstants.CENTER);
            noReservations.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
            return;
        }
        
        for (Reservation reservation : reservations) {
            Flight flight = reservation.getFlight();
            
            String flightNumber = flight != null ? flight.getFlightNumber() : "N/A";
            
            String route = "N/A";
            if (flight != null && flight.getRoute() != null) {
                Route routeObj = flight.getRoute();
                Airport origin = routeObj.getOrigin();
                Airport destination = routeObj.getDestination();
                if (origin != null && destination != null) {
                    route = origin.getAirportCode() + " → " + destination.getAirportCode();
                }
            }
            
            String departureTime = "N/A";
            if (flight != null && flight.getDepartureTime() != null) {
                departureTime = flight.getDepartureTime().format(dateTimeFormatter);
            }
            
            String arrivalTime = "N/A";
            if (flight != null && flight.getArrivalTime() != null) {
                arrivalTime = flight.getArrivalTime().format(dateTimeFormatter);
            }
            
            String status = reservation.getStatus() != null ? 
                reservation.getStatus().toString() : "N/A";
            
            String totalPrice = String.format("$%.2f", reservation.getTotalPrice());
            
            String bookingDate = reservation.getBookingDate() != null ? 
                reservation.getBookingDate().format(dateTimeFormatter) : "N/A";
            
            String seats = "N/A";
            if (reservation.getSeats() != null && !reservation.getSeats().isEmpty()) {
                seats = reservation.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.joining(", "));
            }
            
            tableModel.addRow(new Object[]{
                reservation.getReservationId(),
                flightNumber,
                route,
                departureTime,
                arrivalTime,
                status,
                totalPrice,
                bookingDate,
                seats
            });
        }
    }
}

