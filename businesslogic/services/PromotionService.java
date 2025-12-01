package businesslogic.services;

import businesslogic.entities.Customer;
import businesslogic.entities.Promotion;
import datalayer.dao.PromotionDAO;
import datalayer.dao.UserDAO;
import datalayer.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service class for managing promotions and monthly promotion news.
 * Handles promotion CRUD, customer subscriptions, news generation, and scheduling.
 */
public class PromotionService {

    private final PromotionDAO promotionDAO;
    private final UserDAO userDAO;

    // Scheduler fields
    private ScheduledExecutorService scheduler;
    private boolean isSchedulerRunning;
    private LocalDate lastRunDate;

    // SQL for customer subscription management
    private static final String CHECK_SUBSCRIPTION_SQL = 
        "SELECT subscribed_to_promotions FROM users WHERE user_id = ?";
    
    private static final String UPDATE_SUBSCRIPTION_SQL = 
        "UPDATE users SET subscribed_to_promotions = ? WHERE user_id = ?";
    
    private static final String GET_SUBSCRIBED_CUSTOMERS_SQL = 
        "SELECT user_id FROM users WHERE role = 'CUSTOMER' AND subscribed_to_promotions = TRUE";

    public PromotionService(PromotionDAO promotionDAO, UserDAO userDAO) {
        this.promotionDAO = promotionDAO;
        this.userDAO = userDAO;
        this.isSchedulerRunning = false;
        this.lastRunDate = null;
    }

    // ========================================================================
    // PROMOTION CRUD OPERATIONS
    // ========================================================================

    /**
     * Get all active promotions (valid today).
     */
    public List<Promotion> getActivePromotions() throws SQLException {
        return promotionDAO.findActivePromotions();
    }

    /**
     * Get all promotions for the current month.
     * Used for monthly promotion news feature.
     */
    public List<Promotion> getMonthlyPromotions() throws SQLException {
        return promotionDAO.findPromotionsForCurrentMonth();
    }

    /**
     * Get all promotions.
     */
    public List<Promotion> getAllPromotions() throws SQLException {
        return promotionDAO.findAll();
    }

    /**
     * Get a promotion by ID.
     */
    public Promotion getPromotionById(int promotionId) throws SQLException {
        return promotionDAO.findById(promotionId);
    }

    /**
     * Create a new promotion.
     */
    public Promotion createPromotion(String title, String description, 
                                     double discountPercent,
                                     LocalDate validFrom, LocalDate validTo) throws SQLException {
        
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Promotion title is required.");
        }
        if (discountPercent <= 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount must be between 0 and 100 percent.");
        }
        if (validFrom == null || validTo == null) {
            throw new IllegalArgumentException("Valid date range is required.");
        }
        if (validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        Promotion promotion = new Promotion(
            0,
            title.trim(),
            description != null ? description.trim() : "",
            discountPercent,
            validFrom,
            validTo,
            null
        );

        return promotionDAO.save(promotion);
    }

    /**
     * Update an existing promotion.
     */
    public boolean updatePromotion(Promotion promotion) throws SQLException {
        if (promotion == null || promotion.getPromotionId() <= 0) {
            throw new IllegalArgumentException("Valid promotion is required.");
        }
        return promotionDAO.update(promotion);
    }

    /**
     * Delete a promotion.
     */
    public boolean deletePromotion(int promotionId) throws SQLException {
        return promotionDAO.delete(promotionId);
    }

    // ========================================================================
    // SUBSCRIPTION MANAGEMENT
    // ========================================================================

