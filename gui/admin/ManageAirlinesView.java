package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.Airline;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Manage airlines (CRUD operations).
 * Add, edit, delete, and view all airlines.
 * All database operations are performed through AdminService.
 */
public class ManageAirlinesView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTable airlineTable;
    private DefaultTableModel tableModel;
    private List<Airline> airlineList;
    
    public ManageAirlinesView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        loadAirlines();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Airlines");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "ID", "Name", "Code", "Country"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only, use buttons for editing
            }
        };
        
        airlineTable = new JTable(tableModel);
        airlineTable.setRowHeight(25);
        airlineTable.setFont(new Font("Arial", Font.PLAIN, 12));
        airlineTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        airlineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(airlineTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Airline");
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddAirlineDialog());
        
        JButton editBtn = new JButton("Edit Airline");
        editBtn.setPreferredSize(new Dimension(120, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditAirlineDialog());
        
        JButton deleteBtn = new JButton("Delete Airline");
        deleteBtn.setPreferredSize(new Dimension(120, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedAirline());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadAirlines());
        
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
     * Load all airlines using AdminService.getAllAirlines().
     */
    private void loadAirlines() {
        try {
            airlineList = adminService.getAllAirlines();
            updateTable();
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading airlines: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update the table with current airlines.
     */
    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Airline airline : airlineList) {
            Object[] row = {
                airline.getAirlineId(),
                airline.getName() != null ? airline.getName() : "N/A",
                airline.getCode() != null ? airline.getCode() : "N/A",
                airline.getCountry() != null ? airline.getCountry() : "N/A"
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Show dialog for adding a new airline.
     */
    private void showAddAirlineDialog() {
        AirlineFormDialog dialog = new AirlineFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Add Airline", null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAirlines(); // Refresh table
        }
    }
    
    /**
     * Show dialog for editing an existing airline.
     */
    private void showEditAirlineDialog() {
        int selectedRow = airlineTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an airline to edit.");
            return;
        }
        
        Airline selectedAirline = airlineList.get(selectedRow);
        
        AirlineFormDialog dialog = new AirlineFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this),
            "Edit Airline", selectedAirline);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadAirlines(); // Refresh table
        }
    }
    
    /**
     * Delete the selected airline.
     */
    private void deleteSelectedAirline() {
        int selectedRow = airlineTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select an airline to delete.");
            return;
        }
        
        Airline selectedAirline = airlineList.get(selectedRow);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete airline " + selectedAirline.getName() + 
            " (Code: " + selectedAirline.getCode() + ")?\n\n" +
            "This action cannot be undone. This airline cannot be used in flights if deleted.",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = adminService.removeAirline(selectedAirline.getAirlineId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Airline deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAirlines(); // Refresh table
                } else {
                    ErrorDialog.show(this, "Failed to delete airline.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting airline: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Dialog for adding/editing airlines.
     */
    private class AirlineFormDialog extends JDialog {
        private Airline airline;
        private boolean confirmed = false;
        
        private JTextField nameField;
        private JTextField codeField;
        private JTextField countryField;
        
        public AirlineFormDialog(JFrame parent, String title, Airline airlineToEdit) {
            super(parent, title, true); // true = modal dialog
            this.airline = airlineToEdit;
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
            
            // Name
            formPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            nameField = new JTextField(20);
            if (airline != null) {
                nameField.setText(airline.getName());
            }
            formPanel.add(nameField, gbc);
            
            // Code
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Code (e.g., AC):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            codeField = new JTextField(20);
            if (airline != null) {
                codeField.setText(airline.getCode());
                codeField.setEditable(false); // Can't change code when editing (used in flight numbers)
            } else {
                codeField.setToolTipText("2-letter airline code (e.g., AC for Air Canada). " +
                    "This code will be used in flight numbers.");
            }
            formPanel.add(codeField, gbc);
            
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
            if (airline != null) {
                countryField.setText(airline.getCountry());
            }
            formPanel.add(countryField, gbc);
            
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
            // Validate name
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                ErrorDialog.show(this, "Name is required.");
                return false;
            }
            
            // Validate code
            String code = codeField.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                ErrorDialog.show(this, "Code is required.");
                return false;
            }
            if (code.length() < 2 || code.length() > 10) {
                ErrorDialog.show(this, "Code must be between 2 and 10 characters.");
                return false;
            }
            
            // Validate country
            String country = countryField.getText().trim();
            if (country.isEmpty()) {
                ErrorDialog.show(this, "Country is required.");
                return false;
            }
            
            try {
                Airline airlineToSave;
                if (airline == null) {
                    // Creating new airline
                    airlineToSave = new Airline();
                    airlineToSave.setName(name);
                    airlineToSave.setCode(code);
                    airlineToSave.setCountry(country);
                    
                    Airline created = adminService.addAirline(airlineToSave);
                    if (created != null) {
                        JOptionPane.showMessageDialog(this,
                            "Airline added successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to add airline.");
                        return false;
                    }
                } else {
                    // Updating existing airline
                    airline.setName(name);
                    // Code is not editable when editing
                    airline.setCountry(country);
                    
                    boolean success = adminService.updateAirline(airline);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                            "Airline updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        return true;
                    } else {
                        ErrorDialog.show(this, "Failed to update airline.");
                        return false;
                    }
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error saving airline: " + e.getMessage(), e);
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
