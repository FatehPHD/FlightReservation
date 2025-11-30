package gui.agent;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.Customer;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.services.CustomerService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Manage customers (Add, Update, Delete).
 * Allows agents to create, modify, and remove customer accounts.
 * All database operations are performed through CustomerService.
 */
public class ManageCustomersView extends JPanel {
    
    private ViewManager viewManager;
    private CustomerService customerService;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private List<Customer> customers;
    
    public ManageCustomersView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.customerService = viewManager.getCustomerService();
        initComponents();
        loadCustomers();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Customers");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);
        
        // Table panel
        String[] columnNames = {
            "Customer ID", "Username", "Name", "Email", "Phone", "Membership"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(25);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton addBtn = new JButton("Add Customer");
        addBtn.setPreferredSize(new Dimension(150, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showAddCustomerDialog());
        
        JButton editBtn = new JButton("Edit Customer");
        editBtn.setPreferredSize(new Dimension(150, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> showEditCustomerDialog());
        
        JButton deleteBtn = new JButton("Delete Customer");
        deleteBtn.setPreferredSize(new Dimension(150, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedCustomer());
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadCustomers());
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(150, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("AGENT_DASHBOARD", 
                new AgentDashboardView(viewManager));
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadCustomers() {
        try {
            customers = customerService.getAllCustomers();
            tableModel.setRowCount(0);
            
            for (Customer customer : customers) {
                String name = customer.getFirstName() + " " + 
                             (customer.getLastName() != null ? customer.getLastName() : "");
                
                tableModel.addRow(new Object[]{
                    customer.getUserId(),
                    customer.getUsername(),
                    name,
                    customer.getEmail(),
                    customer.getPhone() != null ? customer.getPhone() : "N/A",
                    customer.getMembershipStatus() != null ? 
                        customer.getMembershipStatus().name() : "REGULAR"
                });
            }
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading customers: " + e.getMessage(), e);
        }
    }
    
    private void showAddCustomerDialog() {
        CustomerFormDialog dialog = new CustomerFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Add Customer", 
            null
        );
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadCustomers();
        }
    }
    
    private void showEditCustomerDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a customer to edit.");
            return;
        }
        
        int customerId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Customer customer = customers.stream()
            .filter(c -> c.getUserId() == customerId)
            .findFirst()
            .orElse(null);
        
        if (customer == null) {
            ErrorDialog.show(this, "Customer not found.");
            return;
        }
        
        CustomerFormDialog dialog = new CustomerFormDialog(
            (JFrame) SwingUtilities.getWindowAncestor(this), 
            "Edit Customer", 
            customer
        );
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadCustomers();
        }
    }
    
    private void deleteSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            ErrorDialog.show(this, "Please select a customer to delete.");
            return;
        }
        
        int customerId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete customer: " + username + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = customerService.deleteCustomer(customerId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
                    loadCustomers();
                } else {
                    ErrorDialog.show(this, "Failed to delete customer.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting customer: " + e.getMessage(), e);
            }
        }
    }
    
    private class CustomerFormDialog extends JDialog {
        private Customer customer;
        private boolean saved = false;
        
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JTextField emailField;
        private JTextField firstNameField;
        private JTextField lastNameField;
        private JTextField phoneField;
        private JTextField addressField;
        private JTextField dateOfBirthField;
        private JComboBox<MembershipStatus> membershipComboBox;
        
        public CustomerFormDialog(JFrame parent, String title, Customer customerToEdit) {
            super(parent, title, true);
            this.customer = customerToEdit;
            initDialog();
        }
        
        private void initDialog() {
            setLayout(new BorderLayout());
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;
            
            // Username
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            usernameField = new JTextField(20);
            if (customer != null) {
                usernameField.setText(customer.getUsername());
                usernameField.setEditable(false);
            }
            formPanel.add(usernameField, gbc);
            
            // Password
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            passwordField = new JPasswordField(20);
            formPanel.add(passwordField, gbc);
            
            // Email
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Email:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            emailField = new JTextField(20);
            if (customer != null) {
                emailField.setText(customer.getEmail());
            }
            formPanel.add(emailField, gbc);
            
            // First Name
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("First Name:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            firstNameField = new JTextField(20);
            if (customer != null) {
                firstNameField.setText(customer.getFirstName());
            }
            formPanel.add(firstNameField, gbc);
            
            // Last Name
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Last Name:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            lastNameField = new JTextField(20);
            if (customer != null && customer.getLastName() != null) {
                lastNameField.setText(customer.getLastName());
            }
            formPanel.add(lastNameField, gbc);
            
            // Phone
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Phone:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            phoneField = new JTextField(20);
            if (customer != null && customer.getPhone() != null) {
                phoneField.setText(customer.getPhone());
            }
            formPanel.add(phoneField, gbc);
            
            // Address
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Address:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            addressField = new JTextField(20);
            if (customer != null && customer.getAddress() != null) {
                addressField.setText(customer.getAddress());
            }
            formPanel.add(addressField, gbc);
            
            // Date of Birth
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            dateOfBirthField = new JTextField(20);
            if (customer != null && customer.getDateOfBirth() != null) {
                dateOfBirthField.setText(customer.getDateOfBirth().toString());
            }
            formPanel.add(dateOfBirthField, gbc);
            
            // Membership Status
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.fill = GridBagConstraints.NONE;
            gbc.ipadx = 0;
            formPanel.add(new JLabel("Membership Status:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.ipadx = 200;
            membershipComboBox = new JComboBox<>(MembershipStatus.values());
            if (customer != null && customer.getMembershipStatus() != null) {
                membershipComboBox.setSelectedItem(customer.getMembershipStatus());
            }
            formPanel.add(membershipComboBox, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel();
            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                if (validateAndSave()) {
                    saved = true;
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
        }
        
        private boolean validateAndSave() {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String dateOfBirthStr = dateOfBirthField.getText().trim();
            
            if (username.isEmpty()) {
                ErrorDialog.show(this, "Username is required.");
                return false;
            }
            if (customer == null && password.isEmpty()) {
                ErrorDialog.show(this, "Password is required for new customers.");
                return false;
            }
            if (email.isEmpty()) {
                ErrorDialog.show(this, "Email is required.");
                return false;
            }
            if (firstName.isEmpty()) {
                ErrorDialog.show(this, "First name is required.");
                return false;
            }
            
            LocalDate dateOfBirth = null;
            if (!dateOfBirthStr.isEmpty()) {
                try {
                    dateOfBirth = LocalDate.parse(dateOfBirthStr);
                } catch (DateTimeParseException e) {
                    ErrorDialog.show(this, "Invalid date format. Use YYYY-MM-DD.");
                    return false;
                }
            }
            
            try {
                if (customer == null) {
                    // Create new customer
                    Customer newCustomer = customerService.createCustomer(
                        username, password, email, firstName, lastName,
                        phone.isEmpty() ? null : phone,
                        address.isEmpty() ? null : address,
                        dateOfBirth
                    );
                    
                    // Update membership status if different from default
                    MembershipStatus selectedStatus = (MembershipStatus) membershipComboBox.getSelectedItem();
                    if (selectedStatus != MembershipStatus.REGULAR) {
                        newCustomer.setMembershipStatus(selectedStatus);
                        customerService.updateCustomer(newCustomer);
                    }
                    
                    JOptionPane.showMessageDialog(this, "Customer created successfully.");
                } else {
                    // Update existing customer
                    customer.setEmail(email);
                    customer.setFirstName(firstName);
                    customer.setLastName(lastName.isEmpty() ? null : lastName);
                    customer.setPhone(phone.isEmpty() ? null : phone);
                    customer.setAddress(address.isEmpty() ? null : address);
                    customer.setDateOfBirth(dateOfBirth);
                    customer.setMembershipStatus((MembershipStatus) membershipComboBox.getSelectedItem());
                    
                    if (!password.isEmpty()) {
                        customer.setPassword(password);
                    }
                    
                    boolean updated = customerService.updateCustomer(customer);
                    if (updated) {
                        JOptionPane.showMessageDialog(this, "Customer updated successfully.");
                    } else {
                        ErrorDialog.show(this, "Failed to update customer.");
                        return false;
                    }
                }
                return true;
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error saving customer: " + e.getMessage(), e);
                return false;
            } catch (IllegalStateException e) {
                ErrorDialog.show(this, e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                ErrorDialog.show(this, e.getMessage());
                return false;
            }
        }
        
        public boolean isSaved() {
            return saved;
        }
    }
}

