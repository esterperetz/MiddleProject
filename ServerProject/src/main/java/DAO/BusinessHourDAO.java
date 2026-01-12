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
	
	public OpeningHours getHoursForDate(java.util.Date date) throws SQLException {
	    
	    java.sql.Date sqlDate = new java.sql.Date(date.getTime());

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

	    String sql = "SELECT * FROM opening_hours " +
	                 "WHERE special_date = ? " + 
	                 "OR (day_of_week = ? AND special_date IS NULL) " +
	                 "ORDER BY special_date DESC LIMIT 1";

	    Connection con = DBConnection.getInstance().getConnection();
	    try (PreparedStatement stmt = con.prepareStatement(sql)) {
	        
	        stmt.setDate(1, sqlDate); 
	        stmt.setInt(2, dayOfWeek);       

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return mapResultSetToOpeningHours(rs);
	            }
	        }
	    } finally {
	        DBConnection.getInstance().releaseConnection(con);
	    }
	    return null;
	}
	
	public boolean deleteOpeningHours(java.sql.Date dateToRemove) {
	
	    String query = "DELETE FROM opening_hours WHERE special_date = ?";
	    
	    Connection con = null;
	    try {
	        con = DBConnection.getInstance().getConnection();
	        try (PreparedStatement stmt = con.prepareStatement(query)) {
	            
	            stmt.setDate(1, dateToRemove);
	            
	            int affectedRows = stmt.executeUpdate();
	            return affectedRows > 0;
	        }
	    } catch (SQLException e) {
	        System.err.println("Delete Error: " + e.getMessage());
	        return false;
	    } finally {
	        DBConnection.getInstance().releaseConnection(con);
	    }
	}
	
    /**
     * Retrieves all configured hours for the management view.
     */
    public List<OpeningHours> getAllOpeningHours() throws SQLException {
        String sql = "SELECT * FROM opening_hours ORDER BY day_of_week ASC, special_date ASC";
        Connection con = DBConnection.getInstance().getConnection(); //takes connection from pool
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            List<OpeningHours> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToOpeningHours(rs));
            }
            return list;
        } finally {
            DBConnection.getInstance().releaseConnection(con); //returns connection to pool
        }
    }
    
    

    /**
     * Saves or updates opening hours record.
     */
    public boolean saveOrUpdate(OpeningHours oh) throws SQLException {
        Connection con = DBConnection.getInstance().getConnection();
        PreparedStatement stmt = null;

        try {
      
            if (oh.getId() == 0) {
                String checkQuery;
                boolean isSpecial = (oh.getSpecialDate() != null);

                if (isSpecial) {
                	
                    checkQuery = "SELECT id FROM opening_hours WHERE special_date = ?";
                } else {
                	
                    checkQuery = "SELECT id FROM opening_hours WHERE day_of_week = ? AND special_date IS NULL";
                }

                try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
                    if (isSpecial) {
                        checkStmt.setDate(1, new java.sql.Date(oh.getSpecialDate().getTime()));
                    } else {
                        checkStmt.setInt(1, oh.getDayOfWeek());
                    }

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                        	
                            int existingId = rs.getInt("id");
                            oh.setId(existingId);
                            System.out.println("Duplicate found (ID: " + existingId + "). Switching to UPDATE mode.");
                        }
                    }
                }
            }

          
            String sql;
            if (oh.getId() == 0) {
            	
                sql = "INSERT INTO opening_hours (day_of_week, special_date, open_time, close_time, is_closed) VALUES (?, ?, ?, ?, ?)";
            } else {
            	
                sql = "UPDATE opening_hours SET day_of_week = ?, special_date = ?, open_time = ?, close_time = ?, is_closed = ? WHERE id = ?";
            }

            stmt = con.prepareStatement(sql);

            stmt.setInt(1, oh.getDayOfWeek());

            if (oh.getSpecialDate() != null) {
                stmt.setDate(2, new java.sql.Date(oh.getSpecialDate().getTime()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }

            stmt.setTime(3, oh.getOpenTime());
            stmt.setTime(4, oh.getCloseTime());
            stmt.setBoolean(5, oh.isClosed());

            if (oh.getId() != 0) {
                stmt.setInt(6, oh.getId());
            }

            return stmt.executeUpdate() > 0;

        } finally {
            if (stmt != null) stmt.close();
            DBConnection.getInstance().releaseConnection(con);
        }
    }
    
    
    

    private OpeningHours mapResultSetToOpeningHours(ResultSet rs) throws SQLException {
    	System.out.println(new OpeningHours(
                rs.getInt("id"),
                (Integer) rs.getObject("day_of_week"),
                rs.getDate("special_date"),
                rs.getTime("open_time"),
                rs.getTime("close_time"),
                rs.getBoolean("is_closed")).toString());
        return new OpeningHours(
                rs.getInt("id"),
                (Integer) rs.getObject("day_of_week"),
                rs.getDate("special_date"),
                rs.getTime("open_time"),
                rs.getTime("close_time"),
                rs.getBoolean("is_closed"));
    }
}