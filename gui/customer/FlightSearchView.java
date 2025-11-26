package gui.customer;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.Airport;
import businesslogic.entities.Flight;
import businesslogic.services.FlightService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Flight search form.
 * Allows customers to search for flights by origin, destination, and date.
 * Loads airports from database and uses FlightService to search flights.
 */
public class FlightSearchView extends JPanel {
    
    private ViewManager viewManager;
    private FlightService flightService;
    private JComboBox<String> originComboBox;
    private JComboBox<String> destinationComboBox;
    private JTextField dateField;
    
    public FlightSearchView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.flightService = viewManager.getFlightService();
        initComponents();
        loadAirports();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Title
        JLabel title = new JLabel("Search Flights");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);
        gbc.gridwidth = 1;
        
        // Origin label and dropdown
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Origin:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        originComboBox = new JComboBox<>();
        originComboBox.setPreferredSize(new Dimension(300, 30));
        add(originComboBox, gbc);
        
        // Destination label and dropdown
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        add(new JLabel("Destination:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        destinationComboBox = new JComboBox<>();
        destinationComboBox.setPreferredSize(new Dimension(300, 30));
        add(destinationComboBox, gbc);
        
        // Date label and field
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        add(new JLabel("Departure Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        dateField = new JTextField(25);
        dateField.setPreferredSize(new Dimension(300, 30));
        add(dateField, gbc);
        
        // Search button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        gbc.insets = new Insets(30, 15, 15, 15);
        JButton searchBtn = new JButton("Search Flights");
        searchBtn.setPreferredSize(new Dimension(200, 40));
        searchBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        searchBtn.addActionListener(e -> performSearch());
        add(searchBtn, gbc);
        
        // Back button
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 15, 15, 15);
        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setPreferredSize(new Dimension(200, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", 
                new CustomerDashboardView(viewManager));
        });
        add(backBtn, gbc);
    }
    
    /**
     * Load all airports from database and populate dropdowns.
     */
    private void loadAirports() {
        try {
            List<Airport> airports = flightService.getAllAirports();
            
            // Sort airports by code
            List<String> airportCodes = airports.stream()
                .map(Airport::getAirportCode)
                .sorted()
                .collect(Collectors.toList());
            
            // Independent combo models (Update #1)
            originComboBox.setModel(new DefaultComboBoxModel<>(
                airportCodes.toArray(new String[0])
            ));
            
            destinationComboBox.setModel(new DefaultComboBoxModel<>(
                airportCodes.toArray(new String[0])
            ));
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Failed to load airports: " + e.getMessage());
        }
    }
    
    /**
     * Perform flight search using FlightService.
     */
    private void performSearch() {
        String originCode = (String) originComboBox.getSelectedItem();
        String destinationCode = (String) destinationComboBox.getSelectedItem();
        String dateStr = dateField.getText().trim();
        
        // Validation
        if (originCode == null || originCode.isEmpty()) {
            ErrorDialog.show(this, "Please select an origin airport.");
            return;
        }
        
        if (destinationCode == null || destinationCode.isEmpty()) {
            ErrorDialog.show(this, "Please select a destination airport.");
            return;
        }
        
        if (originCode.equals(destinationCode)) {
            ErrorDialog.show(this, "Origin and destination must be different.");
            return;
        }
        
        if (dateStr.isEmpty()) {
            ErrorDialog.show(this, "Please enter a departure date.");
            return;
        }
        
        // Parse date
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            ErrorDialog.show(this, "Invalid date format. Please use YYYY-MM-DD (e.g., 2024-12-25).");
            return;
        }
        
        // Past date
        if (date.isBefore(LocalDate.now())) {
            ErrorDialog.show(this, "Departure date cannot be in the past.");
            return;
        }
        
        // Perform search
        try {
            List<Flight> flights = flightService.searchFlights(originCode, destinationCode, date);

            // Update #2 — If no flights found, do NOT navigate
            if (flights.isEmpty()) {
                ErrorDialog.show(this, "No flights found for your selected route and date.");
                return;
            }
            
            // Navigate to results view
            viewManager.showView("FLIGHT_RESULTS", 
                new FlightResultsView(viewManager, flights));
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error searching flights: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            ErrorDialog.show(this, e.getMessage());
        }
    }
}
