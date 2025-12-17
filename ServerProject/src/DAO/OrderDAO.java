package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import Entities.Order;
import Entities.Order.OrderStatus;

public class OrderDAO {

    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM `order`";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Order> list = new ArrayList<>();
            while (rs.next()) {
                int subIdTemp = rs.getInt("subscriber_id");
                Integer subId = rs.wasNull() ? null : subIdTemp;
                
                String statusStr = rs.getString("status");
                OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

                Order o = new Order(
                        rs.getInt("order_number"),
                        rs.getTimestamp("order_date"), 
                        rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"),
                        subId,
                        rs.getTimestamp("date_of_placing_order"),
                        rs.getString("identification_details"),
                        rs.getString("full_name"),
                        rs.getDouble("total_price"),
                        status
                );
                list.add(o);
            }
            return list;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public Order getOrder(int id) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int subIdTemp = rs.getInt("subscriber_id");
                Integer subId = rs.wasNull() ? null : subIdTemp;

                String statusStr = rs.getString("status");
                OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

                return new Order(
                        rs.getInt("order_number"),
                        rs.getTimestamp("order_date"),
                        rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"),
                        subId,
                        rs.getTimestamp("date_of_placing_order"),
                        rs.getString("identification_details"),
                        rs.getString("full_name"),
                        rs.getDouble("total_price"),
                        status
                );
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public boolean createOrder(Order o) throws SQLException {
        String sql = "INSERT INTO `order` (order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, identification_details, full_name, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            stmt.setTimestamp(1, new java.sql.Timestamp(o.getOrder_date().getTime()));
            stmt.setInt(2, o.getNumber_of_guests());
            stmt.setInt(3, o.getConfirmation_code());

            if (o.getSubscriber_id() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, o.getSubscriber_id());
            }

            stmt.setTimestamp(5, new java.sql.Timestamp(o.getDate_of_placing_order().getTime()));
            stmt.setString(6, o.getIdentification_details());
            stmt.setString(7, o.getFull_name());
            stmt.setDouble(8, o.getTotal_price());
            stmt.setString(9, o.getStatus().name());

            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public boolean updateOrder(Order o) throws SQLException {
        String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, subscriber_id = ?, date_of_placing_order = ?, identification_details = ?, full_name = ?, total_price = ?, status = ? WHERE order_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            stmt.setTimestamp(1, new java.sql.Timestamp(o.getOrder_date().getTime()));
            stmt.setInt(2, o.getNumber_of_guests());
            stmt.setInt(3, o.getConfirmation_code());

            if (o.getSubscriber_id() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, o.getSubscriber_id());
            }

            stmt.setTimestamp(5, new java.sql.Timestamp(o.getDate_of_placing_order().getTime()));
            stmt.setString(6, o.getIdentification_details());
            stmt.setString(7, o.getFull_name());
            stmt.setDouble(8, o.getTotal_price());
            stmt.setString(9, o.getStatus().name());

            stmt.setInt(10, o.getOrder_number());

            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    public boolean deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM `order` WHERE order_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    public int countActiveOrdersInTimeRange(java.util.Date requestedDate, int numberOfGuests) throws SQLException {
        // SQL query to count orders within 2 hours before or after the requested time
        // Filter by status (APPROVED/SEATED) and minimum guest capacity
        String sql = "SELECT COUNT(*) FROM `order` " +
                     "WHERE order_date BETWEEN (? - INTERVAL 2 HOUR) AND (? + INTERVAL 2 HOUR) " +
                     "AND status IN ('APPROVED', 'SEATED') " +
                     "AND number_of_guests >= ?";
                     
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            
            // Convert java.util.Date to java.sql.Timestamp for SQL compatibility
            Timestamp ts = new Timestamp(requestedDate.getTime());
            
            stmt.setTimestamp(1, ts); // For the start of the interval
            stmt.setTimestamp(2, ts); // For the end of the interval
            stmt.setInt(3, numberOfGuests); // Match guest capacity logic
            
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Return the count of overlapping active orders
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }
}