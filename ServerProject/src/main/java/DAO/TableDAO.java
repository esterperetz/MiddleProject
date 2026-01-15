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
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                rs = stmt.executeQuery();

                List<Table> list = new ArrayList<>();
                while (rs.next()) {
                    Table t = new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"),
                            rs.getBoolean("is_occupied"));
                    list.add(t);
                }
                return list;
            }
        } catch (Exception e) {
            System.err.println("Error in get all Tables");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    public boolean addTable(Table t) throws SQLException {
        String query = "INSERT INTO tables (table_number, number_of_seats) VALUES (?, ?)";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, t.getTableNumber());
                stmt.setInt(2, t.getNumberOfSeats());
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error in add Table");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }
    public Table getSmallestCapacityTable() throws SQLException{
        String query = "SELECT * FROM tables ORDER BY number_of_seats ASC LIMIT 1";
        Connection con = null;
        Table smallestTable = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    int tableNum = rs.getInt("table_number");
                    int seats = rs.getInt("number_of_seats");
                    
                    smallestTable = new Table(tableNum, seats,false);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting smallest table: " + e.getMessage());
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        
        return smallestTable;
    }

    public boolean updateTable(Table t) throws SQLException {
        String query = "UPDATE tables SET number_of_seats = ? WHERE table_number = ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, t.getNumberOfSeats());
                stmt.setInt(2, t.getTableNumber());
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error in update Table");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }

    public boolean deleteTable(int tableNumber) throws SQLException {
        String query = "DELETE FROM tables WHERE table_number = ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, tableNumber);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error in delete Table");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }

    public Table getTable(int tableNumber) throws SQLException {
        String query = "SELECT * FROM tables WHERE table_number = ?";
        ResultSet rs = null;
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, tableNumber);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"),
                            rs.getBoolean("is_occupied"));
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error in update Table");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    public int countSuitableTables(int numberOfGuests) throws SQLException {
        // SQL query to find tables large enough for the request
        String query = "SELECT COUNT(*) FROM tables WHERE number_of_seats >= ?";

        ResultSet rs = null;
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, numberOfGuests);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0; // Return 0 if no results found
            }
        } catch (Exception e) {
            System.err.println("Error in count Suitable Tables");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return 0;
    }

    public Integer findAvailableTable(int guests) throws SQLException {
        // Greedy logic: Find the smallest table (ASC) that fits the guests and is free
        String query = "SELECT table_number FROM tables " +
                "WHERE is_occupied = 0 AND number_of_seats >= ? " +
                "ORDER BY number_of_seats ASC LIMIT 1";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, guests);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("table_number");
                    }
                    return null;
                } catch (Exception e) {
                    System.err.println("No suitable table found right now");
                }
            }
        } catch (Exception e) {
            System.err.println("Error in get Table");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;

    }

    /**
     * Updates the physical occupancy status of a table[cite: 51].
     */
    public boolean updateTableStatus(int tableNumber, int isOccupied) throws SQLException {
        String sql = "UPDATE tables SET is_occupied = ? WHERE table_number = ?";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, isOccupied);
                stmt.setInt(2, tableNumber);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error in update Table status");
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }
 // הוסף בסוף TableDAO.java
    
    public List<Integer> getAllTableCapacities() {
        List<Integer> capacities = new ArrayList<>();
        String sql = "SELECT DISTINCT number_of_seats FROM tables ORDER BY number_of_seats ASC"; 
        
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    capacities.add(rs.getInt("number_of_seats"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting table capacities: " + e.getMessage());
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return capacities;
    }
    //if working delete above function
    public List<Integer> getAllTableCapacities2() {
        List<Integer> capacities = new ArrayList<>();
        String sql = "SELECT number_of_seats FROM tables  WHERE is_occupied = 0 ORDER BY number_of_seats ASC"; 
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    capacities.add(rs.getInt("number_of_seats"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting table capacities: " + e.getMessage());
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return capacities;
    }
    
    
    public int countTotalPhysicalTables(int minSeats) throws SQLException {
        String query = "SELECT COUNT(*) FROM tables WHERE number_of_seats >= ?";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, minSeats);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }
}