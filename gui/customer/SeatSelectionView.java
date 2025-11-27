package gui.customer;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import businesslogic.entities.enums.SeatClass;
import businesslogic.services.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Seat selection interface.
 * Shows seat map and allows customers to select available seats.
 * Uses ReservationService to load seats from the database.
 */
public class SeatSelectionView extends JPanel {
    
    private ViewManager viewManager;
    private Flight flight;
    private ReservationService reservationService;
    private List<Seat> allSeats;
    private List<Seat> selectedSeats;
    private JPanel seatMapPanel;
    private JLabel selectedSeatsLabel;
    private JLabel totalPriceLabel;
    
    public SeatSelectionView(ViewManager viewManager, Flight flight) {
        this.viewManager = viewManager;
        this.flight = flight;
        this.reservationService = viewManager.getReservationService();
        this.selectedSeats = new ArrayList<>();
        initComponents();
        loadSeats();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Select Seats");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title);
        
        // Flight info
        String flightInfo = "Flight: " + flight.getFlightNumber();
        if (flight.getRoute() != null) {
            String origin = flight.getRoute().getOrigin() != null 
                ? flight.getRoute().getOrigin().getAirportCode() : "N/A";
            String destination = flight.getRoute().getDestination() != null
                ? flight.getRoute().getDestination().getAirportCode() : "N/A";
            flightInfo += " | " + origin + " → " + destination;
        }
        JLabel flightLabel = new JLabel(flightInfo);
        flightLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        flightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(flightLabel);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Seat map panel (will be populated with seats)
        seatMapPanel = new JPanel();
        seatMapPanel.setLayout(new GridBagLayout());
        seatMapPanel.setBorder(BorderFactory.createTitledBorder("Seat Map"));
        
        JScrollPane scrollPane = new JScrollPane(seatMapPanel);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Info and control panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        legendPanel.add(new JLabel("Legend: "));
        
        JButton availableBtn = new JButton("Available");
        availableBtn.setEnabled(false);
        availableBtn.setBackground(new Color(144, 238, 144)); // Light green
        availableBtn.setPreferredSize(new Dimension(80, 25));
        legendPanel.add(availableBtn);
        
        JButton selectedBtn = new JButton("Selected");
        selectedBtn.setEnabled(false);
        selectedBtn.setBackground(new Color(255, 200, 100)); // Orange
        selectedBtn.setPreferredSize(new Dimension(80, 25));
        legendPanel.add(selectedBtn);
        
        JButton occupiedBtn = new JButton("Occupied");
        occupiedBtn.setEnabled(false);
        occupiedBtn.setBackground(new Color(255, 150, 150)); // Light red
        occupiedBtn.setPreferredSize(new Dimension(80, 25));
        legendPanel.add(occupiedBtn);
        
        infoPanel.add(legendPanel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Selected seats display
        selectedSeatsLabel = new JLabel("Selected seats: None");
        selectedSeatsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.add(selectedSeatsLabel);
        
        // Total price display
        totalPriceLabel = new JLabel("Total: $0.00");
        totalPriceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(totalPriceLabel);
        
        add(infoPanel, BorderLayout.EAST);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(120, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            // Go back to flight search
            viewManager.showView("FLIGHT_SEARCH", 
                new FlightSearchView(viewManager));
        });
        
        JButton continueBtn = new JButton("Continue to Payment");
        continueBtn.setPreferredSize(new Dimension(200, 35));
        continueBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        continueBtn.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                ErrorDialog.show(this, "Please select at least one seat.");
                return;
            }
            
