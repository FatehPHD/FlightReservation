package datalayer.database;

import java.util.Map;
import java.util.StringJoiner;

public class QueryBuilder {

    public static String createInsertQuery(String table, Map<String, Object> columns) {
        StringJoiner cols = new StringJoiner(", ");
        StringJoiner vals = new StringJoiner(", ");

        for (String col : columns.keySet()) {
            cols.add(col);
            vals.add("?");
        }

        return "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
    }

    public static String createUpdateQuery(String table, Map<String, Object> columns, String idColumn) {
        StringJoiner setList = new StringJoiner(", ");

        for (String col : columns.keySet()) {
            if (!col.equals(idColumn)) {
                setList.add(col + " = ?");
            }
        }

        return "UPDATE " + table + " SET " + setList + " WHERE " + idColumn + " = ?";
    }

    public static String createSelectByIdQuery(String table, String idColumn) {
        return "SELECT * FROM " + table + " WHERE " + idColumn + " = ?";
    }

    public static String createDeleteByIdQuery(String table, String idColumn) {
        return "DELETE FROM " + table + " WHERE " + idColumn + " = ?";
    }
}
