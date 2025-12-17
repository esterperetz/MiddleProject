package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import Entities.Table;

public class TableDAO {

    public List<Table> getAllTables() throws SQLException {
        String sql = "SELECT * FROM tables";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                Table t = new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"));
                list.add(t);
            }
            return list;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public boolean addTable(Table t) throws SQLException {
        String sql = "INSERT INTO tables (table_number, number_of_seats) VALUES (?, ?)";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, t.getTableNumber());
            stmt.setInt(2, t.getNumberOfSeats());
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public boolean updateTable(Table t) throws SQLException {
        String sql = "UPDATE tables SET number_of_seats = ? WHERE table_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, t.getNumberOfSeats());
            stmt.setInt(2, t.getTableNumber());
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public boolean deleteTable(int tableNumber) throws SQLException {
        String sql = "DELETE FROM tables WHERE table_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tableNumber);
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    public Table getTable(int tableNumber) throws SQLException {
        String sql = "SELECT * FROM tables WHERE table_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, tableNumber);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new Table(rs.getInt("table_number"), rs.getInt("number_of_seats"));
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
}