    /**
     * Check if a customer is subscribed to promotion news.
     */
    public boolean isCustomerSubscribed(int customerId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(CHECK_SUBSCRIPTION_SQL)) {
            stmt.setInt(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("subscribed_to_promotions");
                }
            }
        }
        
        return false;
    }

    /**
     * Update a customer's subscription preference.
     */
    public boolean updateSubscription(int customerId, boolean subscribed) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SUBSCRIPTION_SQL)) {
            stmt.setBoolean(1, subscribed);
            stmt.setInt(2, customerId);
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all customer IDs subscribed to promotion news.
     */
    public List<Integer> getSubscribedCustomerIds() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Integer> customerIds = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(GET_SUBSCRIBED_CUSTOMERS_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                customerIds.add(rs.getInt("user_id"));
            }
        }
        
        return customerIds;
    }

    // ========================================================================
    // NEWS GENERATION
    // ========================================================================

    /**
     * Check if today is the first day of the month (for triggering monthly news).
     */
    public boolean isFirstDayOfMonth() {
        return LocalDate.now().getDayOfMonth() == 1;
    }

    /**
     * Generate monthly promotion news content for a customer.
     * Returns formatted HTML content for display.
     */
    public String generateMonthlyNewsContent(Customer customer) throws SQLException {
        List<Promotion> promotions = getMonthlyPromotions();
        
        if (promotions.isEmpty()) {
            return null;
        }

        StringBuilder html = new StringBuilder();
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        
        html.append("<html><body style='font-family: Arial, sans-serif; padding: 20px;'>");
        html.append("<h1 style='color: #2E8B57;'>🎉 Monthly Promotions - ").append(monthYear).append("</h1>");
        
        if (customer != null) {
            html.append("<p>Hello <strong>").append(customer.getFirstName()).append("</strong>,</p>");
        }
        
        html.append("<p>Check out our exclusive offers this month:</p>");
        html.append("<hr style='border: 1px solid #ddd;'/>");
        
        for (Promotion promo : promotions) {
            html.append("<div style='background: #f9f9f9; padding: 15px; margin: 10px 0; border-radius: 8px; border-left: 4px solid #2E8B57;'>");
            html.append("<h3 style='color: #333; margin: 0 0 10px 0;'>").append(promo.getTitle()).append("</h3>");
            html.append("<p style='color: #666; margin: 5px 0;'>").append(promo.getDescription()).append("</p>");
            html.append("<p style='margin: 5px 0;'><strong style='color: #e74c3c; font-size: 18px;'>")
                .append(String.format("%.0f", promo.getDiscountPercent()))
                .append("% OFF</strong></p>");
            html.append("<p style='color: #888; font-size: 12px; margin: 5px 0;'>Valid: ")
                .append(promo.getValidFrom().format(DateTimeFormatter.ofPattern("MMM d")))
                .append(" - ")
                .append(promo.getValidTo().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                .append("</p>");
            html.append("</div>");
        }
        
        html.append("<hr style='border: 1px solid #ddd;'/>");
        html.append("<p style='color: #888; font-size: 12px;'>You're receiving this because you're subscribed to monthly promotion news. ");
        html.append("You can unsubscribe in your account settings.</p>");
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * Generate plain text version of monthly news (for email simulation).
     */
    public String generateMonthlyNewsPlainText(Customer customer) throws SQLException {
        List<Promotion> promotions = getMonthlyPromotions();
        
        if (promotions.isEmpty()) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        
        text.append("=== Monthly Promotions - ").append(monthYear).append(" ===\n\n");
        
        if (customer != null) {
            text.append("Hello ").append(customer.getFirstName()).append(",\n\n");
        }
        
        text.append("Check out our exclusive offers this month:\n\n");
        text.append("-------------------------------------------\n\n");
        
        for (Promotion promo : promotions) {
            text.append("★ ").append(promo.getTitle()).append("\n");
            text.append("   ").append(promo.getDescription()).append("\n");
            text.append("   DISCOUNT: ").append(String.format("%.0f", promo.getDiscountPercent())).append("% OFF\n");
            text.append("   Valid: ")
                .append(promo.getValidFrom().format(DateTimeFormatter.ofPattern("MMM d")))
                .append(" - ")
                .append(promo.getValidTo().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                .append("\n\n");
        }
        
        text.append("-------------------------------------------\n");
        text.append("You're receiving this because you're subscribed to monthly promotion news.\n");
        
        return text.toString();
    }

    /**
     * Simulate sending monthly promotion news to all subscribed customers.
     * In a real system, this would integrate with an email service.
     * Returns the count of customers notified.
     */
    public int sendMonthlyPromotionNews() throws SQLException {
        if (!isFirstDayOfMonth()) {
            return 0;
        }

        return forceSendPromotionNews();
    }

    // ========================================================================
    // SCHEDULER METHODS
    // ========================================================================

    /**
     * Start the scheduler. It will check daily if it's the first of the month.
     */
    public void startScheduler() {
        if (isSchedulerRunning) {
            System.out.println("[PromotionService] Scheduler already running.");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        isSchedulerRunning = true;
        System.out.println("[PromotionService] Starting monthly promotion news scheduler...");

        // Calculate delay until next midnight
        long delayUntilMidnight = calculateDelayUntilMidnight();
        
        // Schedule to run daily at midnight
        scheduler.scheduleAtFixedRate(
            this::checkAndSendPromotionNews,
            delayUntilMidnight,
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.MILLISECONDS
        );

        // Also check immediately on startup (in case it's the first of the month)
        scheduler.execute(this::checkAndSendPromotionNews);
        
        System.out.println("[PromotionService] Scheduler started. Will check daily at midnight.");
    }

    /**
     * Stop the scheduler.
     */
    public void stopScheduler() {
        if (!isSchedulerRunning || scheduler == null) {
            return;
        }

        isSchedulerRunning = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[PromotionService] Scheduler stopped.");
    }

    /**
     * Check if it's the first of the month and send promotion news if so.
     */
    private void checkAndSendPromotionNews() {
        LocalDate today = LocalDate.now();
        
        // Prevent running multiple times on the same day
        if (lastRunDate != null && lastRunDate.equals(today)) {
            return;
        }

        if (today.getDayOfMonth() == 1) {
            System.out.println("[PromotionService] First day of month detected. Sending promotion news...");
            
            try {
                int notifiedCount = forceSendPromotionNews();
                lastRunDate = today;
                
                System.out.println("[PromotionService] Monthly promotion news sent to " + 
                    notifiedCount + " customers.");
                
            } catch (SQLException e) {
                System.err.println("[PromotionService] Error sending promotion news: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[PromotionService] Not the first of the month. Day: " + today.getDayOfMonth());
        }
    }

    /**
     * Calculate milliseconds until next midnight.
     */
    private long calculateDelayUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.MILLIS.between(now, nextMidnight);
    }

    /**
     * Force send promotion news (for testing or manual trigger).
     * Bypasses the first-of-month check.
     */
    public int forceSendPromotionNews() throws SQLException {
        System.out.println("[PromotionService] Sending promotion news...");
        
        List<Promotion> promotions = getMonthlyPromotions();
        if (promotions.isEmpty()) {
            System.out.println("[PromotionService] No promotions to send.");
            return 0;
        }

        List<Integer> subscribedCustomerIds = getSubscribedCustomerIds();
        int notifiedCount = 0;

        for (Integer customerId : subscribedCustomerIds) {
            try {
                Customer customer = (Customer) userDAO.findById(customerId);
                if (customer != null && customer.getEmail() != null) {
                    // In production, this would send an actual email
                    // For now, we just simulate the notification
                    String newsContent = generateMonthlyNewsPlainText(customer);
                    if (newsContent != null) {
                        // Log the simulated email send
                        System.out.println("[SIMULATED EMAIL] To: " + customer.getEmail());
                        System.out.println("[SIMULATED EMAIL] Subject: Monthly Promotions - " + 
                            LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                        notifiedCount++;
                    }
                }
            } catch (Exception e) {
                System.err.println("[PromotionService] Error notifying customer " + customerId + ": " + e.getMessage());
            }
        }

        lastRunDate = LocalDate.now();
        System.out.println("[PromotionService] Send completed. Notified: " + notifiedCount);
        
        return notifiedCount;
    }

    /**
     * Check if the scheduler is running.
     */
    public boolean isSchedulerRunning() {
        return isSchedulerRunning;
    }

    /**
     * Get the last date when promotion news was sent.
     */
    public LocalDate getLastRunDate() {
        return lastRunDate;
    }
}