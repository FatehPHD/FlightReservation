package datalayer.impl;

import businesslogic.entities.Promotion;
import businesslogic.entities.Route;
import datalayer.dao.PromotionDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PromotionDAO for MySQL database operations.
 */
public class PromotionDAOImpl implements PromotionDAO {

    private static final String INSERT_SQL = 
        "INSERT INTO promotions (title, description, discount_percent, valid_from, valid_to) " +
        "VALUES (?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM promotions WHERE promotion_id = ?";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM promotions ORDER BY valid_from DESC";
    
    private static final String SELECT_ACTIVE_SQL = 
        "SELECT * FROM promotions WHERE valid_from <= CURDATE() AND valid_to >= CURDATE() " +
        "ORDER BY discount_percent DESC";
    
    private static final String SELECT_BY_DATE_SQL = 
        "SELECT * FROM promotions WHERE valid_from <= ? AND valid_to >= ? " +
        "ORDER BY discount_percent DESC";
    
    private static final String SELECT_CURRENT_MONTH_SQL = 
        "SELECT * FROM promotions WHERE " +
        "(YEAR(valid_from) = YEAR(CURDATE()) AND MONTH(valid_from) = MONTH(CURDATE())) OR " +
        "(YEAR(valid_to) = YEAR(CURDATE()) AND MONTH(valid_to) = MONTH(CURDATE())) OR " +
        "(valid_from <= CURDATE() AND valid_to >= CURDATE()) " +
        "ORDER BY discount_percent DESC";
    
    private static final String UPDATE_SQL = 
        "UPDATE promotions SET title = ?, description = ?, discount_percent = ?, " +
        "valid_from = ?, valid_to = ? WHERE promotion_id = ?";
    
    private static final String DELETE_SQL = 
        "DELETE FROM promotions WHERE promotion_id = ?";

    @Override
    public Promotion save(Promotion promotion) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, promotion.getTitle());
            stmt.setString(2, promotion.getDescription());
            stmt.setDouble(3, promotion.getDiscountPercent());
            stmt.setDate(4, Date.valueOf(promotion.getValidFrom()));
            stmt.setDate(5, Date.valueOf(promotion.getValidTo()));

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Creating promotion failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int promotionId = keys.getInt(1);
                    return new Promotion(
                        promotionId,
                        promotion.getTitle(),
                        promotion.getDescription(),
                        promotion.getDiscountPercent(),
                        promotion.getValidFrom(),
                        promotion.getValidTo(),
                        promotion.getApplicableRoutes()
                    );
                }
            }
        }

        return promotion;
    }

    @Override
    public Promotion findById(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Promotion> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Promotion> promotions = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                promotions.add(mapRow(rs));
            }
        }

        return promotions;
    }

    @Override
    public List<Promotion> findActivePromotions() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Promotion> promotions = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ACTIVE_SQL)) {

            while (rs.next()) {
                promotions.add(mapRow(rs));
            }
        }

        return promotions;
    }

    @Override
    public List<Promotion> findPromotionsByDate(LocalDate date) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Promotion> promotions = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DATE_SQL)) {
            stmt.setDate(1, Date.valueOf(date));
            stmt.setDate(2, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    promotions.add(mapRow(rs));
                }
            }
        }

        return promotions;
    }

    @Override
    public List<Promotion> findPromotionsForCurrentMonth() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Promotion> promotions = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_CURRENT_MONTH_SQL)) {

            while (rs.next()) {
                promotions.add(mapRow(rs));
            }
        }

        return promotions;
    }

    @Override
    public boolean update(Promotion promotion) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, promotion.getTitle());
            stmt.setString(2, promotion.getDescription());
            stmt.setDouble(3, promotion.getDiscountPercent());
            stmt.setDate(4, Date.valueOf(promotion.getValidFrom()));
            stmt.setDate(5, Date.valueOf(promotion.getValidTo()));
            stmt.setInt(6, promotion.getPromotionId());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Map a ResultSet row to a Promotion object.
     */
    private Promotion mapRow(ResultSet rs) throws SQLException {
        return new Promotion(
            rs.getInt("promotion_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDouble("discount_percent"),
            rs.getDate("valid_from").toLocalDate(),
            rs.getDate("valid_to").toLocalDate(),
            null  // Routes loaded separately if needed
        );
    }
}