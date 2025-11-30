package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.Flight;
import businesslogic.entities.Aircraft;
import businesslogic.entities.Route;
import businesslogic.entities.enums.FlightStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Manage flights (CRUD operations).
 * Add, edit, delete, and view all flights.
 * All database operations are performed through AdminService.
 */
public class ManageFlightsView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTable flightTable;
    private DefaultTableModel tableModel;
    private List<Flight> flights;
    private List<Aircraft> aircraftList;
    private List<Route> routesList;
    
    public ManageFlightsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        loadFlights();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Flights");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "Flight Number", "Departure", "Arrival", "Status", 
            "Available Seats", "Price", "Aircraft", "Route"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only, use buttons for editing
            }
        };
        
        flightTable = new JTable(tableModel);
        flightTable.setRowHeight(25);
        flightTable.setFont(new Font("Arial", Font.PLAIN, 12));
        flightTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Flight");
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddFlightDialog());
        
        JButton editBtn = new JButton("Edit Flight");
        editBtn.setPreferredSize(new Dimension(120, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditFlightDialog());
        
        JButton deleteBtn = new JButton("Cancel Flight");
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> cancelSelectedFlight());
        
        JButton deletePermanentBtn = new JButton("Delete Flight");
        deletePermanentBtn.setPreferredSize(new Dimension(120, 35));
        deletePermanentBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deletePermanentBtn.addActionListener(e -> deleteSelectedFlight());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadFlights());
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(120, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("ADMIN_DASHBOARD", 
                new AdminDashboardView(viewManager));
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(deletePermanentBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load all flights using AdminService.getAllFlights().
     */
    private void loadFlights() {
        try {
            flights = adminService.getAllFlights();
            updateTable();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading flights: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update the table with current flights.
     */
    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Flight flight : flights) {
            String departure = flight.getDepartureTime() != null
                ? flight.getDepartureTime().format(formatter) : "N/A";
            String arrival = flight.getArrivalTime() != null
                ? flight.getArrivalTime().format(formatter) : "N/A";
            String status = flight.getStatus() != null ? flight.getStatus().toString() : "N/A";
            String aircraft = flight.getAircraft() != null 
                ? flight.getAircraft().getModel() : "N/A";
            String route = "N/A";
            if (flight.getRoute() != null) {
                String origin = flight.getRoute().getOrigin() != null
                    ? flight.getRoute().getOrigin().getAirportCode() : "?";
                String dest = flight.getRoute().getDestination() != null
                    ? flight.getRoute().getDestination().getAirportCode() : "?";
                route = origin + " → " + dest;
            }
            
            tableModel.addRow(new Object[]{
                flight.getFlightNumber(),
                departure,
                arrival,
                status,
                flight.getAvailableSeats(),
                String.format("$%.2f", flight.getPrice()),
                aircraft,
                route
            });
        }
    }
    
    /**
     * Show dialog to add a new flight.
     */
    private void showAddFlightDialog() {
        loadDependencies(); // Load aircraft and routes for dropdowns
        
        // Check if dependencies are loaded
        if (aircraftList == null || aircraftList.isEmpty()) {
            ErrorDialog.show(this, 
                "Cannot add flight: No aircraft available in the system.\n" +
                "Please add aircraft first using 'Manage Aircraft'.");
            return;
        }
        
        if (routesList == null || routesList.isEmpty()) {
            ErrorDialog.show(this, 
                "Cannot add flight: No routes available in the system.\n" +
                "Please add routes first using 'Manage Routes'.");
            return;
        }
        
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            // Fallback: try to get frame from container
            Container container = viewManager.getContainer();
            while (container != null && !(container instanceof JFrame)) {
                container = container.getParent();
            }
            parentFrame = (JFrame) container;
        }
        
        if (parentFrame == null) {
            ErrorDialog.show(this, "Cannot open dialog: Parent window not found.");
            return;
        }
        
        FlightFormDialog dialog = new FlightFormDialog(
            parentFrame,
            "Add New Flight",
            null // null means it's a new flight
        );
        
        if (dialog.isConfirmed()) {
            Flight newFlight = dialog.getFlight();
            if (newFlight != null) {
                try {
                    adminService.addFlight(newFlight);
                    JOptionPane.showMessageDialog(this,
                        "Flight added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadFlights(); // Refresh table
                } catch (SQLException e) {
                    // Provide helpful error messages
                    String errorMsg = "Error adding flight: " + e.getMessage();
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("airline") || e.getMessage().contains("Cannot find airline")) {
                            String airlineCode = newFlight.getFlightNumber() != null && 
                                newFlight.getFlightNumber().length() >= 2 
                                ? newFlight.getFlightNumber().substring(0, 2).toUpperCase()
                                : "XX";
                            errorMsg += "\n\nTip: The airline code (first 2 letters of flight number) must exist in the database.\n" +
                                       "Flight '" + newFlight.getFlightNumber() + "' requires an airline with code '" + airlineCode + "'.\n" +
                                       "Please add the airline first using 'Manage Airlines'.";
                        } else if (e.getMessage().contains("Aircraft") || e.getMessage().contains("aircraft")) {
                            errorMsg += "\n\nTip: Make sure the selected aircraft exists in the database.\n" +
                                       "Aircraft ID: " + (newFlight.getAircraft() != null ? newFlight.getAircraft().getAircraftId() : "null");
                        } else if (e.getMessage().contains("Route") || e.getMessage().contains("route")) {
                            errorMsg += "\n\nTip: Make sure the selected route exists in the database.\n" +
                                       "Route ID: " + (newFlight.getRoute() != null ? newFlight.getRoute().getRouteId() : "null");
                        }
                    }
                    ErrorDialog.show(this, errorMsg, e);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    ErrorDialog.show(this, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Show dialog to edit selected flight.
     */
    private void showEditFlightDialog() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a flight to edit.");
            return;
        }
        
        Flight selectedFlight = flights.get(selectedRow);
        loadDependencies(); // Load aircraft and routes for dropdowns
        
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            Container container = viewManager.getContainer();
            while (container != null && !(container instanceof JFrame)) {
                container = container.getParent();
            }
            parentFrame = (JFrame) container;
        }
        
        if (parentFrame == null) {
            ErrorDialog.show(this, "Cannot open dialog: Parent window not found.");
            return;
        }
        
        FlightFormDialog dialog = new FlightFormDialog(
            parentFrame,
            "Edit Flight",
            selectedFlight
        );
        
        if (dialog.isConfirmed()) {
            Flight updatedFlight = dialog.getFlight();
            if (updatedFlight != null) {
                try {
                    boolean success = adminService.updateFlight(updatedFlight);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "Flight updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadFlights(); // Refresh table
                    } else {
                        ErrorDialog.show(this, "Failed to update flight.");
                    }
                } catch (SQLException e) {
                    ErrorDialog.show(this, "Error updating flight: " + e.getMessage(), e);
                } catch (IllegalArgumentException e) {
                    ErrorDialog.show(this, e.getMessage());
                }
            }
        }
    }
    
    /**
     * Cancel the selected flight.
     */
    private void cancelSelectedFlight() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a flight to cancel.");
            return;
        }
        
        Flight selectedFlight = flights.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel flight " + selectedFlight.getFlightNumber() + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.removeFlight(selectedFlight.getFlightNumber());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Flight cancelled successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadFlights(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to cancel flight.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error cancelling flight: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Permanently delete the selected flight.
     */
    private void deleteSelectedFlight() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a flight to delete.");
            return;
        }
        
        Flight selectedFlight = flights.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to PERMANENTLY DELETE flight " + selectedFlight.getFlightNumber() + "?\n\n" +
            "This action cannot be undone. All associated seats and reservations will also be deleted.",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.deleteFlight(selectedFlight.getFlightNumber());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Flight deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadFlights(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to delete flight.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting flight: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Load aircraft and routes for dropdowns.
     */
    private void loadDependencies() {
        try {
            aircraftList = adminService.getAllAircraft();
            routesList = adminService.getAllRoutes();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading dependencies: " + e.getMessage(), e);
        }
    }
    
    /**
     * Dialog for adding/editing flights.
     */
    private class FlightFormDialog extends JDialog {
        private Flight flight;
        private boolean confirmed = false;
        
        private JTextField flightNumberField;
        private JTextField departureTimeField;
        private JLabel arrivalTimeLabel; // Read-only label showing calculated arrival time
        private JComboBox<FlightStatus> statusComboBox;
        private JLabel availableSeatsLabel;
        private JTextField priceField;
        private JComboBox<String> aircraftComboBox;
        private JComboBox<String> routeComboBox;
        
        public FlightFormDialog(JFrame parent, String title, Flight flightToEdit) {
            super(parent, title, true); // true = modal dialog
            this.flight = flightToEdit;
            setModal(true); // Ensure dialog is modal
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            initDialog();
        }
        
        private void initDialog() {
            setLayout(new BorderLayout());
            
            // Form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 10, 5, 10);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            
            // Flight Number
            formPanel.add(new JLabel("Flight Number:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            flightNumberField = new JTextField(20);
            if (flight != null) {
                flightNumberField.setText(flight.getFlightNumber());
                flightNumberField.setEditable(false); // Can't change flight number when editing
            } else {
                flightNumberField.setToolTipText("Format: [AirlineCode][Number] (e.g., AC123). " +
                    "First 2 letters must match an existing airline code.");
            }
            formPanel.add(flightNumberField, gbc);
            
            // Departure Time
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Departure Time (YYYY-MM-DD HH:MM):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            departureTimeField = new JTextField(20);
            if (flight != null && flight.getDepartureTime() != null) {
                departureTimeField.setText(flight.getDepartureTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            formPanel.add(departureTimeField, gbc);
            
            // Arrival Time (calculated, read-only)
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Arrival Time (Calculated):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            arrivalTimeLabel = new JLabel("Select route to calculate");
            arrivalTimeLabel.setForeground(Color.GRAY);
            if (flight != null && flight.getArrivalTime() != null) {
                arrivalTimeLabel.setText(flight.getArrivalTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                arrivalTimeLabel.setForeground(Color.BLACK);
            }
            formPanel.add(arrivalTimeLabel, gbc);
            
            // Status
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            statusComboBox = new JComboBox<>(FlightStatus.values());
            if (flight != null && flight.getStatus() != null) {
                statusComboBox.setSelectedItem(flight.getStatus());
            }
            formPanel.add(statusComboBox, gbc);
            
            // Available Seats (Read-only, auto-calculated from aircraft)
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Available Seats:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            availableSeatsLabel = new JLabel("Select aircraft to see total seats");
            availableSeatsLabel.setForeground(Color.GRAY);
            if (flight != null && flight.getAircraft() != null) {
                availableSeatsLabel.setText(String.valueOf(flight.getAvailableSeats()));
                availableSeatsLabel.setForeground(Color.BLACK);
            }
            formPanel.add(availableSeatsLabel, gbc);
            
            // Price
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Price:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            priceField = new JTextField(20);
            if (flight != null) {
                priceField.setText(String.valueOf(flight.getPrice()));
            }
            formPanel.add(priceField, gbc);
            
            // Aircraft
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Aircraft:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            aircraftComboBox = new JComboBox<>();
            if (aircraftList != null && !aircraftList.isEmpty()) {
                for (Aircraft a : aircraftList) {
                    aircraftComboBox.addItem(a.getModel() + " (ID: " + a.getAircraftId() + ")");
                }
            } else {
                aircraftComboBox.addItem("No aircraft available");
                aircraftComboBox.setEnabled(false);
            }
            if (flight != null && flight.getAircraft() != null) {
                String aircraftStr = flight.getAircraft().getModel() + " (ID: " + 
                    flight.getAircraft().getAircraftId() + ")";
                aircraftComboBox.setSelectedItem(aircraftStr);
            }
            // Auto-populate available seats label when aircraft is selected
            aircraftComboBox.addActionListener(e -> {
                String selected = (String) aircraftComboBox.getSelectedItem();
                if (selected != null && !selected.equals("No aircraft available") && aircraftList != null) {
                    // Extract aircraft ID from selection string
                    try {
                        int idStart = selected.indexOf("(ID: ") + 5;
                        int idEnd = selected.indexOf(")", idStart);
                        if (idStart > 4 && idEnd > idStart) {
                            int aircraftId = Integer.parseInt(selected.substring(idStart, idEnd));
                            Aircraft selectedAircraft = aircraftList.stream()
                                .filter(a -> a.getAircraftId() == aircraftId)
                                .findFirst()
                                .orElse(null);
                            if (selectedAircraft != null) {
                                availableSeatsLabel.setText(String.valueOf(selectedAircraft.getTotalSeats()));
                                availableSeatsLabel.setForeground(Color.BLACK);
                            }
                        }
                    } catch (Exception ex) {
                        // Ignore parsing errors
                    }
                } else {
                    availableSeatsLabel.setText("Select aircraft to see total seats");
                    availableSeatsLabel.setForeground(Color.GRAY);
                }
            });
            formPanel.add(aircraftComboBox, gbc);
            
            // Route
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Route:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            routeComboBox = new JComboBox<>();
            if (routesList != null && !routesList.isEmpty()) {
                for (Route r : routesList) {
                    String origin = r.getOrigin() != null ? r.getOrigin().getAirportCode() : "?";
                    String dest = r.getDestination() != null ? r.getDestination().getAirportCode() : "?";
                    routeComboBox.addItem(origin + " → " + dest + " (ID: " + r.getRouteId() + ")");
                }
            } else {
                routeComboBox.addItem("No routes available");
                routeComboBox.setEnabled(false);
            }
            if (flight != null && flight.getRoute() != null) {
                String origin = flight.getRoute().getOrigin() != null 
                    ? flight.getRoute().getOrigin().getAirportCode() : "?";
                String dest = flight.getRoute().getDestination() != null
                    ? flight.getRoute().getDestination().getAirportCode() : "?";
                String routeStr = origin + " → " + dest + " (ID: " + flight.getRoute().getRouteId() + ")";
                routeComboBox.setSelectedItem(routeStr);
            }
            formPanel.add(routeComboBox, gbc);
            
            // Add listeners to departure time and route to calculate arrival time (after routeComboBox is created)
            departureTimeField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent evt) {
                    calculateArrivalTime();
                }
            });
            routeComboBox.addActionListener(e -> calculateArrivalTime());
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel();
            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                if (validateAndSave()) {
                    confirmed = true;
                    dispose();
                }
            });
            
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> dispose());
            
            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            add(buttonPanel, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(getParent());
            setVisible(true); // Show the dialog
        }
        
        private boolean validateAndSave() {
            // Validation
            String flightNumber = flightNumberField.getText().trim();
            if (flightNumber.isEmpty()) {
                ErrorDialog.show(this, "Flight number is required.");
                return false;
            }
            
            // Validate flight number format (should start with 2-letter airline code)
            if (flightNumber.length() < 3) {
                ErrorDialog.show(this, 
                    "Flight number must be at least 3 characters.\n" +
                    "Format: [AirlineCode][Number] (e.g., AC123)\n" +
                    "The first 2 characters must match an existing airline code.");
                return false;
            }
            
            if (departureTimeField.getText().trim().isEmpty()) {
                ErrorDialog.show(this, "Departure time is required.");
                return false;
            }
            
            // Parse departure date
            LocalDateTime departureTime;
            try {
                departureTime = LocalDateTime.parse(departureTimeField.getText().trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException e) {
                ErrorDialog.show(this, "Invalid date format. Use YYYY-MM-DD HH:MM (e.g., 2024-12-25 14:30)");
                return false;
            }
            
            // Parse price
            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
                if (price < 0) {
                    ErrorDialog.show(this, "Price must be non-negative.");
                    return false;
                }
            } catch (NumberFormatException e) {
                ErrorDialog.show(this, "Price must be a valid number.");
                return false;
            }
            
            // Get selected aircraft
            if (aircraftComboBox.getSelectedItem() == null || aircraftList == null || aircraftList.isEmpty()) {
                ErrorDialog.show(this, "Please select an aircraft. If none are available, add aircraft first using 'Manage Aircraft'.");
                return false;
            }
            String aircraftStr = (String) aircraftComboBox.getSelectedItem();
            int aircraftId;
            try {
                aircraftId = Integer.parseInt(aircraftStr.substring(aircraftStr.indexOf("ID: ") + 4, 
                    aircraftStr.indexOf(")")));
            } catch (Exception e) {
                ErrorDialog.show(this, "Error parsing aircraft ID. Please select a valid aircraft.");
                return false;
            }
            Aircraft selectedAircraft = aircraftList.stream()
                .filter(a -> a.getAircraftId() == aircraftId)
                .findFirst()
                .orElse(null);
            
            if (selectedAircraft == null) {
                ErrorDialog.show(this, "Selected aircraft not found. Please select a valid aircraft.");
                return false;
            }
            
            // Available seats automatically equals aircraft's total seats for new flights
            // For existing flights, use the current available seats
            int availableSeats;
            if (flight == null) {
                // New flight: available seats = aircraft total seats
                availableSeats = selectedAircraft.getTotalSeats();
            } else {
                // Existing flight: keep current available seats
                availableSeats = flight.getAvailableSeats();
            }
            
            // Get selected route
            if (routeComboBox.getSelectedItem() == null || routesList == null || routesList.isEmpty()) {
                ErrorDialog.show(this, "Please select a route. If none are available, add routes first using 'Manage Routes'.");
                return false;
            }
            String routeStr = (String) routeComboBox.getSelectedItem();
            int routeId;
            try {
                routeId = Integer.parseInt(routeStr.substring(routeStr.indexOf("ID: ") + 4,
                    routeStr.indexOf(")")));
            } catch (Exception e) {
                ErrorDialog.show(this, "Error parsing route ID. Please select a valid route.");
                return false;
            }
            Route selectedRoute = routesList.stream()
                .filter(r -> r.getRouteId() == routeId)
                .findFirst()
                .orElse(null);
            
            if (selectedRoute == null) {
                ErrorDialog.show(this, "Selected route not found. Please select a valid route.");
                return false;
            }
            
            // Calculate arrival time from departure time + route's estimated duration
            LocalDateTime arrivalTime = departureTime.plusMinutes(selectedRoute.getEstimatedDuration());
            
            // Create or update flight
            if (flight == null) {
                // New flight
                flight = new Flight();
            }
            
            flight.setFlightNumber(flightNumber);
            flight.setDepartureTime(departureTime);
            flight.setArrivalTime(arrivalTime); // Automatically calculated
            flight.setStatus((FlightStatus) statusComboBox.getSelectedItem());
            flight.setAvailableSeats(availableSeats);
            flight.setPrice(price);
            flight.setAircraft(selectedAircraft);
            flight.setRoute(selectedRoute);
            
            return true;
        }
        
        /**
         * Calculate and display arrival time based on departure time and selected route.
         */
        private void calculateArrivalTime() {
            try {
                String depTimeStr = departureTimeField.getText().trim();
                if (depTimeStr.isEmpty()) {
                    arrivalTimeLabel.setText("Enter departure time");
                    arrivalTimeLabel.setForeground(Color.GRAY);
                    return;
                }
                
                LocalDateTime departureTime = LocalDateTime.parse(depTimeStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
                // Get selected route
                if (routeComboBox.getSelectedItem() == null || routesList == null || routesList.isEmpty()) {
                    arrivalTimeLabel.setText("Select route to calculate");
                    arrivalTimeLabel.setForeground(Color.GRAY);
                    return;
                }
                
                String routeStr = (String) routeComboBox.getSelectedItem();
                int routeId;
                try {
                    routeId = Integer.parseInt(routeStr.substring(routeStr.indexOf("ID: ") + 4,
                        routeStr.indexOf(")")));
                } catch (Exception e) {
                    arrivalTimeLabel.setText("Invalid route");
                    arrivalTimeLabel.setForeground(Color.GRAY);
                    return;
                }
                
                Route selectedRoute = routesList.stream()
                    .filter(r -> r.getRouteId() == routeId)
                    .findFirst()
                    .orElse(null);
                
                if (selectedRoute == null) {
                    arrivalTimeLabel.setText("Route not found");
                    arrivalTimeLabel.setForeground(Color.GRAY);
                    return;
                }
                
                // Calculate arrival time
                LocalDateTime arrivalTime = departureTime.plusMinutes(selectedRoute.getEstimatedDuration());
                arrivalTimeLabel.setText(arrivalTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                arrivalTimeLabel.setForeground(Color.BLACK);
                
            } catch (DateTimeParseException e) {
                arrivalTimeLabel.setText("Invalid departure time format");
                arrivalTimeLabel.setForeground(Color.RED);
            } catch (Exception e) {
                arrivalTimeLabel.setText("Error calculating");
                arrivalTimeLabel.setForeground(Color.RED);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Flight getFlight() {
            return flight;
        }
    }
}