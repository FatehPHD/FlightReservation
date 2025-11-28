package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.Airport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Manage airports (CRUD operations).
 * Add, edit, delete, and view all airports.
 * All database operations are performed through AdminService.
 */
public class ManageAirportsView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTable airportTable;
    private DefaultTableModel tableModel;
    private List<Airport> airportList;
    
    public ManageAirportsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        loadAirports();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Airports");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "Code", "Name", "City", "Country", "Timezone"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only, use buttons for editing
            }
        };
        
        airportTable = new JTable(tableModel);
        airportTable.setRowHeight(25);
        airportTable.setFont(new Font("Arial", Font.PLAIN, 12));
        airportTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        airportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(airportTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Airport");
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddAirportDialog());
        
        JButton editBtn = new JButton("Edit Airport");
        editBtn.setPreferredSize(new Dimension(120, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditAirportDialog());
        
        JButton deleteBtn = new JButton("Delete Airport");
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedAirport());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadAirports());
        
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
     * Load all airports using AdminService.getAllAirports().
     */
    private void loadAirports() {
        try {
            airportList = adminService.getAllAirports();
            updateTable();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading airports: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update the table with current airports.
     */
    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Airport airport : airportList) {
            Object[] row = {
                airport.getAirportCode() != null ? airport.getAirportCode() : "N/A",
                airport.getName() != null ? airport.getName() : "N/A",
                airport.getCity() != null ? airport.getCity() : "N/A",
                airport.getCountry() != null ? airport.getCountry() : "N/A",
                airport.getTimezone() != null ? airport.getTimezone() : "N/A"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Show dialog for adding a new airport.
     */
    private void showAddAirportDialog() {
        AirportFormDialog dialog = new AirportFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Add Airport", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAirports(); // Refresh table
        }
    }
    
    /**
     * Show dialog for editing an existing airport.
     */
    private void showEditAirportDialog() {
        int selectedRow = airportTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an airport to edit.");
            return;
        }
        
        Airport selectedAirport = airportList.get(selectedRow);
        
        AirportFormDialog dialog = new AirportFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Edit Airport", selectedAirport);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAirports(); // Refresh table
        }
    }
    
    /**
     * Delete the selected airport.
     */
    private void deleteSelectedAirport() {
        int selectedRow = airportTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an airport to delete.");
            return;
        }
        
        Airport selectedAirport = airportList.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete airport " + selectedAirport.getAirportCode() + 
            " (" + selectedAirport.getName() + ")?\n\n" +
            "This action cannot be undone. This airport cannot be used in routes if deleted.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.removeAirport(selectedAirport.getAirportCode());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Airport deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAirports(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to delete airport.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting airport: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Dialog for adding/editing airports.
     */
    private class AirportFormDialog extends JDialog {
        private Airport airport;
        private boolean confirmed = false;
        
        private JTextField codeField;
        private JTextField nameField;
        private JTextField cityField;
        private JTextField countryField;
        private JTextField timezoneField;
        
        public AirportFormDialog(JFrame parent, String title, Airport airportToEdit) {
            super(parent, title, true); // true = modal dialog
            this.airport = airportToEdit;
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
            
            // Airport Code
            formPanel.add(new JLabel("Airport Code:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            codeField = new JTextField(20);
            if (airport != null) {
                codeField.setText(airport.getAirportCode());
                codeField.setEditable(false); // Can't change code when editing (it's the primary key)
            } else {
                codeField.setToolTipText("3-letter IATA airport code (e.g., YYC for Calgary). " +
                    "This code is used as the primary identifier.");
            }
            formPanel.add(codeField, gbc);
            
            // Name
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            nameField = new JTextField(20);
            if (airport != null) {
                nameField.setText(airport.getName());
            }
            formPanel.add(nameField, gbc);
            
            // City
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("City:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            cityField = new JTextField(20);
            if (airport != null) {
                cityField.setText(airport.getCity());
            }
            formPanel.add(cityField, gbc);
            
            // Country
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Country:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            countryField = new JTextField(20);
            if (airport != null) {
                countryField.setText(airport.getCountry());
            }
            formPanel.add(countryField, gbc);
            
            // Timezone
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Timezone (e.g., America/Edmonton):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            timezoneField = new JTextField(20);
            if (airport != null) {
                timezoneField.setText(airport.getTimezone());
            }
            formPanel.add(timezoneField, gbc);
            
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
            // Validate airport code
            String code = codeField.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                ErrorDialog.show(this, "Airport code is required.");
                return false;
            }
            if (code.length() < 2 || code.length() > 10) {
                ErrorDialog.show(this, "Airport code must be between 2 and 10 characters.");
                return false;
            }
            
            // Validate name
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                ErrorDialog.show(this, "Name is required.");
                return false;
            }
            
            // Validate city
            String city = cityField.getText().trim();
            if (city.isEmpty()) {
                ErrorDialog.show(this, "City is required.");
                return false;
            }
            
            // Validate country
            String country = countryField.getText().trim();
            if (country.isEmpty()) {
                ErrorDialog.show(this, "Country is required.");
                return false;
            }
            
            // Validate timezone
            String timezone = timezoneField.getText().trim();
            if (timezone.isEmpty()) {
                ErrorDialog.show(this, "Timezone is required.");
                return false;
            }
            
            try {
                Airport airportToSave;
                if (airport == null) {
                    // Creating new airport
                    airportToSave = new Airport();
                    airportToSave.setAirportCode(code);
                    airportToSave.setName(name);
                    airportToSave.setCity(city);
                    airportToSave.setCountry(country);
                    airportToSave.setTimezone(timezone);
                    
                    Airport created = adminService.addAirport(airportToSave);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this,
                            "Airport added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to add airport.");
                        return false;
                    }
                } else {
                    // Updating existing airport
                    // Code is not editable when editing
                    airport.setName(name);
                    airport.setCity(city);
                    airport.setCountry(country);
                    airport.setTimezone(timezone);
                    
                    boolean success = adminService.updateAirport(airport);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "Airport updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to update airport.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error saving airport: " + e.getMessage(), e);
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
