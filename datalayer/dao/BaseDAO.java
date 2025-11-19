// File: datalayer/dao/BaseDAO.java
package datalayer.dao;

import java.sql.SQLException;
import java.util.List;

public interface BaseDAO<T, ID> {

    T save(T entity) throws SQLException;

    T findById(ID id) throws SQLException;

    List<T> findAll() throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(ID id) throws SQLException;
}
