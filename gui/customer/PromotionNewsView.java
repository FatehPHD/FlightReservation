package gui.customer;

import businesslogic.entities.Customer;
import businesslogic.entities.Promotion;
import businesslogic.entities.User;
import businesslogic.services.PromotionService;
import gui.common.ViewManager;
import gui.common.ErrorDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * View for displaying monthly promotion news to customers.
 * Shows current promotions and allows subscription management.
 */
public class PromotionNewsView extends JPanel {

    private final ViewManager viewManager;
    private final PromotionService promotionService;
    private JPanel promotionsPanel;
    private JCheckBox subscriptionCheckbox;
    private JLabel statusLabel;

    public PromotionNewsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.promotionService = viewManager.getServiceManager().getPromotionService();
        
        initializeUI();
        loadPromotions();
        loadSubscriptionStatus();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Main content - scrollable promotions list
        promotionsPanel = new JPanel();
        promotionsPanel.setLayout(new BoxLayout(promotionsPanel, BoxLayout.Y_AXIS));
        promotionsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(promotionsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with subscription toggle and back button
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(46, 139, 87));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        JLabel titleLabel = new JLabel("Monthly Promotions - " + monthYear);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.WEST);

        // Info about first day notification
        JLabel infoLabel = new JLabel("<html><i>Promotions are sent on the first day of each month</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(200, 255, 200));
        panel.add(infoLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Left side - subscription toggle
        JPanel subscriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subscriptionPanel.setBackground(new Color(245, 245, 245));
        
        subscriptionCheckbox = new JCheckBox("Subscribe to monthly promotion news");
        subscriptionCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        subscriptionCheckbox.setBackground(new Color(245, 245, 245));
        subscriptionCheckbox.addActionListener(e -> updateSubscription());
        subscriptionPanel.add(subscriptionCheckbox);
        
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        subscriptionPanel.add(statusLabel);
        
        panel.add(subscriptionPanel, BorderLayout.WEST);

        // Right side - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> loadPromotions());
        buttonPanel.add(refreshBtn);

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> viewManager.showView("CUSTOMER_DASHBOARD",
            new CustomerDashboardView(viewManager)));
        buttonPanel.add(backBtn);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private void loadPromotions() {
        promotionsPanel.removeAll();

        try {
            List<Promotion> promotions = promotionService.getMonthlyPromotions();

            if (promotions.isEmpty()) {
                promotionsPanel.add(createNoPromotionsPanel());
            } else {
                // Welcome message for logged-in customer
                User currentUser = viewManager.getCurrentUser();
                if (currentUser instanceof Customer) {
                    Customer customer = (Customer) currentUser;
                    promotionsPanel.add(createWelcomePanel(customer));
                }

                // Add each promotion card
                for (Promotion promo : promotions) {
                    promotionsPanel.add(createPromotionCard(promo));
                    promotionsPanel.add(Box.createVerticalStrut(10));
                }
            }

        } catch (SQLException e) {
            ErrorDialog.show(this, "Failed to load promotions: " + e.getMessage());
            promotionsPanel.add(createErrorPanel());
        }

        promotionsPanel.revalidate();
        promotionsPanel.repaint();
    }

    private JPanel createWelcomePanel(Customer customer) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 255, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 230, 200)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel welcomeLabel = new JLabel("Hello " + customer.getFirstName() + 
            "! Here are this month's exclusive offers for you:");
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(0, 100, 0));
        panel.add(welcomeLabel);

        return panel;
    }

    private JPanel createPromotionCard(Promotion promo) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(new Color(249, 249, 249));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Left side - promotion details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(249, 249, 249));

        JLabel titleLabel = new JLabel(promo.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(titleLabel);

        detailsPanel.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel("<html>" + promo.getDescription() + "</html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        descLabel.setForeground(new Color(102, 102, 102));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(descLabel);

        detailsPanel.add(Box.createVerticalStrut(5));

        String validityText = "Valid: " + 
            promo.getValidFrom().format(DateTimeFormatter.ofPattern("MMM d")) + " - " +
            promo.getValidTo().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        JLabel validityLabel = new JLabel(validityText);
        validityLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        validityLabel.setForeground(new Color(136, 136, 136));
        validityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(validityLabel);

        card.add(detailsPanel, BorderLayout.CENTER);

        // Right side - discount badge
        JPanel discountPanel = new JPanel(new GridBagLayout());
        discountPanel.setBackground(new Color(231, 76, 60));
        discountPanel.setPreferredSize(new Dimension(100, 80));
        discountPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel discountLabel = new JLabel(String.format("%.0f%%", promo.getDiscountPercent()));
        discountLabel.setFont(new Font("Arial", Font.BOLD, 28));
        discountLabel.setForeground(Color.WHITE);
        discountPanel.add(discountLabel);

        JLabel offLabel = new JLabel("OFF");
        offLabel.setFont(new Font("Arial", Font.BOLD, 14));
        offLabel.setForeground(Color.WHITE);

        JPanel badgeContainer = new JPanel();
        badgeContainer.setLayout(new BoxLayout(badgeContainer, BoxLayout.Y_AXIS));
        badgeContainer.setBackground(new Color(231, 76, 60));
        discountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        offLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgeContainer.add(discountLabel);
        badgeContainer.add(offLabel);

        discountPanel.removeAll();
        discountPanel.add(badgeContainer);

        card.add(discountPanel, BorderLayout.EAST);

        // Active indicator
        LocalDate today = LocalDate.now();
        boolean isActive = !today.isBefore(promo.getValidFrom()) && !today.isAfter(promo.getValidTo());
        if (isActive) {
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(46, 139, 87), 2),
                new EmptyBorder(15, 15, 15, 15)
            ));
        }

        return card;
    }

    private JPanel createNoPromotionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("<html><center>" +
            "<h2 style='color: #888;'>No Promotions This Month</h2>" +
            "<p style='color: #aaa;'>Check back on the first of next month for new offers!</p>" +
            "</center></html>");
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label);

        return panel;
    }

    private JPanel createErrorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 240, 240));

        JLabel label = new JLabel("<html><center>" +
            "<h3 style='color: #c0392b;'>Unable to Load Promotions</h3>" +
            "<p style='color: #888;'>Please try again later.</p>" +
            "</center></html>");
        panel.add(label);

        return panel;
    }

    private void loadSubscriptionStatus() {
        User currentUser = viewManager.getCurrentUser();
        if (!(currentUser instanceof Customer)) {
            subscriptionCheckbox.setEnabled(false);
            statusLabel.setText("(Login required)");
            return;
        }

        try {
            boolean isSubscribed = promotionService.isCustomerSubscribed(currentUser.getUserId());
            subscriptionCheckbox.setSelected(isSubscribed);
            updateStatusLabel(isSubscribed);
        } catch (SQLException e) {
            subscriptionCheckbox.setEnabled(false);
            statusLabel.setText("(Unable to load status)");
        }
    }

    private void updateSubscription() {
        User currentUser = viewManager.getCurrentUser();
        if (!(currentUser instanceof Customer)) {
            ErrorDialog.show(this, "Please login to manage subscriptions.");
            return;
        }

        boolean subscribe = subscriptionCheckbox.isSelected();
        
        try {
            boolean success = promotionService.updateSubscription(currentUser.getUserId(), subscribe);
            if (success) {
                updateStatusLabel(subscribe);
                String message = subscribe ? 
                    "You're now subscribed to monthly promotion news!" :
                    "You've been unsubscribed from monthly promotion news.";
                JOptionPane.showMessageDialog(this, message, "Subscription Updated", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                subscriptionCheckbox.setSelected(!subscribe);
                ErrorDialog.show(this, "Failed to update subscription.");
            }
        } catch (SQLException e) {
            subscriptionCheckbox.setSelected(!subscribe);
            ErrorDialog.show(this, "Error updating subscription: " + e.getMessage());
        }
    }

    private void updateStatusLabel(boolean subscribed) {
        if (subscribed) {
            statusLabel.setText("You'll receive news on the 1st of each month");
            statusLabel.setForeground(new Color(46, 139, 87));
        } else {
            statusLabel.setText("You won't receive monthly news");
            statusLabel.setForeground(Color.GRAY);
        }
    }
}