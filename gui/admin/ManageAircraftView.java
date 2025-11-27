package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.Aircraft;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Manage aircraft (CRUD operations).
 * Add, edit, delete, and view all aircraft.
 * All database operations are performed through AdminService.
 */
public class ManageAircraftView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTable aircraftTable;
    private DefaultTableModel tableModel;
    private List<Aircraft> aircraftList;
    
    public ManageAircraftView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        loadAircraft();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Aircraft");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "ID", "Model", "Manufacturer", "Total Seats", 
            "Seat Configuration", "Status"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only, use buttons for editing
            }
        };
        
        aircraftTable = new JTable(tableModel);
        aircraftTable.setRowHeight(25);
        aircraftTable.setFont(new Font("Arial", Font.PLAIN, 12));
        aircraftTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        aircraftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(aircraftTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Aircraft");
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddAircraftDialog());
        
        JButton editBtn = new JButton("Edit Aircraft");
        editBtn.setPreferredSize(new Dimension(120, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditAircraftDialog());
        
        JButton deleteBtn = new JButton("Delete Aircraft");
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedAircraft());
        
        JButton changeStatusBtn = new JButton("Change Status");
        changeStatusBtn.setPreferredSize(new Dimension(120, 35));
        changeStatusBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        changeStatusBtn.addActionListener(e -> showChangeStatusDialog());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadAircraft());
        
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
        buttonPanel.add(changeStatusBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Load all aircraft using AdminService.getAllAircraft().
     */
    private void loadAircraft() {
        try {
            aircraftList = adminService.getAllAircraft();
            updateTable();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading aircraft: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update the table with current aircraft.
     */
    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Aircraft aircraft : aircraftList) {
            Object[] row = {
                aircraft.getAircraftId(),
                aircraft.getModel() != null ? aircraft.getModel() : "N/A",
                aircraft.getManufacturer() != null ? aircraft.getManufacturer() : "N/A",
                aircraft.getTotalSeats(),
                aircraft.getSeatConfiguration() != null ? aircraft.getSeatConfiguration() : "N/A",
                aircraft.getStatus() != null ? aircraft.getStatus() : "N/A"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Show dialog for adding a new aircraft.
     */
    private void showAddAircraftDialog() {
        AircraftFormDialog dialog = new AircraftFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Add Aircraft", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAircraft(); // Refresh table
        }
    }
    
    /**
     * Show dialog for editing an existing aircraft.
     */
    private void showEditAircraftDialog() {
        int selectedRow = aircraftTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an aircraft to edit.");
            return;
        }
        
        Aircraft selectedAircraft = aircraftList.get(selectedRow);
        
        AircraftFormDialog dialog = new AircraftFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Edit Aircraft", selectedAircraft);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAircraft(); // Refresh table
        }
    }
    
    /**
     * Delete the selected aircraft.
     */
    private void deleteSelectedAircraft() {
        int selectedRow = aircraftTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an aircraft to delete.");
            return;
        }
        
        Aircraft selectedAircraft = aircraftList.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete aircraft " + selectedAircraft.getModel() + 
            " (ID: " + selectedAircraft.getAircraftId() + ")?\n\n" +
            "This action cannot be undone. This aircraft cannot be used in flights if deleted.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.removeAircraft(selectedAircraft.getAircraftId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Aircraft deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAircraft(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to delete aircraft.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting aircraft: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Show dialog for changing aircraft status.
     */
    private void showChangeStatusDialog() {
        int selectedRow = aircraftTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an aircraft to change status.");
            return;
        }
        
        Aircraft selectedAircraft = aircraftList.get(selectedRow);
        
        // Create status selection dialog
        String[] statusOptions = {"ACTIVE", "INACTIVE", "MAINTENANCE"};
        String currentStatus = selectedAircraft.getStatus() != null ? selectedAircraft.getStatus() : "ACTIVE";
        
        String newStatus = (String) JOptionPane.showInputDialog(
            this,
            "Select new status for aircraft: " + selectedAircraft.getModel() + "\n" +
            "Current status: " + currentStatus,
            "Change Aircraft Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statusOptions,
            currentStatus
        );
        
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            try {
                selectedAircraft.setStatus(newStatus);
                boolean success = adminService.updateAircraft(selectedAircraft);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Aircraft status updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAircraft(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to update aircraft status.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error updating aircraft status: " + e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                ErrorDialog.show(this, e.getMessage());
            }
        }
    }
    
    /**
     * Dialog for adding/editing aircraft.
     */
    private class AircraftFormDialog extends JDialog {
        private Aircraft aircraft;
        private boolean confirmed = false;
        
        private JTextField modelField;
        private JTextField manufacturerField;
        private JTextField totalSeatsField;
        private JTextField seatConfigurationField;
        private JComboBox<String> statusComboBox;
        
        public AircraftFormDialog(JFrame parent, String title, Aircraft aircraftToEdit) {
            super(parent, title, true); // true = modal dialog
            this.aircraft = aircraftToEdit;
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
            
            // Model
            formPanel.add(new JLabel("Model:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            modelField = new JTextField(20);
            if (aircraft != null) {
                modelField.setText(aircraft.getModel());
            }
            formPanel.add(modelField, gbc);
            
            // Manufacturer
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Manufacturer:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            manufacturerField = new JTextField(20);
            if (aircraft != null) {
                manufacturerField.setText(aircraft.getManufacturer());
            }
            formPanel.add(manufacturerField, gbc);
            
            // Total Seats
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Total Seats:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            totalSeatsField = new JTextField(20);
            if (aircraft != null) {
                totalSeatsField.setText(String.valueOf(aircraft.getTotalSeats()));
            }
            formPanel.add(totalSeatsField, gbc);
            
            // Seat Configuration
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Seat Configuration (e.g., 3-3):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            seatConfigurationField = new JTextField(20);
            if (aircraft != null) {
                seatConfigurationField.setText(aircraft.getSeatConfiguration());
            }
            formPanel.add(seatConfigurationField, gbc);
            
            // Status
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Status:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "MAINTENANCE"});
            if (aircraft != null && aircraft.getStatus() != null) {
                statusComboBox.setSelectedItem(aircraft.getStatus());
            } else {
                statusComboBox.setSelectedItem("ACTIVE");
            }
            formPanel.add(statusComboBox, gbc);
            
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
            // Validate model
            String model = modelField.getText().trim();
            if (model.isEmpty()) {
                ErrorDialog.show(this, "Model is required.");
                return false;
            }
            
            // Validate manufacturer
            String manufacturer = manufacturerField.getText().trim();
            if (manufacturer.isEmpty()) {
                ErrorDialog.show(this, "Manufacturer is required.");
                return false;
            }
            
            // Validate total seats
            int totalSeats;
            try {
                totalSeats = Integer.parseInt(totalSeatsField.getText().trim());
                if (totalSeats <= 0) {
                    ErrorDialog.show(this, "Total seats must be greater than 0.");
                    return false;
                }
            } catch (NumberFormatException e) {
                ErrorDialog.show(this, "Total seats must be a valid number.");
                return false;
            }
            
            // Seat configuration is optional, but validate format if provided
            String seatConfiguration = seatConfigurationField.getText().trim();
            
            // Get status
            String status = (String) statusComboBox.getSelectedItem();
            
            try {
                Aircraft aircraftToSave;
                if (aircraft == null) {
                    // Creating new aircraft
                    aircraftToSave = new Aircraft();
                    aircraftToSave.setModel(model);
                    aircraftToSave.setManufacturer(manufacturer);
                    aircraftToSave.setTotalSeats(totalSeats);
                    aircraftToSave.setSeatConfiguration(seatConfiguration.isEmpty() ? null : seatConfiguration);
                    aircraftToSave.setStatus(status);
                    
                    Aircraft created = adminService.addAircraft(aircraftToSave);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this,
                            "Aircraft added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to add aircraft.");
                        return false;
                    }
                } else {
                    // Updating existing aircraft
                    aircraft.setModel(model);
                    aircraft.setManufacturer(manufacturer);
                    aircraft.setTotalSeats(totalSeats);
                    aircraft.setSeatConfiguration(seatConfiguration.isEmpty() ? null : seatConfiguration);
                    aircraft.setStatus(status);
                    
                    boolean success = adminService.updateAircraft(aircraft);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "Aircraft updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to update aircraft.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error saving aircraft: " + e.getMessage(), e);
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
