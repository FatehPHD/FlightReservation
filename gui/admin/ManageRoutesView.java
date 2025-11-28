package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.Route;
import businesslogic.entities.Airport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Manage routes (CRUD operations).
 * Add, edit, delete, and view all routes.
 * All database operations are performed through AdminService.
 */
public class ManageRoutesView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTable routeTable;
    private DefaultTableModel tableModel;
    private List<Route> routeList;
    private List<Airport> airportList;
    
    public ManageRoutesView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        loadRoutes();
        loadAirports();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Routes");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "ID", "Origin", "Destination", "Distance (km)", "Duration (min)"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only, use buttons for editing
            }
        };
        
        routeTable = new JTable(tableModel);
        routeTable.setRowHeight(25);
        routeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        routeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        routeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(routeTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Route");
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddRouteDialog());
        
        JButton editBtn = new JButton("Edit Route");
        editBtn.setPreferredSize(new Dimension(120, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditRouteDialog());
        
        JButton deleteBtn = new JButton("Delete Route");
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedRoute());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> {
            loadRoutes();
            loadAirports();
        });
        
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
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load all routes using AdminService.getAllRoutes().
     */
    private void loadRoutes() {
        try {
            routeList = adminService.getAllRoutes();
            updateTable();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading routes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load all airports for dropdowns.
     */
    private void loadAirports() {
        try {
            airportList = adminService.getAllAirports();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading airports: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update the table with current routes.
     */
    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Route route : routeList) {
            String origin = route.getOrigin() != null && route.getOrigin().getAirportCode() != null
                ? route.getOrigin().getAirportCode() : "N/A";
            String destination = route.getDestination() != null && route.getDestination().getAirportCode() != null
                ? route.getDestination().getAirportCode() : "N/A";
            
            Object[] row = {
                route.getRouteId(),
                origin,
                destination,
                route.getDistance() > 0 ? String.format("%.2f", route.getDistance()) : "N/A",
                route.getEstimatedDuration() > 0 ? route.getEstimatedDuration() : "N/A"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Show dialog for adding a new route.
     */
    private void showAddRouteDialog() {
        if (airportList == null || airportList.isEmpty()) {
            ErrorDialog.show(this, "No airports available. Please add airports first.");
            return;
        }
        
        RouteFormDialog dialog = new RouteFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Add Route", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadRoutes(); // Refresh table
        }
    }
    
    /**
     * Show dialog for editing an existing route.
     */
    private void showEditRouteDialog() {
        int selectedRow = routeTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a route to edit.");
            return;
        }
        
        if (airportList == null || airportList.isEmpty()) {
            ErrorDialog.show(this, "No airports available. Please add airports first.");
            return;
        }
        
        Route selectedRoute = routeList.get(selectedRow);
        
        RouteFormDialog dialog = new RouteFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Edit Route", selectedRoute);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadRoutes(); // Refresh table
        }
    }
    
    /**
     * Delete the selected route.
     */
    private void deleteSelectedRoute() {
        int selectedRow = routeTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a route to delete.");
            return;
        }
        
        Route selectedRoute = routeList.get(selectedRow);
        String originCode = selectedRoute.getOrigin() != null ? 
            selectedRoute.getOrigin().getAirportCode() : "N/A";
        String destCode = selectedRoute.getDestination() != null ? 
            selectedRoute.getDestination().getAirportCode() : "N/A";
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete route " + originCode + " → " + destCode + 
            " (ID: " + selectedRoute.getRouteId() + ")?\n\n" +
            "This action cannot be undone. All flights using this route will also be deleted.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.removeRoute(selectedRoute.getRouteId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Route deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadRoutes(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to delete route.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting route: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Dialog for adding/editing routes.
     */
    private class RouteFormDialog extends JDialog {
        private Route route;
        private boolean confirmed = false;
        
        private JComboBox<String> originComboBox;
        private JComboBox<String> destinationComboBox;
        private JTextField distanceField;
        private JTextField durationField;
        
        public RouteFormDialog(JFrame parent, String title, Route routeToEdit) {
            super(parent, title, true); // true = modal dialog
            this.route = routeToEdit;
            setModal(true);
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
            
            // Origin Airport
            formPanel.add(new JLabel("Origin Airport:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            originComboBox = new JComboBox<>();
            if (airportList != null && !airportList.isEmpty()) {
                for (Airport airport : airportList) {
                    String display = airport.getAirportCode() + " - " + airport.getName() + 
                        " (" + airport.getCity() + ")";
                    originComboBox.addItem(display);
                }
            } else {
                originComboBox.addItem("No airports available");
                originComboBox.setEnabled(false);
            }
            if (route != null && route.getOrigin() != null) {
                String originDisplay = route.getOrigin().getAirportCode() + " - " + 
                    route.getOrigin().getName() + " (" + route.getOrigin().getCity() + ")";
                originComboBox.setSelectedItem(originDisplay);
            }
            formPanel.add(originComboBox, gbc);
            
            // Destination Airport
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Destination Airport:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            destinationComboBox = new JComboBox<>();
            if (airportList != null && !airportList.isEmpty()) {
                for (Airport airport : airportList) {
                    String display = airport.getAirportCode() + " - " + airport.getName() + 
                        " (" + airport.getCity() + ")";
                    destinationComboBox.addItem(display);
                }
            } else {
                destinationComboBox.addItem("No airports available");
                destinationComboBox.setEnabled(false);
            }
            if (route != null && route.getDestination() != null) {
                String destDisplay = route.getDestination().getAirportCode() + " - " + 
                    route.getDestination().getName() + " (" + route.getDestination().getCity() + ")";
                destinationComboBox.setSelectedItem(destDisplay);
            }
            formPanel.add(destinationComboBox, gbc);
            
            // Distance
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Distance (km):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            distanceField = new JTextField(20);
            if (route != null && route.getDistance() > 0) {
                distanceField.setText(String.valueOf(route.getDistance()));
            }
            formPanel.add(distanceField, gbc);
            
            // Estimated Duration
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Estimated Duration (minutes):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            durationField = new JTextField(20);
            if (route != null && route.getEstimatedDuration() > 0) {
                durationField.setText(String.valueOf(route.getEstimatedDuration()));
            }
            formPanel.add(durationField, gbc);
            
            // Buttons panel
            JPanel buttonPanel = new JPanel();
            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");
            
            saveBtn.addActionListener(e -> {
                if (validateAndSave()) {
                    confirmed = true;
                    dispose();
                }
            });
            
            cancelBtn.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            
            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            
            // Add panels to dialog
            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            
            pack();
            setLocationRelativeTo(getParent());
        }
        
        private boolean validateAndSave() {
            // Validate origin and destination selection
            if (originComboBox.getSelectedItem() == null || 
                originComboBox.getSelectedItem().toString().equals("No airports available")) {
                ErrorDialog.show(this, "Please select an origin airport.");
                return false;
            }
            
            if (destinationComboBox.getSelectedItem() == null || 
                destinationComboBox.getSelectedItem().toString().equals("No airports available")) {
                ErrorDialog.show(this, "Please select a destination airport.");
                return false;
            }
            
            // Extract airport codes from selected items
            String originDisplay = originComboBox.getSelectedItem().toString();
            String destDisplay = destinationComboBox.getSelectedItem().toString();
            
            // Extract airport code (first 3 characters before " - ")
            String originCode = originDisplay.split(" - ")[0];
            String destCode = destDisplay.split(" - ")[0];
            
            // Validate origin != destination
            if (originCode.equals(destCode)) {
                ErrorDialog.show(this, "Origin and destination airports must be different.");
                return false;
            }
            
            // Find Airport objects
            Airport originAirport = null;
            Airport destAirport = null;
            for (Airport airport : airportList) {
                if (airport.getAirportCode().equals(originCode)) {
                    originAirport = airport;
                }
                if (airport.getAirportCode().equals(destCode)) {
                    destAirport = airport;
                }
            }
            
            if (originAirport == null || destAirport == null) {
                ErrorDialog.show(this, "Selected airport not found.");
                return false;
            }
            
            // Validate distance (optional, but if provided must be positive)
            double distance = 0.0;
            String distanceStr = distanceField.getText().trim();
            if (!distanceStr.isEmpty()) {
                try {
                    distance = Double.parseDouble(distanceStr);
                    if (distance < 0) {
                        ErrorDialog.show(this, "Distance must be a positive number.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    ErrorDialog.show(this, "Distance must be a valid number.");
                    return false;
                }
            }
            
            // Validate duration (optional, but if provided must be positive)
            int duration = 0;
            String durationStr = durationField.getText().trim();
            if (!durationStr.isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr);
                    if (duration < 0) {
                        ErrorDialog.show(this, "Duration must be a positive number.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    ErrorDialog.show(this, "Duration must be a valid number.");
                    return false;
                }
            }
            
            try {
                Route routeToSave;
                if (route == null) {
                    // Creating new route
                    routeToSave = new Route();
                    routeToSave.setOrigin(originAirport);
                    routeToSave.setDestination(destAirport);
                    routeToSave.setDistance(distance);
                    routeToSave.setEstimatedDuration(duration);
                    
                    Route created = adminService.addRoute(routeToSave);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this,
                            "Route added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to add route.");
                        return false;
                    }
                } else {
                    // Updating existing route
                    route.setOrigin(originAirport);
                    route.setDestination(destAirport);
                    route.setDistance(distance);
                    route.setEstimatedDuration(duration);
                    
                    boolean success = adminService.updateRoute(route);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "Route updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to update route.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error saving route: " + e.getMessage(), e);
                return false;
            } catch (IllegalArgumentException e) {
                ErrorDialog.show(this, e.getMessage());
                return false;
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
    }
}
