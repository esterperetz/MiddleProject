package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import DBConnection.DBConnection;
import entities.OpeningHours;

public class BusinessHourDAO {

    /**
     * Finds the relevant operating hours for a specific date.
     * Priority: 1. Special Date, 2. Regular Day of Week.
     */
    public OpeningHours getHoursForDate(java.util.Date requestedDate) throws SQLException {
        java.sql.Date sqlDate = new java.sql.Date(requestedDate.getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(requestedDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        String sql = "SELECT * FROM opening_hours " +
                     "WHERE special_date = ? OR (day_of_week = ? AND special_date IS NULL) " +
                     "ORDER BY special_date DESC LIMIT 1";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, sqlDate);
            stmt.setInt(2, dayOfWeek);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOpeningHours(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all configured hours for the management view.
     */
    public List<OpeningHours> getAllOpeningHours() throws SQLException {
        String sql = "SELECT * FROM opening_hours ORDER BY day_of_week ASC, special_date ASC";
        try (Connection con = DBConnection.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<OpeningHours> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToOpeningHours(rs));
            }
            return list;
        }
    }

    /**
     * Saves or updates opening hours record.
     */
    public boolean saveOrUpdate(OpeningHours oh) throws SQLException {
        String sql;
        if (oh.getId() == 0) {
            sql = "INSERT INTO opening_hours (day_of_week, special_date, open_time, close_time, is_closed) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE opening_hours SET day_of_week = ?, special_date = ?, open_time = ?, close_time = ?, is_closed = ? WHERE id = ?";
        }

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setObject(1, oh.getDayOfWeek());
            stmt.setDate(2, oh.getSpecialDate() != null ? new java.sql.Date(oh.getSpecialDate().getTime()) : null);
            stmt.setTime(3, oh.getOpenTime());
            stmt.setTime(4, oh.getCloseTime());
            stmt.setBoolean(5, oh.isClosed());
            if (oh.getId() != 0) stmt.setInt(6, oh.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    private OpeningHours mapResultSetToOpeningHours(ResultSet rs) throws SQLException {
        return new OpeningHours(
            rs.getInt("id"),
            (Integer) rs.getObject("day_of_week"),
            rs.getDate("special_date"),
            rs.getTime("open_time"),
            rs.getTime("close_time"),
            rs.getBoolean("is_closed")
        );
    }
}