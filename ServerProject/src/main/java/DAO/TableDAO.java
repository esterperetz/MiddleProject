package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import entities.Table;

public class TableDAO {

    public List<Table> getAllTables() throws SQLException {
        String query = "SELECT * FROM tables";
        ResultSet rs = null;

        try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
            rs = stmt.executeQuery();

            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                Table t = new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"),rs.getBoolean("is_occupied"));
                list.add(t);
            }
            return list;
        } catch(Exception e) {
        	System.err.println("Error in get all Tables");
        }
        return null;
    }

    public boolean addTable(Table t) throws SQLException {
        String query = "INSERT INTO tables (table_number, number_of_seats) VALUES (?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, t.getTableNumber());
            stmt.setInt(2, t.getNumberOfSeats());
            return stmt.executeUpdate() > 0;
        } catch(Exception e) {
        	System.err.println("Error in add Table");
        }
        return false;
    }

    public boolean updateTable(Table t) throws SQLException {
        String query = "UPDATE tables SET number_of_seats = ? WHERE table_number = ?";
        

      try (Connection con = DBConnection.getInstance().getConnection();
    				PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, t.getNumberOfSeats());
            stmt.setInt(2, t.getTableNumber());
            return stmt.executeUpdate() > 0;
        }catch(Exception e) {
        	System.err.println("Error in update Table");
        }
      return false;
    }

    public boolean deleteTable(int tableNumber) throws SQLException {
        String query = "DELETE FROM tables WHERE table_number = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, tableNumber);
            return stmt.executeUpdate() > 0;
        }catch(Exception e) {
        	System.err.println("Error in delete Table");
        }
        return false;
    }
    
    public Table getTable(int tableNumber) throws SQLException {
        String query = "SELECT * FROM tables WHERE table_number = ?";
        ResultSet rs = null;

        try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, tableNumber);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"),rs.getBoolean("is_occupied"));
            }
            return null;
        }catch(Exception e) {
        	System.err.println("Error in update Table");
        }
        return null;
    }
    public int countSuitableTables(int numberOfGuests) throws SQLException {
        // SQL query to find tables large enough for the request
        String query = "SELECT COUNT(*) FROM tables WHERE number_of_seats >= ?";
        
        ResultSet rs = null;

		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, numberOfGuests);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0; // Return 0 if no results found
        } catch(Exception e) {
        	System.err.println("Error in count Suitable Tables");
        }
//            finally {
//            if (rs != null) rs.close();
//            if (stmt != null) stmt.close();
//        }
		return 0;
    }
    public Integer findAvailableTable(int guests) throws SQLException {
        // Greedy logic: Find the smallest table (ASC) that fits the guests and is free
        String query = "SELECT table_number FROM tables " +
                     "WHERE is_occupied = 0 AND number_of_seats >= ? " +
                     "ORDER BY number_of_seats ASC LIMIT 1";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, guests);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("table_number");
                }
                return null; // No suitable table found right now
            }catch(Exception e) {
            	System.err.println("No suitable table found right now");
            }
        }catch(Exception e) {
        	System.err.println("Error in get Table");
        }
        return null;
       
    }

    /**
     * Updates the physical occupancy status of a table[cite: 51].
     */
    public boolean updateTableStatus(int tableNumber, boolean isOccupied) throws SQLException {
        String sql = "UPDATE tables SET is_occupied = ? WHERE table_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, tableNumber);
            return stmt.executeUpdate() > 0;
        }catch(Exception e) {
        	System.err.println("Error in update Table status");
        }
        return false;
    }
}