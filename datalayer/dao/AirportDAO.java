package datalayer.dao;

import businesslogic.entities.Airport;

public interface AirportDAO extends BaseDAO<Airport, String> {

    // Optional convenience alias if you prefer the old name:
    default Airport findByCode(String code) throws java.sql.SQLException {
        return findById(code);
    }

    default boolean deleteByCode(String code) throws java.sql.SQLException {
        return delete(code);
    }
}
