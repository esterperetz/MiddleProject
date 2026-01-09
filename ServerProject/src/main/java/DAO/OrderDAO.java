package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DBConnection.DBConnection;
import entities.Order;
import entities.Order.OrderStatus;

public class OrderDAO {

	// /**
	// * Retrieves all orders from the database.
	// */
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
	 * Retrieves orders that are scheduled between 'minutesAhead' - 1 and
	 * 'minutesAhead' + 1 minutes from now,
	 * and have NOT been reminded yet.
	 * 
	 * @param minutesAhead The target time window in minutes (e.g., 120 for 2 hours)
	 */
	public List<Order> getOrdersForReminder(int minutesAhead) throws SQLException {
		// We look for orders where (order_date) is roughly (now + minutesAhead)
		// AND reminder_sent is FALSE.
		// JOIN to fetch minimal customer details for email

		String sql = "SELECT o.*, c.email, c.customer_name, c.phone_number "
				+ "FROM `order` o "
				+ "JOIN Customer c ON o.customer_id = c.customer_id "
				+ "WHERE o.order_date BETWEEN (NOW() + INTERVAL ? MINUTE - INTERVAL 1 MINUTE) "
				+ "AND (NOW() + INTERVAL ? MINUTE + INTERVAL 1 MINUTE) "
				+ "AND o.reminder_sent = FALSE "
				+ "AND o.order_status IN ('APPROVED')";

		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setInt(1, minutesAhead);
			stmt.setInt(2, minutesAhead);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Order> list = new ArrayList<>();
				while (rs.next()) {
					Order order = mapResultSetToOrder(rs);
					// Populate temporary client fields explicitly for email service
					order.setTempClientEmail(rs.getString("email"));
					order.setTempClientName(rs.getString("customer_name"));
					order.setTempClientPhone(rs.getString("phone_number"));
					list.add(order);
				}
				return list;
			}
		}
	}

	/**
	 * Updates the reminder_sent flag for a specific order.
	 */
	public boolean markAsReminded(int orderNumber) throws SQLException {
		String sql = "UPDATE `order` SET reminder_sent = TRUE WHERE order_number = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setInt(1, orderNumber);
			return stmt.executeUpdate() > 0;
		}
	}

	public List<Map<String, Object>> getAllOrdersWithCustomers() {
		List<Map<String, Object>> resultList = new ArrayList<>();

		String sql = "SELECT " +
				" c.subscriber_code, c.email, c.customer_name, c.phone_number, " +
				" o.order_number, o.customer_id, o.order_date, o.arrival_time, " +
				" o.number_of_guests, o.total_price, o.order_status, " +
				" o.confirmation_code, o.date_of_placing_order " +
				"FROM Customer c " +
				"JOIN `order` o ON c.customer_id = o.customer_id";

		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Map<String, Object> row = new HashMap<>();

				// customer
				row.put("customer_name", rs.getString("customer_name"));
				row.put("email", rs.getString("email"));
				row.put("phone_number", rs.getString("phone_number"));
				row.put("subscriber_code", (Integer) rs.getObject("subscriber_code")); // מאפשר null

				// order
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
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resultList;
	}

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
		String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, "
				+ "customer_id = ?, table_number = ?, date_of_placing_order = ?, "
				+ "arrival_time = ?, total_price = ?, order_status = ?, reminder_sent = ? " + "WHERE order_number = ?";

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
			stmt.setBoolean(10, o.isReminderSent());
			stmt.setInt(11, o.getOrderNumber());

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
	 * Counts approved orders within a specific window to protect upcoming
	 * reservations.
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
	 * Fetches orders with a specific status from the database. This is used for
	 * efficient background processing (e.g., cleanup or waiting list logic).
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

		Order order = new Order(rs.getInt("order_number"), rs.getTimestamp("order_date"), rs.getInt("number_of_guests"),
				rs.getInt("confirmation_code"), cusId, tableNumber, rs.getTimestamp("date_of_placing_order"),
				rs.getTimestamp("arrival_time"), rs.getTimestamp("leaving_time"), rs.getDouble("total_price"), status);

		// Attempt to read reminder_sent, default to false if column missing or null
		// (though boolean usually not null)
		// Assuming column exists as per requirements.
		try {
			order.setReminderSent(rs.getBoolean("reminder_sent"));
		} catch (SQLException e) {
			// Column might not exist in old schema version, ignore or log
			// System.err.println("Column reminder_sent missing in result set");
		}

		return order;
	}

	public Order getOrderByContact(String contactDetail) throws SQLException {
		String sql = "SELECT o.*, c.email, c.customer_name, c.phone_number "
				+ "FROM `order` o "
				+ "JOIN Customer c ON o.customer_id = c.customer_id "
				+ "WHERE (c.email = ? OR c.phone_number = ?) "
				+ "AND o.order_status = 'APPROVED' "
				+ "AND o.order_date > NOW() "
				+ "ORDER BY o.order_date ASC LIMIT 1";

		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, contactDetail);
			stmt.setString(2, contactDetail);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Order order = mapResultSetToOrder(rs);
					// Populate temporary client fields for controller usage
					order.setTempClientEmail(rs.getString("email"));
					order.setTempClientName(rs.getString("customer_name"));
					order.setTempClientPhone(rs.getString("phone_number"));
					return order;
				}
				return null;
			}
		}
	}

	public int cancelExpiredOrders() throws SQLException {
		// Cancel orders that are 'APPROVED' but the time has passed by more than 20
		// minutes
		String sql = "UPDATE `order` SET order_status = 'CANCELLED' "
				+ "WHERE order_status = 'APPROVED' AND order_date < (NOW() - INTERVAL 20 MINUTE)";

		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {
			return stmt.executeUpdate();
		}
	}
}