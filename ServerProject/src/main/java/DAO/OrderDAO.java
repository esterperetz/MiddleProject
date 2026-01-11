package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DBConnection.DBConnection;
import entities.Customer;
import entities.Order;
import entities.Order.OrderStatus;

public class OrderDAO {

	public List<Order> getAllOrders() throws SQLException {
		// הוספת JOIN כדי להביא את שם הלקוח ופרטיו
		String sql = "SELECT o.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type " +
				"FROM `order` o " +
				"LEFT JOIN Customer c ON o.customer_id = c.customer_id";

		Connection con = DBConnection.getInstance().getConnection();
		try (PreparedStatement stmt = con.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
			List<Order> list = new ArrayList<>();
			while (rs.next()) {
				Order order = mapResultSetToOrder(rs);
				// מילוי ידני של פרטי הלקוח כדי שה-UI לא יתרוקן
				order.getCustomer().setName(rs.getString("customer_name"));
				order.getCustomer().setEmail(rs.getString("email"));
				order.getCustomer().setPhoneNumber(rs.getString("phone_number"));
				// ... וכן הלאה
				list.add(order);
			}
			return list;
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
	}

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
			try (PreparedStatement stmt = con.prepareStatement(sql);
					ResultSet rs = stmt.executeQuery()) {
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
			stmt.setString(9, o.getOrderStatus().name());

			return stmt.executeUpdate() > 0;
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
	}

	public boolean updateOrder(Order o) throws SQLException {
		String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, "
				+ "customer_id = ?, table_number = ?, date_of_placing_order = ?, "
				+ "arrival_time = ?, total_price = ?, order_status = ?, reminder_sent = ? " + "WHERE order_number = ?";

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
			stmt.setString(9, o.getOrderStatus().name());
			stmt.setBoolean(10, o.isReminderSent());
			stmt.setInt(11, o.getOrderNumber());

			return stmt.executeUpdate() > 0;
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
	}

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

	public Order getOrderByConfirmationCodeApproved(int code, Integer customerId) throws SQLException {
		String sql = "SELECT * FROM `order` WHERE (customer_id = ? OR confirmation_code = ?) "
				+ "AND order_status = 'APPROVED' "
				+ "AND DATE(order_date) = CURDATE() AND TIME(order_date)>= SUBTIME(CURTIME(), '00:15:00')";

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

	public Order getOrderByContact(String contactDetail) throws SQLException {
	    String sql = "SELECT o.*, c.email, c.customer_name, c.phone_number " 
	            + "FROM `order` o "
	            + "JOIN Customer c ON o.customer_id = c.customer_id " 
	            + "WHERE (c.email = ? OR c.phone_number = ?) "
	            + "AND o.order_status = 'APPROVED' "          
	            + "AND o.order_date >= DATE_SUB(NOW(), INTERVAL 15 MINUTE) " 
	            + "ORDER BY o.order_date ASC " 
	            + "LIMIT 1";

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
	//to manager reports 
	public List<Order> getFinishedOrdersByMonth(int month, int year) throws SQLException {
	    // 1. וודא שה-SQL שולף את השם (c.customer_name)
	    String sql = "SELECT o.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type " +
	                 "FROM `order` o " +
	                 "LEFT JOIN Customer c ON o.customer_id = c.customer_id " +
	                 "WHERE (MONTH(o.order_date) = ? AND YEAR(o.order_date) = ?) " +
	                 "AND o.order_status IN ('PAID', 'CANCELLED') " + 
	                 "ORDER BY o.order_date ASC";

	    Connection con = DBConnection.getInstance().getConnection();
	    try (PreparedStatement stmt = con.prepareStatement(sql)) {
	        stmt.setInt(1, month);
	        stmt.setInt(2, year);

	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Order> list = new ArrayList<>();
	            while (rs.next()) {
	                // המרה בסיסית של נתוני ההזמנה
	                Order order = mapResultSetToOrder(rs);
	                
	                // --- התיקון: מילוי פרטי הלקוח מתוך ה-JOIN ---
	                
	                // וודא שיש אובייקט לקוח (בדרך כלל נוצר ב-mapResultSetToOrder אם יש customer_id)
	                if (order.getCustomer() == null) {
	                    order.setCustomer(new entities.Customer());
	                }

	                // הגדרת השם ידנית מהתוצאה של ה-SQL
	                String nameFromDB = rs.getString("customer_name");
	                if (nameFromDB != null) {
	                    order.getCustomer().setName(nameFromDB); // <--- זו השורה שחסרה לך!
	                } else {
	                    order.getCustomer().setName("Guest"); // במקרה שאין שם
	                }

	                // מילוי שאר הפרטים אם הם חסרים בדוח
	                order.getCustomer().setEmail(rs.getString("email"));
	                
	                // טיפול בסוג לקוח (כפי שכבר עשינו)
	                if (rs.getString("customer_type") != null) {
	                    try {
	                        order.getCustomer().setType(entities.CustomerType.valueOf(rs.getString("customer_type")));
	                    } catch (Exception e) {}
	                }

	                list.add(order);
	            }
	            return list;
	        }
	    } finally {
	        DBConnection.getInstance().releaseConnection(con);
	    }
	}
}