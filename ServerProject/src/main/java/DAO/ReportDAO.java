package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import DBConnection.DBConnection;

public class ReportDAO {

    // Fetches customer arrival counts grouped by hour (0-23).
    public Map<Integer, Integer> getArrivalsByHour(Integer month, Integer year) throws SQLException {
        String sql = "SELECT HOUR(arrival_time) as hour_of_day, COUNT(*) as count " +
                "FROM `order` " +
                "WHERE arrival_time IS NOT NULL " +
                buildDateFilter(month, year, "arrival_time") +
                " GROUP BY HOUR(arrival_time)";
        return executeHourQuery(sql);
    }

    // Fetches customer departure counts grouped by hour (0-23).
    public Map<Integer, Integer> getDeparturesByHour(Integer month, Integer year) throws SQLException {
        String sql = "SELECT HOUR(leaving_time) as hour_of_day, COUNT(*) as count " +
                "FROM `order` " +
                "WHERE leaving_time IS NOT NULL " +
                buildDateFilter(month, year, "leaving_time") +
                " GROUP BY HOUR(leaving_time)";
        return executeHourQuery(sql);
    }

    // Fetches cancelled/late order counts grouped by hour (0-23).
    public Map<Integer, Integer> getCancellationsByHour(Integer month, Integer year) throws SQLException {
        String sql = "SELECT HOUR(order_date) as hour_of_day, COUNT(*) as count " +
                "FROM `order` " +
                "WHERE order_status = 'CANCELLED' " +
                buildDateFilter(month, year, "order_date") +
                " GROUP BY HOUR(order_date)";
        return executeHourQuery(sql);
    }

    // Fetches total order counts per day.
    public Map<String, Integer> getDailyOrderCount(Integer month, Integer year) throws SQLException {
        String sql = "SELECT DATE_FORMAT(order_date, '%d/%m') as day, COUNT(*) as count " +
                "FROM `order` " +
                "WHERE 1=1 " +
                buildDateFilter(month, year, "order_date") +
                " GROUP BY DATE_FORMAT(order_date, '%d/%m') " +
                "ORDER BY MAX(order_date) ASC";

        Map<String, Integer> result = new TreeMap<>();
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("count"));
            }
        }
        return result;
    }

    // Helper to build SQL date filter
    private String buildDateFilter(Integer month, Integer year, String dateColumn) {
        if (month != null && year != null) {
            return " AND MONTH(" + dateColumn + ") = " + month + " AND YEAR(" + dateColumn + ") = " + year;
        } else {
            // Default: Last 30 days
            return " AND " + dateColumn + " >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
        }
    }

    // Helper method to execute hourly grouping queries and fill missing hours with
    // 0.
    private Map<Integer, Integer> executeHourQuery(String sql) throws SQLException {
        Map<Integer, Integer> result = new HashMap<>();

        // Initialize all hours (0-23) to 0 to avoid gaps in the graph
        for (int i = 0; i < 24; i++)
            result.put(i, 0);

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getInt("hour_of_day"), rs.getInt("count"));
            }
        }
        return result;
    }
}
