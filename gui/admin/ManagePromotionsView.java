package gui.admin;

import businesslogic.entities.Promotion;
import businesslogic.services.PromotionService;
import gui.common.ViewManager;
import gui.common.ErrorDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Admin view for managing promotions.
 * Allows creating, editing, and deleting promotions for the monthly news feature.
 */
public class ManagePromotionsView extends JPanel {

    private final ViewManager viewManager;
    private final PromotionService promotionService;
    private JTable promotionsTable;
    private DefaultTableModel tableModel;

    public ManagePromotionsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.promotionService = viewManager.getServiceManager().getPromotionService();
        
        initComponents();
        loadPromotions();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel title = new JLabel("Manage Promotions");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
        add(titlePanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Title", "Description", "Discount %", "Valid From", "Valid To", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        promotionsTable = new JTable(tableModel);
        promotionsTable.setRowHeight(25);
        promotionsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        promotionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        promotionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        promotionsTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        promotionsTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // Title
        promotionsTable.getColumnModel().getColumn(2).setPreferredWidth(250);  // Description
        promotionsTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // Discount
        promotionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Valid From
        promotionsTable.getColumnModel().getColumn(5).setPreferredWidth(100);  // Valid To
        promotionsTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Status

        JScrollPane scrollPane = new JScrollPane(promotionsTable);
        scrollPane.setPreferredSize(new Dimension(900, 400));
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("Add Promotion");
        addBtn.setPreferredSize(new Dimension(130, 35));
        addBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        addBtn.addActionListener(e -> showPromotionDialog(null));

        JButton editBtn = new JButton("Edit Promotion");
        editBtn.setPreferredSize(new Dimension(130, 35));
        editBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        editBtn.addActionListener(e -> editSelectedPromotion());

        JButton deleteBtn = new JButton("Delete Promotion");
        deleteBtn.setPreferredSize(new Dimension(130, 35));
        deleteBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteBtn.addActionListener(e -> deleteSelectedPromotion());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(130, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadPromotions());

        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(130, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> viewManager.showView("ADMIN_DASHBOARD", 
            new AdminDashboardView(viewManager)));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPromotions() {
        tableModel.setRowCount(0);

        try {
            List<Promotion> promotions = promotionService.getAllPromotions();
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Promotion promo : promotions) {
                String status;
                if (today.isBefore(promo.getValidFrom())) {
                    status = "Upcoming";
                } else if (today.isAfter(promo.getValidTo())) {
                    status = "Expired";
                } else {
                    status = "Active";
                }

                tableModel.addRow(new Object[]{
                    promo.getPromotionId(),
                    promo.getTitle(),
                    promo.getDescription(),
                    String.format("%.0f%%", promo.getDiscountPercent()),
                    promo.getValidFrom().format(formatter),
                    promo.getValidTo().format(formatter),
                    status
                });
            }

        } catch (SQLException e) {
            ErrorDialog.show(this, "Failed to load promotions: " + e.getMessage());
        }
    }

    private void showPromotionDialog(Promotion existing) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add Promotion" : "Edit Promotion",
            true
        );
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField titleField = new JTextField(20);
        if (existing != null) titleField.setText(existing.getTitle());
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        if (existing != null) descArea.setText(existing.getDescription());
        formPanel.add(new JScrollPane(descArea), gbc);

        // Discount
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Discount (%):"), gbc);
        gbc.gridx = 1;
        JSpinner discountSpinner = new JSpinner(new SpinnerNumberModel(
            existing != null ? existing.getDiscountPercent() : 10.0, 
            1.0, 100.0, 1.0
        ));
        formPanel.add(discountSpinner, gbc);

        // Valid From
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Valid From (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField validFromField = new JTextField(10);
        if (existing != null) {
            validFromField.setText(existing.getValidFrom().toString());
        } else {
            validFromField.setText(LocalDate.now().toString());
        }
        formPanel.add(validFromField, gbc);

        // Valid To
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Valid To (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        JTextField validToField = new JTextField(10);
        if (existing != null) {
            validToField.setText(existing.getValidTo().toString());
        } else {
            validToField.setText(LocalDate.now().plusMonths(1).toString());
        }
        formPanel.add(validToField, gbc);

        dialog.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String description = descArea.getText().trim();
                double discount = (Double) discountSpinner.getValue();
                LocalDate validFrom = LocalDate.parse(validFromField.getText().trim());
                LocalDate validTo = LocalDate.parse(validToField.getText().trim());

                if (title.isEmpty()) {
                    ErrorDialog.show(dialog, "Title is required.");
                    return;
                }

                if (validTo.isBefore(validFrom)) {
                    ErrorDialog.show(dialog, "End date cannot be before start date.");
                    return;
                }

                if (existing == null) {
                    promotionService.createPromotion(title, description, discount, validFrom, validTo);
                    JOptionPane.showMessageDialog(dialog, "Promotion created successfully!");
                } else {
                    Promotion updated = new Promotion(
                        existing.getPromotionId(),
                        title, description, discount,
                        validFrom, validTo, null
                    );
                    promotionService.updatePromotion(updated);
                    JOptionPane.showMessageDialog(dialog, "Promotion updated successfully!");
                }

                dialog.dispose();
                loadPromotions();

            } catch (DateTimeParseException ex) {
                ErrorDialog.show(dialog, "Invalid date format. Use YYYY-MM-DD.");
            } catch (Exception ex) {
                ErrorDialog.show(dialog, "Error: " + ex.getMessage());
            }
        });
        btnPanel.add(saveBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedPromotion() {
        int row = promotionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a promotion to edit.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int promotionId = (Integer) tableModel.getValueAt(row, 0);
        
        try {
            Promotion promo = promotionService.getPromotionById(promotionId);
            if (promo != null) {
                showPromotionDialog(promo);
            }
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading promotion: " + e.getMessage());
        }
    }

    private void deleteSelectedPromotion() {
        int row = promotionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a promotion to delete.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int promotionId = (Integer) tableModel.getValueAt(row, 0);
        String title = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the promotion:\n\"" + title + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = promotionService.deletePromotion(promotionId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Promotion deleted successfully.");
                    loadPromotions();
                } else {
                    ErrorDialog.show(this, "Failed to delete promotion.");
                }
            } catch (SQLException e) {
                ErrorDialog.show(this, "Error deleting promotion: " + e.getMessage());
            }
        }
    }
}