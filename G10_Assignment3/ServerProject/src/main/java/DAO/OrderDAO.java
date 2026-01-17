package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import DBConnection.DBConnection;
import entities.Customer;
import entities.Order;
import entities.Order.OrderStatus;

/**
 * Data Access Object (DAO) for managing Order entities.
 * 
 * Handles all database interactions regarding orders, including creation,
 * retrieval, updates, status changes, and complex reporting queries.
 */
public class OrderDAO {

    /**
     * Retrieves all orders from the database, joined with customer details.
     *
     * @return A list of all orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getAllOrders() throws SQLException {
        String sql = "SELECT o.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type "
                + "FROM `order` o " + "LEFT JOIN Customer c ON o.customer_id = c.customer_id";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            List<Order> list = new ArrayList<>();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.getCustomer().setName(rs.getString("customer_name"));
                order.getCustomer().setEmail(rs.getString("email"));
                order.getCustomer().setPhoneNumber(rs.getString("phone_number"));
                list.add(order);
            }
            return list;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves all APPROVED orders for a specific subscriber code.
     *
     * @param subscriberCode The subscriber's unique code.
     * @return List of approved orders for that subscriber.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getAllOrdersApproved(int subscriberCode) throws SQLException {
        String sql = "SELECT o.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type "
                + "FROM `order` o "
                + "LEFT JOIN Customer c ON o.customer_id = c.customer_id "
                + "WHERE o.order_status='APPROVED' AND c.subscriber_code = ?";

        Connection con = DBConnection.getInstance().getConnection();

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, subscriberCode);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);

                    if (order.getCustomer() != null) {
                        order.getCustomer().setName(rs.getString("customer_name"));
                        order.getCustomer().setEmail(rs.getString("email"));
                        order.getCustomer().setPhoneNumber(rs.getString("phone_number"));
                        order.getCustomer().setSubscriberCode(rs.getInt("subscriber_code"));
                    }

                    list.add(order);
                }
                return list;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves the size (number of guests) of active orders that overlap with a 2-hour window
     * starting from the requested date.
     *
     * @param requestedDate The start time to check.
     * @return List of integers representing guest counts of conflicting orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Integer> getActiveOrderSizes2(Date requestedDate) throws SQLException {
        List<Integer> activeSizes = new ArrayList<>();

        java.sql.Timestamp checkStart = new java.sql.Timestamp(requestedDate.getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(requestedDate);
        cal.add(Calendar.HOUR_OF_DAY, 2);
        java.sql.Timestamp checkEnd = new java.sql.Timestamp(cal.getTime().getTime());

        String sql = "SELECT number_of_guests FROM `order` "
                + "WHERE (order_status NOT IN ('CANCELLED', 'PAID','SEATED') OR order_status = 'PENDING') "
                + "AND order_date < ? " + "AND DATE_ADD(order_date, INTERVAL 2 HOUR) > ?";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, checkEnd);
            stmt.setTimestamp(2, checkStart);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activeSizes.add(rs.getInt("number_of_guests"));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return activeSizes;
    }

    /**
     * Retrieves orders that are due for a reminder.
     * Looks for orders occurring within a specific time window (+/- 2 minutes of the target time).
     *
     * @param minutesAhead How many minutes ahead the reminder should be sent (e.g., 120 for 2 hours).
     * @return List of orders requiring reminders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getOrdersForReminder(int minutesAhead) throws SQLException {
        String sql = "SELECT o.*, c.email, c.customer_name, c.phone_number " + "FROM `order` o "
                + "JOIN Customer c ON o.customer_id = c.customer_id "
                + "WHERE o.order_date BETWEEN (NOW() + INTERVAL ? MINUTE - INTERVAL 2 MINUTE) "
                + "AND (NOW() + INTERVAL ? MINUTE + INTERVAL 2 MINUTE) " + "AND o.reminder_sent = FALSE "
                + "AND o.order_status IN ('APPROVED')";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, minutesAhead);
            stmt.setInt(2, minutesAhead);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.getCustomer().setEmail(rs.getString("email"));
                    order.getCustomer().setName(rs.getString("customer_name"));
                    order.getCustomer().setPhoneNumber(rs.getString("phone_number"));
                    list.add(order);
                }
                return list;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Marks an order as having its reminder sent.
     *
     * @param orderNumber The order ID.
     * @return true if update was successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean markAsReminded(int orderNumber) throws SQLException {
        String sql = "UPDATE `order` SET reminder_sent = TRUE WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, orderNumber);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves all orders joined with customer data as a Map.
     * Useful for constructing data tables in the UI.
     *
     * @return List of Maps containing order and customer data.
     */
    public List<Map<String, Object>> getAllOrdersWithCustomers() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        String sql = "SELECT " + " c.subscriber_code, c.email, c.customer_name, c.phone_number, "
                + " o.order_number, o.customer_id, o.order_date, o.arrival_time, "
                + " o.number_of_guests, o.total_price, o.order_status, "
                + " o.confirmation_code, o.date_of_placing_order " + "FROM Customer c "
                + "JOIN `order` o ON c.customer_id = o.customer_id";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("customer_name", rs.getString("customer_name"));
                    row.put("email", rs.getString("email"));
                    row.put("phone_number", rs.getString("phone_number"));
                    row.put("subscriber_code", (Integer) rs.getObject("subscriber_code"));
                    row.put("order_number", rs.getInt("order_number"));
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("order_date", rs.getTimestamp("order_date"));
                    row.put("arrival_time", rs.getTimestamp("arrival_time"));
                    row.put("number_of_guests", rs.getInt("number_of_guests"));
                    row.put("total_price", rs.getDouble("total_price"));
                    row.put("order_status", rs.getString("order_status"));
                    row.put("confirmation_code", rs.getInt("confirmation_code"));
                    row.put("date_of_placing_order", rs.getTimestamp("date_of_placing_order"));
                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return resultList;
    }

    /**
     * Retrieves all orders for a specific customer ID.
     *
     * @param customerId The customer ID.
     * @return List of orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getOrdersByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE customer_id = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> orderList = new ArrayList<>();
                while (rs.next()) {
                    orderList.add(mapResultSetToOrder(rs));
                }
                return orderList;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Checks if a customer already has an approved order within a 2-hour window of the checkDate.
     *
     * @param customerId The customer ID.
     * @param checkDate  The date to check around.
     * @return The existing Order if found, or null.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderInTimeWindow(int customerId, Date checkDate) throws SQLException {
        Timestamp sqlTimestamp = new Timestamp(checkDate.getTime());

        String sql = "SELECT * FROM `order` WHERE customer_id = ? " +
                "AND order_date >= DATE_SUB(?, INTERVAL 2 HOUR) " +
                "AND order_date <= DATE_ADD(?, INTERVAL 2 HOUR) " +
                "AND order_status = 'APPROVED'" +
                "LIMIT 1";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            stmt.setTimestamp(2, sqlTimestamp);
            stmt.setTimestamp(3, sqlTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Counts conflicting orders for table availability check.
     * 
     * Checks for approved or seated orders that overlap with the requested 2-hour window.
     *
     * @param requestedDate The requested reservation time.
     * @param guests        The number of guests (to match table capacity).
     * @return Count of conflicting orders.
     * @throws SQLException If a database error occurs.
     */
    public int countConflictingOrders(Date requestedDate, int guests) throws SQLException {
        java.sql.Timestamp newStart = new java.sql.Timestamp(requestedDate.getTime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(requestedDate);
        cal.add(Calendar.HOUR_OF_DAY, 2);
        java.sql.Timestamp newEnd = new java.sql.Timestamp(cal.getTime().getTime());

        String sql = "SELECT COUNT(*) FROM `order` o " + "JOIN tables t ON o.table_number = t.table_number "
                + "WHERE t.number_of_seats >= ? " + "AND o.order_status IN ('APPROVED', 'SEATED') "
                + "AND o.order_date < ? " + "AND DATE_ADD(o.order_date, INTERVAL 2 HOUR) > ?";

        Connection con = DBConnection.getInstance().getConnection();

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, guests);
            stmt.setTimestamp(2, newEnd);
            stmt.setTimestamp(3, newStart);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return 0;
    }

    /**
     * Retrieves an order by its unique order number.
     *
     * @param id The order ID.
     * @return The Order object.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrder(int id) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Inserts a new order into the database.
     *
     * @param o The Order object to create.
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean createOrder(Order o) throws SQLException {
        String sql = "INSERT INTO `order` (order_date, number_of_guests, confirmation_code, customer_id, table_number, "
                + "date_of_placing_order, arrival_time, total_price, order_status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(o.getOrderDate().getTime()));
            stmt.setInt(2, o.getNumberOfGuests());
            stmt.setInt(3, o.getConfirmationCode());

            if (o.getCustomer() == null || o.getCustomer().getCustomerId() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, o.getCustomer().getCustomerId());
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
            if (o.getOrderStatus() != null) {
                stmt.setString(9, o.getOrderStatus().name());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Updates all fields of an existing order.
     *
     * @param o The Order object with updated values.
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateOrder(Order o) throws SQLException {
        String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, "
                + "customer_id = ?, table_number = ?, date_of_placing_order = ?, "
                + "arrival_time = ?, total_price = ?, order_status = ?, reminder_sent = ? " + "WHERE order_number = ?";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            System.out.println(o.toString());
            stmt.setTimestamp(1, new Timestamp(o.getOrderDate().getTime()));
            stmt.setInt(2, o.getNumberOfGuests());
            stmt.setInt(3, o.getConfirmationCode());

            if (o.getCustomer() == null || o.getCustomer().getCustomerId() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, o.getCustomer().getCustomerId());
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
            System.out.println("in the dao " + o.getOrderStatus().name());
            stmt.setString(9, o.getOrderStatus().name());
            stmt.setBoolean(10, o.isReminderSent());
            stmt.setInt(11, o.getOrderNumber());

            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Deletes an order from the database.
     *
     * @param id The order ID.
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM `order` WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves APPROVED orders for a specific number of guests.
     *
     * @param guests The number of guests.
     * @return List of matching orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getApprovedOrdersByGuestCount(int guests) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_status = 'APPROVED' AND number_of_guests = ?";

        List<Order> list = new ArrayList<>();
        Connection con = DBConnection.getInstance().getConnection();

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, guests);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = this.mapResultSetToOrder(rs);

                    list.add(o);
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return list;
    }

    /**
     * Fetches an APPROVED order by confirmation code.
     *
     * @param code The confirmation code.
     * @return The Order object if found.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByConfirmationCode(int code) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE confirmation_code = ? AND order_status = 'APPROVED' ";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Fetches a PENDING order by confirmation code.
     *
     * @param confirmationCode The confirmation code.
     * @return The Order object if found.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByConfirmationCodePending(int confirmationCode) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE confirmation_code = ? AND order_status = 'PENDING' ";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, confirmationCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Fetches an order by confirmation code that is either PENDING or APPROVED.
     * Used for waiting list checks.
     *
     * @param code The confirmation code.
     * @return The Order object if found.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByConfirmationCodeWithStatusNull(int code) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE confirmation_code = ? AND (order_status = 'PENDING' OR order_status = 'APPROVED')";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Checks if there is an approved order ready for seating (within 15 minutes).
     *
     * @param code       Confirmation code.
     * @param customerId Customer ID (optional, can be null).
     * @return The Order if found and valid.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByConfirmationCodeApproved(int code, Integer customerId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE (customer_id = ? OR confirmation_code = ?) "
                + "AND order_status = 'APPROVED' "
                + "AND ABS(TIMESTAMPDIFF(MINUTE, order_date, NOW())) <= 15";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            if (customerId != null) {
                stmt.setInt(1, customerId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Checks if there is a SEATED order for a customer or code.
     *
     * @param code       Confirmation code.
     * @param customerId Customer ID.
     * @return The Order if found.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByConfirmationCodeSeated(int code, Integer customerId) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE (customer_id = ? OR confirmation_code = ?) "
                + "AND order_status = 'SEATED' ";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            if (customerId != null && customerId != 0) {
                stmt.setInt(1, customerId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Updates order status to PAID and sets leaving time.
     *
     * @param orderId    Order ID.
     * @param totalPrice Final price.
     * @param status     New status (PAID).
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateOrderCheckOut(int orderId, double totalPrice, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE `order` SET  order_status = ?, leaving_time = NOW() ,total_price = ? WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setDouble(2, totalPrice);
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Updates order status to SEATED, assigns a table, and sets arrival time.
     *
     * @param orderId  Order ID.
     * @param tableNum Assigned table number.
     * @param status   New status (SEATED).
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateOrderSeating(int orderId, int tableNum, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE `order` SET table_number = ?, order_status = ?, arrival_time = NOW() WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, tableNum);
            stmt.setString(2, status.name());
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Updates the status of a specific order.
     *
     * @param orderNumber The order ID.
     * @param status      The new status.
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateOrderStatus(int orderNumber, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE `order` SET order_status = ? WHERE order_number = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderNumber);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Updates order status based on confirmation code.
     *
     * @param confirmation_code The confirmation code.
     * @param status            The new status.
     * @return true if successful.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateOrderStatusByConformationCode(int confirmation_code, Order.OrderStatus status) throws SQLException {
        String sql = "UPDATE `order` SET order_status = ? WHERE confirmation_code = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, confirmation_code);
            return stmt.executeUpdate() > 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves guest counts for active orders within 2 hours of the requested date.
     *
     * @param requestedDate The date to check.
     * @return List of guest counts.
     * @throws SQLException If a database error occurs.
     */
    public List<Integer> getActiveOrdersInTimeRange(java.util.Date requestedDate) throws SQLException {

        List<Integer> activeSizes = new ArrayList<>();

        String sql = "SELECT number_of_guests FROM `order` " + "WHERE order_status IN ('APPROVED', 'SEATED') "
                + "AND ABS(TIMESTAMPDIFF(MINUTE, order_date, ?)) < 120";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            Timestamp reqTime = new Timestamp(requestedDate.getTime());

//            stmt.setInt(1, minGuestsThreshold); 
            stmt.setTimestamp(1, reqTime);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activeSizes.add(rs.getInt("number_of_guests"));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return activeSizes;
    }

    /**
     * Counts active orders (APPROVED/SEATED) that overlap with the requested time.
     *
     * @param requestedDate      Date to check.
     * @param minGuestsThreshold Filter by guest count.
     * @return Count of orders.
     * @throws SQLException If a database error occurs.
     */
    public int countActiveOrdersInTimeRange(java.util.Date requestedDate, int minGuestsThreshold) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` " + "WHERE order_status IN ('APPROVED', 'SEATED') "
                + "AND number_of_guests >= ? " + "AND ABS(TIMESTAMPDIFF(MINUTE, order_date, ?)) < 120";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                Timestamp reqTime = new Timestamp(requestedDate.getTime());
                stmt.setInt(1, minGuestsThreshold);
                stmt.setTimestamp(2, reqTime);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Counts dis-active orders (CANCELLED/PAID) that overlap with the requested time.
     *
     * @param requestedDate      Date to check.
     * @param minGuestsThreshold Filter by guest count.
     * @return Count of orders.
     * @throws SQLException If a database error occurs.
     */
    public int countDisActiveOrdersInTimeRange(java.util.Date requestedDate, int minGuestsThreshold)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` " + "WHERE order_status IN ('CANCELLED', 'PAID') "
                + "AND number_of_guests >= ? " + "AND ABS(TIMESTAMPDIFF(MINUTE, order_date, ?)) < 120";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                Timestamp reqTime = new Timestamp(requestedDate.getTime());
                stmt.setInt(1, minGuestsThreshold);
                stmt.setTimestamp(2, reqTime);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? rs.getInt(1) : 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Counts currently SEATED orders with at least the specified guest count.
     *
     * @param guests Minimum guests.
     * @return Count of orders.
     * @throws SQLException If a database error occurs.
     */
    public int countCurrentlySeatedOrders(int guests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE order_status = 'SEATED' AND number_of_guests >= ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, guests);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Counts APPROVED orders within a specific date range.
     *
     * @param start  Start date.
     * @param end    End date.
     * @param guests Minimum guests.
     * @return Count of orders.
     * @throws SQLException If a database error occurs.
     */
    public int countApprovedOrdersInRange(java.util.Date start, java.util.Date end, int guests) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `order` WHERE order_status = 'APPROVED' "
                + "AND order_date BETWEEN ? AND ? AND number_of_guests >= ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(start.getTime()));
            stmt.setTimestamp(2, new Timestamp(end.getTime()));
            stmt.setInt(3, guests);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Counts active APPROVED orders for a specific timestamp.
     *
     * @param timestamp Exact time to check.
     * @param guests    Minimum guests.
     * @return Count of orders.
     */
    public int countActiveOrders(java.util.Date timestamp, int guests) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM `order` " + "WHERE order_date = ? " + "AND order_status = 'APPROVED' "
                + "AND number_of_guests >= ?";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setTimestamp(1, new java.sql.Timestamp(timestamp.getTime()));
                stmt.setInt(2, guests);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            return 0;
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return count;
    }

    /**
     * Retrieves all orders matching a specific status.
     *
     * @param status The OrderStatus to filter by.
     * @return List of matching orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_status = ?";
        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
                return list;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Helper method to map a ResultSet row to an Order object.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return The mapped Order object.
     * @throws SQLException If a database error occurs.
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int cusIdTemp = rs.getInt("customer_id");
        Integer cusId = rs.wasNull() ? null : cusIdTemp;
        int tableNumTemp = rs.getInt("table_number");
        Integer tableNumber = rs.wasNull() ? null : tableNumTemp;
        String statusStr = rs.getString("order_status");
        OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

        Customer customer = new Customer();
        if (cusId != null) {
            customer.setCustomerId(cusId);
        }

        Order order = new Order(rs.getInt("order_number"), rs.getTimestamp("order_date"), rs.getInt("number_of_guests"),
                rs.getInt("confirmation_code"), customer, tableNumber, rs.getTimestamp("date_of_placing_order"),
                rs.getTimestamp("arrival_time"), rs.getTimestamp("leaving_time"), rs.getDouble("total_price"), status);

        try {
            order.setReminderSent(rs.getBoolean("reminder_sent"));
        } catch (SQLException e) {
        }
        return order;
    }

    /**
     * Retrieves an order by customer contact details (email or phone).
     * Looks for recent APPROVED orders (last 15 minutes window).
     *
     * @param contactDetail Email or phone number.
     * @return The matching Order or null.
     * @throws SQLException If a database error occurs.
     */
    public Order getOrderByContact(String contactDetail) throws SQLException {
        String sql = "SELECT o.*, c.email, c.customer_name, c.phone_number " + "FROM `order` o "
                + "JOIN Customer c ON o.customer_id = c.customer_id " + "WHERE (c.email = ? OR c.phone_number = ?) "
                + "AND o.order_status = 'APPROVED' " + "AND o.order_date >= DATE_SUB(NOW(), INTERVAL 15 MINUTE) "
                + "ORDER BY o.order_date ASC " + "LIMIT 1";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, contactDetail);
            stmt.setString(2, contactDetail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.getCustomer().setEmail(rs.getString("email"));
                    order.getCustomer().setName(rs.getString("customer_name"));
                    order.getCustomer().setPhoneNumber(rs.getString("phone_number"));
                    return order;
                }
                return null;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves finished orders (PAID or CANCELLED) for a specific month/year.
     * Used for generating management reports.
     *
     * @param month The month to filter.
     * @param year  The year to filter.
     * @return List of finished orders.
     * @throws SQLException If a database error occurs.
     */
    public List<Order> getFinishedOrdersByMonth(int month, int year) throws SQLException {
        String sql = "SELECT o.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type "
                + "FROM `order` o " + "LEFT JOIN Customer c ON o.customer_id = c.customer_id "
                + "WHERE (MONTH(o.order_date) = ? AND YEAR(o.order_date) = ?) "
                + "AND o.order_status IN ('PAID', 'CANCELLED') " + "ORDER BY o.order_date ASC";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, month);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);

                    if (order.getCustomer() == null) {
                        order.setCustomer(new entities.Customer());
                    }

                    String nameFromDB = rs.getString("customer_name");
                    if (nameFromDB != null) {
                        order.getCustomer().setName(nameFromDB);
                    } else {
                        order.getCustomer().setName("Guest");
                    }

                    order.getCustomer().setEmail(rs.getString("email"));

                    if (rs.getString("customer_type") != null) {
                        try {
                            order.getCustomer().setType(entities.CustomerType.valueOf(rs.getString("customer_type")));
                        } catch (Exception e) {
                        }
                    }

                    list.add(order);
                }
                return list;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    /**
     * Retrieves sizes of active orders around the requested date (2 hour window).
     *
     * @param requestedDate The date to check.
     * @return List of guest counts.
     * @throws SQLException If a database error occurs.
     */
    public List<Integer> getActiveOrderSizes(Date requestedDate) throws SQLException {
        List<Integer> activeSizes = new ArrayList<>();

        String sql = "SELECT number_of_guests FROM `order` " + "WHERE order_status IN ('APPROVED', 'SEATED') "
                + "AND ABS(TIMESTAMPDIFF(MINUTE, order_date, ?)) < 120";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(requestedDate.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activeSizes.add(rs.getInt("number_of_guests"));
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return activeSizes;
    }
}