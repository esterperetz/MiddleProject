package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import entities.Order;
import entities.Order.OrderStatus;

public class OrderDAO {

    /**
     * Retrieves all orders from the database.
     */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT * FROM `order`";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Order> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
            return list;
        }
    }

    /**
     * Retrieves orders belonging to a specific subscriber.
     */
    public List<Order> getOrdersByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE customer_id = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orderList = new ArrayList<>();
                while (rs.next()) {
                    orderList.add(mapResultSetToOrder(rs));
                }
                return orderList;
            }
        }
    }

    /**
     * Retrieves a single order by its ID.
     */
    public Order getOrder(int id) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        }
    }

    /**
     * Inserts a new order into the database.
     */
    public boolean createOrder(Order o) throws SQLException {
        String sql = "INSERT INTO `order` (order_date, number_of_guests, confirmation_code, customer_id, table_number, "
                + "date_of_placing_order,arrival_time, total_price, order_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, new Timestamp(o.getOrderDate().getTime()));
            stmt.setInt(2, o.getNumberOfGuests());
            stmt.setInt(3, o.getConfirmationCode());

            if (o.getCustomerId() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, o.getCustomerId());
            }

            if (o.getTableNumber() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, o.getTableNumber());
            }

            stmt.setTimestamp(6, new Timestamp(o.getDateOfPlacingOrder().getTime()));
         

            if (o.getArrivalTime() != null) {
                stmt.setTimestamp(7, new Timestamp(o.getArrivalTime().getTime()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setDouble(8, o.getTotalPrice());
            stmt.setString(9, o.getOrderStatus().name());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing order.
     */
    public boolean updateOrder(Order o) throws SQLException {
        String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, " +
                     "customer_id = ?, table_number = ?, date_of_placing_order = ?, " +
                     "arrival_time = ?, total_price = ?, order_status = ? " +
                     "WHERE order_number = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(o.getOrderDate().getTime()));
            stmt.setInt(2, o.getNumberOfGuests());
            stmt.setInt(3, o.getConfirmationCode());

            if (o.getCustomerId() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, o.getCustomerId());
            }

            if (o.getTableNumber() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, o.getTableNumber());
            }

            stmt.setTimestamp(6, new Timestamp(o.getDateOfPlacingOrder().getTime()));
           

            if (o.getArrivalTime() != null) {
                stmt.setTimestamp(7, new Timestamp(o.getArrivalTime().getTime()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setDouble(8, o.getTotalPrice());
            stmt.setString(9, o.getOrderStatus().name());
            stmt.setInt(10, o.getOrderNumber());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an order by ID.
     */
    public boolean deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM `order` WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Fetches an APPROVED order by its confirmation code.
     */
    public Order getByConfirmationCode(int code) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE confirmation_code = ? AND order_status = 'APPROVED'";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        }
    }

    /**
     * Updates only the status of a specific order.
     */
    public boolean updateOrderStatus(int orderNumber, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE `order` SET order_status = ? WHERE order_number = ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderNumber);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Counts overlapping active orders for availability check.
     */
    public int countActiveOrdersInTimeRange(java.util.Date requestedDate, int numberOfGuests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` "
                + "WHERE order_date BETWEEN (? - INTERVAL 2 HOUR) AND (? + INTERVAL 2 HOUR) "
                + "AND order_status IN ('APPROVED', 'SEATED') AND number_of_guests >= ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            Timestamp ts = new Timestamp(requestedDate.getTime());
            stmt.setTimestamp(1, ts);
            stmt.setTimestamp(2, ts);
            stmt.setInt(3, numberOfGuests);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* --- NEW FUNCTIONS FOR WAITING LIST THREAD --- */

    /**
     * Counts currently seated customers that occupy a table of suitable size.
     */
    public int countCurrentlySeatedOrders(int guests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE order_status = 'SEATED' AND number_of_guests >= ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, guests);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Counts approved orders within a specific window to protect upcoming reservations.
     */
    public int countApprovedOrdersInRange(java.util.Date start, java.util.Date end, int guests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE order_status = 'APPROVED' "
                   + "AND order_date BETWEEN ? AND ? AND number_of_guests >= ?";
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(start.getTime()));
            stmt.setTimestamp(2, new Timestamp(end.getTime()));
            stmt.setInt(3, guests);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Fetches orders with a specific status from the database.
     * This is used for efficient background processing (e.g., cleanup or waiting list logic).
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_status = ?";
        
        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
                return list;
            }
        }
    }

    /**
     * Helper to map ResultSet row to Order object.
     */
	 private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
		 int cusIdTemp = rs.getInt("customer_id");
		 Integer cusId = rs.wasNull() ? null : cusIdTemp;

		 int tableNumTemp = rs.getInt("table_number");
		 Integer tableNumber = rs.wasNull() ? null : tableNumTemp;

		 String statusStr = rs.getString("order_status");
		 OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

		 return new Order(
			 rs.getInt("order_number"),
			 rs.getTimestamp("order_date"),
			 rs.getInt("number_of_guests"),
			 rs.getInt("confirmation_code"),
			 cusId,
			 tableNumber,
			 rs.getTimestamp("date_of_placing_order"),
			 rs.getTimestamp("arrival_time"),
			 rs.getTimestamp("leaving_time"),
			 rs.getDouble("total_price"),
			 status
		 );
	 }
}