            // Navigate to payment view with flight and selected seats
            // Note: We'll pass flight and seats, PaymentView will create reservation
            viewManager.showView("PAYMENT", 
                new PaymentView(viewManager, flight, selectedSeats));
        });
        
        buttonPanel.add(backBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(continueBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load available seats for the flight using ReservationService.
     */
    private void loadSeats() {
        try {
            // Get all seats (available and unavailable) to show full seat map
            allSeats = reservationService.getAllSeatsForFlight(flight);
            
            if (allSeats.isEmpty()) {
                JLabel noSeatsLabel = new JLabel("No seats available for this flight.");
                noSeatsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                noSeatsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                seatMapPanel.add(noSeatsLabel);
                return;
            }
            
            // Group seats by class
            List<Seat> economySeats = allSeats.stream()
                .filter(s -> s.getSeatClass() == SeatClass.ECONOMY)
                .sorted((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()))
                .collect(Collectors.toList());
            
            List<Seat> businessSeats = allSeats.stream()
                .filter(s -> s.getSeatClass() == SeatClass.BUSINESS)
                .sorted((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()))
                .collect(Collectors.toList());
            
            List<Seat> firstSeats = allSeats.stream()
                .filter(s -> s.getSeatClass() == SeatClass.FIRST)
                .sorted((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()))
                .collect(Collectors.toList());
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            
            // Display seats by class
            if (!firstSeats.isEmpty()) {
                addSeatClassSection("First Class", firstSeats, gbc);
                gbc.gridy++;
            }
            
            if (!businessSeats.isEmpty()) {
                addSeatClassSection("Business Class", businessSeats, gbc);
                gbc.gridy++;
            }
            
            if (!economySeats.isEmpty()) {
                addSeatClassSection("Economy Class", economySeats, gbc);
            }
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading seats: " + e.getMessage(), e);
        }
    }
    
    /**
     * Add a section of seats for a specific class.
     */
    private void addSeatClassSection(String className, List<Seat> seats, GridBagConstraints gbc) {
        // Class label
        gbc.gridx = 0;
        gbc.gridwidth = 10;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel classLabel = new JLabel(className + ":");
        classLabel.setFont(new Font("Arial", Font.BOLD, 14));
        seatMapPanel.add(classLabel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        
        // Seat buttons in a grid
        int seatsPerRow = 6; // Adjust based on your preference
        int currentRow = gbc.gridy;
        int currentCol = 0;
        
        for (Seat seat : seats) {
            gbc.gridx = currentCol;
            gbc.gridy = currentRow;
            gbc.anchor = GridBagConstraints.CENTER;
            
            JButton seatBtn = createSeatButton(seat);
            seatMapPanel.add(seatBtn, gbc);
            
            currentCol++;
            if (currentCol >= seatsPerRow) {
                currentCol = 0;
                currentRow++;
            }
        }
        
        gbc.gridy = currentRow + 1;
    }
    
    /**
     * Create a button for a seat.
     */
    private JButton createSeatButton(Seat seat) {
        JButton btn = new JButton(seat.getSeatNumber());
        btn.setPreferredSize(new Dimension(60, 35));
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        
        if (seat.isAvailable()) {
            btn.setBackground(new Color(144, 238, 144)); // Light green
            btn.setEnabled(true);
            btn.addActionListener(e -> toggleSeatSelection(seat, btn));
        } else {
            btn.setBackground(new Color(255, 150, 150)); // Light red
            btn.setEnabled(false);
            btn.setToolTipText("Seat is already occupied");
        }
        
        return btn;
    }
    
    /**
     * Toggle seat selection.
     */
    private void toggleSeatSelection(Seat seat, JButton btn) {
        if (selectedSeats.contains(seat)) {
            // Deselect
            selectedSeats.remove(seat);
            btn.setBackground(new Color(144, 238, 144)); // Light green
            btn.setBorder(null);
        } else {
            // Select
            selectedSeats.add(seat);
            btn.setBackground(new Color(255, 200, 100)); // Orange
            btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
        
        updateSelectionDisplay();
    }
    
    /**
     * Update the selected seats and total price display.
     */
    private void updateSelectionDisplay() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("Selected seats: None");
            totalPriceLabel.setText("Total: $0.00");
        } else {
            String seatNumbers = selectedSeats.stream()
                .map(Seat::getSeatNumber)
                .collect(Collectors.joining(", "));
            selectedSeatsLabel.setText("Selected seats: " + seatNumbers);
            
            // Calculate total price
            double total = calculateTotalPrice();
            totalPriceLabel.setText(String.format("Total: $%.2f", total));
        }
    }
    
    /**
     * Calculate total price for selected seats.
     */
    private double calculateTotalPrice() {
        double basePrice = flight.getPrice();
        double total = 0.0;
        
        for (Seat seat : selectedSeats) {
            double seatPrice = basePrice;
            
            // Apply seat class multiplier
            if (seat.getSeatClass() != null) {
                switch (seat.getSeatClass()) {
                    case BUSINESS:
                        seatPrice *= 1.5; // 50% premium
                        break;
                    case FIRST:
                        seatPrice *= 2.5; // 150% premium
                        break;
                    case ECONOMY:
                    default:
                        // Base price
                        break;
                }
            }
            
            total += seatPrice;
        }
        
        return total;
    }
}
