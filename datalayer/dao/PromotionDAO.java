package datalayer.dao;

import businesslogic.entities.Promotion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for Promotion entity.
 * Handles CRUD operations for promotions in the database.
 */
public interface PromotionDAO {
    
    /**
     * Save a new promotion to the database.
     * @param promotion The promotion to save
     * @return The saved promotion with generated ID
     * @throws SQLException if database operation fails
     */
    Promotion save(Promotion promotion) throws SQLException;
    
    /**
     * Find a promotion by its ID.
     * @param id The promotion ID
     * @return The promotion, or null if not found
     * @throws SQLException if database operation fails
     */
    Promotion findById(Integer id) throws SQLException;
    
    /**
     * Get all promotions from the database.
     * @return List of all promotions
     * @throws SQLException if database operation fails
     */
    List<Promotion> findAll() throws SQLException;
    
    /**
     * Get all currently active promotions (valid today).
     * @return List of active promotions
     * @throws SQLException if database operation fails
     */
    List<Promotion> findActivePromotions() throws SQLException;
    
    /**
     * Get all promotions valid on a specific date.
     * @param date The date to check
     * @return List of promotions valid on that date
     * @throws SQLException if database operation fails
     */
    List<Promotion> findPromotionsByDate(LocalDate date) throws SQLException;
    
    /**
     * Get promotions for the current month (for monthly news).
     * @return List of promotions valid this month
     * @throws SQLException if database operation fails
     */
    List<Promotion> findPromotionsForCurrentMonth() throws SQLException;
    
    /**
     * Update an existing promotion.
     * @param promotion The promotion with updated values
     * @return true if update successful
     * @throws SQLException if database operation fails
     */
    boolean update(Promotion promotion) throws SQLException;
    
    /**
     * Delete a promotion by ID.
     * @param id The promotion ID to delete
     * @return true if deletion successful
     * @throws SQLException if database operation fails
     */
    boolean delete(Integer id) throws SQLException;
}