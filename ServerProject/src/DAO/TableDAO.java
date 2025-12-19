package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import Entities.Table;

public class TableDAO {

    public List<Table> getAllTables() throws SQLException {
        String sql = "SELECT * FROM tables";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                Table t = new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"));
                t.setOccupied(rs.getBoolean("is_occupied"));
                list.add(t);
            }
            return list;
        }
    }

    public boolean addTable(Table t) throws SQLException {
        String sql = "INSERT INTO tables (table_number, number_of_seats, is_occupied) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, t.getTableNumber());
            stmt.setInt(2, t.getNumberOfSeats());
            stmt.setBoolean(3, false);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Finds an available table of sufficient size. */
    public Integer findAvailableTable(int numberOfGuests) throws SQLException {
        String sql = "SELECT table_number FROM tables WHERE number_of_seats >= ? AND is_occupied = FALSE LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, numberOfGuests);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("table_number");
            }
        }
        return null;
    }

    /** Updates occupancy status in DB. */
    public boolean updateTableStatus(int tableNumber, boolean isOccupied) throws SQLException {
        String sql = "UPDATE tables SET is_occupied = ? WHERE table_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setBoolean(1, isOccupied);
            stmt.setInt(2, tableNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countSuitableTables(int numberOfGuests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tables WHERE number_of_seats >= ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, numberOfGuests);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public Table getTable(int tableNumber) throws SQLException {
        String sql = "SELECT * FROM tables WHERE table_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, tableNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Table t = new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"));
                    t.setOccupied(rs.getBoolean("is_occupied"));
                    return t;
                }
            }
        }
        return null;
    }

    public boolean updateTable(Table t) throws SQLException {
        String sql = "UPDATE tables SET number_of_seats = ?, is_occupied = ? WHERE table_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, t.getNumberOfSeats());
            stmt.setBoolean(2, t.isOccupied());
            stmt.setInt(3, t.getTableNumber());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteTable(int tableNumber) throws SQLException {
        String sql = "DELETE FROM tables WHERE table_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, tableNumber);
            return stmt.executeUpdate() > 0;
        }
    }
    public synchronized Integer findAndOccupyTable(int numberOfGuests) throws SQLException {
        String findSql = "SELECT table_number FROM tables WHERE number_of_seats >= ? AND is_occupied = FALSE LIMIT 1";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(findSql)) {
            stmt.setInt(1, numberOfGuests);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tableNum = rs.getInt("table_number");
                    updateTableStatus(tableNum, true); // Mark as occupied immediately
                    return tableNum;
                }
            }
        }
        return null;
    }
}