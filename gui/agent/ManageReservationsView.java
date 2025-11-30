package gui.agent;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.Reservation;
import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import businesslogic.entities.enums.ReservationStatus;
import businesslogic.services.ReservationService;
import businesslogic.services.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage all customer reservations.
 * Allows agents to view, search, and cancel reservations.
 * All database operations are performed through ReservationService, CustomerService, and FlightService.
 */
public class ManageReservationsView extends JPanel {
    
    private ViewManager viewManager;
    private ReservationService reservationService;
    private CustomerService customerService;
    
    private JTable reservationsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;
    private List<Reservation> allReservations;
    
    public ManageReservationsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.reservationService = viewManager.getReservationService();
        this.customerService = viewManager.getCustomerService();
        initComponents();
        loadAllReservations();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Reservations");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Reservations"));
        searchPanel.setLayout(new FlowLayout());
        
        searchTypeComboBox = new JComboBox<>(new String[]{"All", "Reservation ID", "Customer Username"});
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchTypeComboBox);
        
        searchField = new JTextField(20);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> performSearch());
        searchPanel.add(searchBtn);
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadAllReservations();
        });
        searchPanel.add(clearBtn);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "Reservation ID", "Customer", "Flight", "Status", "Total Price", "Booking Date", "Seats"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reservationsTable = new JTable(tableModel);
        reservationsTable.setRowHeight(25);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setPreferredSize(new Dimension(150, 35));
        viewDetailsBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        viewDetailsBtn.addActionListener(e -> showReservationDetails());
        
        JButton cancelBtn = new JButton("Cancel Reservation");
        cancelBtn.setPreferredSize(new Dimension(150, 35));
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelBtn.addActionListener(e -> cancelSelectedReservation());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadAllReservations());
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("AGENT_DASHBOARD", 
                new AgentDashboardView(viewManager));
        });
        
        buttonPanel.add(viewDetailsBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadAllReservations() {
        try {
            allReservations = reservationService.getAllReservations();
            displayReservations(allReservations);
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading reservations: " + e.getMessage(), e);
        }
    }
    
    private void performSearch() {
        String searchValue = searchField.getText().trim();
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        
        try {
            List<Reservation> filteredReservations;
            
            if ("All".equals(searchType) || searchValue.isEmpty()) {
                filteredReservations = reservationService.getAllReservations();
            } else if ("Reservation ID".equals(searchType)) {
                try {
                    int reservationId = Integer.parseInt(searchValue);
                    Reservation reservation = reservationService.getReservationById(reservationId);
                    filteredReservations = reservation != null ? 
                        List.of(reservation) : List.of();
                } catch (NumberFormatException e) {
                    ErrorDialog.show(this, "Reservation ID must be a number.");
                    return;
                }
            } else if ("Customer Username".equals(searchType)) {
                Customer customer = customerService.getCustomerByUsername(searchValue);
                if (customer == null) {
                    ErrorDialog.show(this, "Customer not found: " + searchValue);
                    return;
                }
                filteredReservations = reservationService.getCustomerReservations(customer);
            } else {
                filteredReservations = allReservations;
            }
            
            displayReservations(filteredReservations);
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error searching reservations: " + e.getMessage(), e);
        }
    }
    
    private void displayReservations(List<Reservation> reservations) {
        tableModel.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Reservation reservation : reservations) {
            String customerName = "N/A";
            if (reservation.getCustomer() != null) {
                customerName = reservation.getCustomer().getFirstName() + " " + 
                              (reservation.getCustomer().getLastName() != null ? 
                               reservation.getCustomer().getLastName() : "");
            }
            
            String flightNumber = reservation.getFlight() != null ? 
                reservation.getFlight().getFlightNumber() : "N/A";
            
            String bookingDate = reservation.getBookingDate() != null ? 
                reservation.getBookingDate().format(formatter) : "N/A";
            
            String seats = "N/A";
            if (reservation.getSeats() != null && !reservation.getSeats().isEmpty()) {
                seats = reservation.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.joining(", "));
            }
            
            tableModel.addRow(new Object[]{
                reservation.getReservationId(),
                customerName,
                flightNumber,
                reservation.getStatus() != null ? reservation.getStatus().name() : "N/A",
                String.format("$%.2f", reservation.getTotalPrice()),
                bookingDate,
                seats
            });
        }
    }
    
    private void showReservationDetails() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a reservation to view details.");
            return;
        }
        
        int reservationId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (reservation == null) {
                ErrorDialog.show(this, "Reservation not found.");
                return;
            }
            
            showDetailsDialog(reservation);
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading reservation details: " + e.getMessage(), e);
        }
    }
    
    private void showDetailsDialog(Reservation reservation) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
            "Reservation Details", true);
        dialog.setLayout(new BorderLayout());
        
        StringBuilder details = new StringBuilder("<html>");
        details.append("<b>Reservation ID:</b> ").append(reservation.getReservationId()).append("<br>");
        details.append("<b>Status:</b> ").append(reservation.getStatus() != null ? 
            reservation.getStatus().name() : "N/A").append("<br>");
        details.append("<b>Total Price:</b> $").append(String.format("%.2f", reservation.getTotalPrice())).append("<br>");
        details.append("<b>Booking Date:</b> ").append(reservation.getBookingDate() != null ? 
            reservation.getBookingDate().toString() : "N/A").append("<br><br>");
        
        if (reservation.getCustomer() != null) {
            Customer customer = reservation.getCustomer();
            details.append("<b>Customer Information:</b><br>");
            details.append("  Name: ").append(customer.getFirstName()).append(" ");
            if (customer.getLastName() != null) {
                details.append(customer.getLastName());
            }
            details.append("<br>");
            details.append("  Username: ").append(customer.getUsername()).append("<br>");
            details.append("  Email: ").append(customer.getEmail()).append("<br>");
            details.append("  Membership: ").append(customer.getMembershipStatus() != null ? 
                customer.getMembershipStatus().name() : "REGULAR").append("<br><br>");
        }
        
        if (reservation.getFlight() != null) {
            Flight flight = reservation.getFlight();
            details.append("<b>Flight Information:</b><br>");
            details.append("  Flight Number: ").append(flight.getFlightNumber()).append("<br>");
            if (flight.getRoute() != null) {
                String origin = flight.getRoute().getOrigin() != null ? 
                    flight.getRoute().getOrigin().getAirportCode() : "N/A";
                String dest = flight.getRoute().getDestination() != null ? 
                    flight.getRoute().getDestination().getAirportCode() : "N/A";
                details.append("  Route: ").append(origin).append(" -> ").append(dest).append("<br>");
            }
            details.append("  Departure: ").append(flight.getDepartureTime() != null ? 
                flight.getDepartureTime().toString() : "N/A").append("<br>");
            details.append("  Arrival: ").append(flight.getArrivalTime() != null ? 
                flight.getArrivalTime().toString() : "N/A").append("<br>");
            details.append("  Status: ").append(flight.getStatus() != null ? 
                flight.getStatus().name() : "N/A").append("<br><br>");
        }
        
        if (reservation.getSeats() != null && !reservation.getSeats().isEmpty()) {
            details.append("<b>Seats:</b><br>");
            for (Seat seat : reservation.getSeats()) {
                details.append("  ").append(seat.getSeatNumber());
                if (seat.getSeatClass() != null) {
                    details.append(" (").append(seat.getSeatClass().name()).append(")");
                }
                details.append("<br>");
            }
            details.append("<br>");
        }
        
        if (reservation.getPayment() != null) {
            details.append("<b>Payment Information:</b><br>");
            details.append("  Payment ID: ").append(reservation.getPayment().getPaymentId()).append("<br>");
            details.append("  Amount: $").append(String.format("%.2f", 
                reservation.getPayment().getAmount())).append("<br>");
            details.append("  Status: ").append(reservation.getPayment().getStatus() != null ? 
                reservation.getPayment().getStatus().name() : "N/A").append("<br>");
            details.append("  Method: ").append(reservation.getPayment().getPaymentMethod() != null ? 
                reservation.getPayment().getPaymentMethod().name() : "N/A").append("<br>");
        }
        
        details.append("</html>");
        
        JLabel detailsLabel = new JLabel(details.toString());
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(detailsLabel);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void cancelSelectedReservation() {
        int selectedRow = reservationsTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a reservation to cancel.");
            return;
        }
        
        int reservationId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 3);
        
        if (ReservationStatus.CANCELLED.name().equals(status)) {
            ErrorDialog.show(this, "This reservation is already cancelled.");
            return;
        }
        
        if (ReservationStatus.COMPLETED.name().equals(status)) {
            ErrorDialog.show(this, "Cannot cancel a completed reservation.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel reservation #" + reservationId + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean cancelled = reservationService.cancelReservation(reservationId);
                if (cancelled) {
                    JOptionPane.showMessageDialog(this, "Reservation cancelled successfully.");
                    loadAllReservations();
                } else {
                    ErrorDialog.show(this, "Failed to cancel reservation.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error cancelling reservation: " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                ErrorDialog.show(this, e.getMessage());
            }
        }
    }
}
