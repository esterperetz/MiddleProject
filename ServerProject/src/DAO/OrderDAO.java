package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import Entities.Order;

public class OrderDAO {

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM bistro.order";
        try (Connection con = DBConnection.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToOrder(rs));
        }
        return list;
    }

    public Order getOrder(int id) throws SQLException {
        String sql = "SELECT * FROM bistro.order WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToOrder(rs);
            }
        }
        return null;
    }

    public Order getByConfirmationCode(int code) throws SQLException {
        String sql = "SELECT * FROM bistro.order WHERE confirmation_code = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToOrder(rs);
            }
        }
        return null;
    }

    public boolean createOrder(Order o) throws SQLException {
        String sql = "INSERT INTO bistro.order (order_date, number_of_guests, confirmation_code, subscriber_id, " +
                     "date_of_placing_order, client_name, client_email, client_phone, order_status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, new Timestamp(o.getOrder_date().getTime()));
            stmt.setInt(2, o.getNumber_of_guests());
            stmt.setInt(3, o.getConfirmation_code());
            if (o.getSubscriber_id() != null) stmt.setInt(4, o.getSubscriber_id()); else stmt.setNull(4, Types.INTEGER);
            stmt.setTimestamp(5, new Timestamp(o.getDate_of_placing_order().getTime()));
            stmt.setString(6, o.getClient_name());
            stmt.setString(7, o.getClient_email());
            stmt.setString(8, o.getClient_Phone());
            stmt.setString(9, o.getOrder_status().name());
            
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) o.setOrder_number(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateOrder(Order o) throws SQLException {
        String sql = "UPDATE bistro.order SET order_status = ?, table_number = ?, arrival_time = ?, leave_time = ?, total_price = ? WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, o.getOrder_status().name());
            if (o.getTable_number() != null) stmt.setInt(2, o.getTable_number()); else stmt.setNull(2, Types.INTEGER);
            stmt.setTimestamp(3, o.getArrivalTime() != null ? new Timestamp(o.getArrivalTime().getTime()) : null);
            stmt.setTimestamp(4, o.getLeaveTime() != null ? new Timestamp(o.getLeaveTime().getTime()) : null);
            stmt.setDouble(5, o.getTotal_price());
            stmt.setInt(6, o.getOrder_number());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM bistro.order WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countActiveOrdersInTimeRange(java.util.Date date, int guests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bistro.order " +
                     "WHERE order_date = ? AND order_status != 'CANCELLED' " +
                     "AND number_of_guests >= ?"; 
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(date.getTime()));
            stmt.setInt(2, guests); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order(
            rs.getInt("order_number"), rs.getTimestamp("order_date"), rs.getInt("number_of_guests"),
            rs.getInt("confirmation_code"), (Integer) rs.getObject("subscriber_id"),
            rs.getTimestamp("date_of_placing_order"), rs.getString("client_name"),
            rs.getString("client_email"), rs.getString("client_phone"),
            rs.getTimestamp("arrival_time"), rs.getDouble("total_price"),
            Order.OrderStatus.valueOf(rs.getString("order_status"))
        );
        o.setTable_number((Integer) rs.getObject("table_number"));
        o.setLeaveTime(rs.getTimestamp("leave_time"));
        return o;
    }
}