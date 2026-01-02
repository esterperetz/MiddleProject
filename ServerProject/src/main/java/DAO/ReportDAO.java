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

    // Fetches customer arrival counts grouped by hour (0-23) for the last month.
    public Map<Integer, Integer> getArrivalsByHour() throws SQLException {
        String sql = "SELECT HOUR(arrival_time) as hour_of_day, COUNT(*) as count " +
                     "FROM `order` " +
                     "WHERE arrival_time IS NOT NULL " +
                     "AND order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                     "GROUP BY HOUR(arrival_time)";
        return executeHourQuery(sql);
    }

    // Fetches customer departure counts grouped by hour (0-23) for the last month.
    public Map<Integer, Integer> getDeparturesByHour() throws SQLException {
        String sql = "SELECT HOUR(leaving_time) as hour_of_day, COUNT(*) as count " +
                     "FROM `order` " +
                     "WHERE leaving_time IS NOT NULL " +
                     "AND order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                     "GROUP BY HOUR(leaving_time)";
        return executeHourQuery(sql);
    }

    // Fetches cancelled/late order counts grouped by hour (0-23) for the last month.
    public Map<Integer, Integer> getCancellationsByHour() throws SQLException {
        String sql = "SELECT HOUR(order_date) as hour_of_day, COUNT(*) as count " +
                     "FROM `order` " +
                     "WHERE order_status = 'CANCELLED' " +
                     "AND order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                     "GROUP BY HOUR(order_date)";
        return executeHourQuery(sql);
    }

    // Fetches total order counts per day for the last month (formatted dd/MM).
    public Map<String, Integer> getDailyOrderCount() throws SQLException {
        String sql = "SELECT DATE_FORMAT(order_date, '%d/%m') as day, COUNT(*) as count " +
                     "FROM `order` " +
                     "WHERE order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH) " +
                     "GROUP BY DATE(order_date) " +
                     "ORDER BY DATE(order_date) ASC";
        
        Map<String, Integer> result = new TreeMap<>(); // TreeMap ensures dates remain sorted
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getString("day"), rs.getInt("count"));
            }
        }
        return result;
    }

    // Helper method to execute hourly grouping queries and fill missing hours with 0.
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