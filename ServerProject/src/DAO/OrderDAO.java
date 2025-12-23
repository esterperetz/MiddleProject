package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import entities.Order;
import entities.Order.OrderStatus;

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

				String statusStr = rs.getString("order_status");
				OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

				Order o = new Order(rs.getInt("order_number"), rs.getTimestamp("order_date"),
						rs.getInt("number_of_guests"), rs.getInt("confirmation_code"), subId,
						rs.getTimestamp("date_of_placing_order"), rs.getString("client_name"), 
						rs.getString("client_email"), 
						rs.getString("client_phone"), 
						rs.getTimestamp("arrival_time"),
						rs.getTimestamp("leaving_time"), 
						rs.getDouble("total_price"), status);
				list.add(o);
			}
			return list;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}
	
	public List<Order> getOrdersBySubscriberId(int subscriberId) throws SQLException {
	    // שאילתה לשליפת כל ההזמנות ששייכות למנוי מסוים
	    String sql = "SELECT * FROM `order` WHERE subscriber_id = ?";
	    Connection con = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    try {
	        con = DBConnection.getInstance().getConnection();
	        stmt = con.prepareStatement(sql);
	        stmt.setInt(1, subscriberId);
	        rs = stmt.executeQuery();

	        List<Order> orderList = new ArrayList<>();
	        while (rs.next()) {
	            // טיפול ב-Status (מניעת שגיאה אם השדה ריק)
	            String statusStr = rs.getString("order_status");
	            OrderStatus status = (statusStr != null) ? OrderStatus.valueOf(statusStr) : OrderStatus.APPROVED;

	            // יצירת אובייקט הזמנה והוספה לרשימה
	            Order o = new Order(
	                rs.getInt("order_number"),
	                rs.getTimestamp("order_date"),
	                rs.getInt("number_of_guests"),
	                rs.getInt("confirmation_code"),
	                subscriberId, // אנחנו כבר יודעים את ה-ID
	                rs.getTimestamp("date_of_placing_order"),
	                rs.getString("client_name"), 
	                rs.getString("client_email"), 
	                rs.getString("client_phone"), 
	                rs.getTimestamp("arrival_time"),
	                rs.getTimestamp("leaving_time"),
	                rs.getDouble("total_price"), 
	                status
	            );
	            orderList.add(o);
	        }
	        return orderList;
	    } finally {
	        // סגירת משאבים
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

				return new Order(rs.getInt("order_number"), rs.getTimestamp("order_date"),
						rs.getInt("number_of_guests"), rs.getInt("confirmation_code"), subId,
						rs.getTimestamp("date_of_placing_order"), rs.getString("client_name"), 
						rs.getString("client_email"), 
						rs.getString("client_phone"), 
						rs.getTimestamp("arrival_time"),
						rs.getTimestamp("leaving_time"),
						rs.getDouble("total_price"), status);
			}
			return null;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public boolean createOrder(Order o) throws SQLException {
		// עדכון השאילתה לשדות החדשים
		String sql = "INSERT INTO `order` (order_date, number_of_guests, confirmation_code, subscriber_id, "
				+ "date_of_placing_order, client_name, client_email, client_phone, arrival_time, total_price, order_status) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(o.getOrderDate().getTime()));
			stmt.setInt(2, o.getNumberOfGuests());
			stmt.setInt(3, o.getConfirmationCode());

			if (o.getSubscriberId() == null) {
				stmt.setNull(4, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(4, o.getSubscriberId());
			}

			stmt.setTimestamp(5, new java.sql.Timestamp(o.getDateOfPlacingOrder().getTime()));

			stmt.setString(6, o.getClientName());
			stmt.setString(7, o.getClientEmail());
			stmt.setString(8, o.getClientPhone());
			if (o.getArrivalTime() != null) {
				stmt.setTimestamp(9, new java.sql.Timestamp(o.getArrivalTime().getTime()));
			} else {
				stmt.setNull(9, java.sql.Types.TIMESTAMP);
			}

			// 10. total_price
			stmt.setDouble(10, o.getTotalPrice());
			// 11. order_status
			stmt.setString(11, o.getOrderStatus().name());

			return stmt.executeUpdate() > 0;
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	public boolean updateOrder(Order o) throws SQLException {
	    String sql = "UPDATE `order` SET order_date = ?, number_of_guests = ?, confirmation_code = ?, " +
	                 "subscriber_id = ?, date_of_placing_order = ?, client_name = ?, client_email = ?, " +
	                 "client_Phone = ?, arrival_time = ?, total_price = ?, order_status = ? " +
	                 "WHERE order_number = ?";
	    
	    Connection con = null;
	    PreparedStatement stmt = null;

	    try {
	        con = DBConnection.getInstance().getConnection();
	        stmt = con.prepareStatement(sql);

	        stmt.setTimestamp(1, new java.sql.Timestamp(o.getOrderDate().getTime()));
	        stmt.setInt(2, o.getNumberOfGuests());
	        stmt.setInt(3, o.getConfirmationCode());

	        if (o.getSubscriberId() == null) {
	            stmt.setNull(4, java.sql.Types.INTEGER);
	        } else {
	            stmt.setInt(4, o.getSubscriberId());
	        }

	        stmt.setTimestamp(5, new java.sql.Timestamp(o.getDateOfPlacingOrder().getTime()));
	        
	        stmt.setString(6, o.getClientName());
	        stmt.setString(7, o.getClientEmail());
	        stmt.setString(8, o.getClientPhone());
	        
	        if (o.getArrivalTime() != null) {
	            stmt.setTimestamp(9, new java.sql.Timestamp(o.getArrivalTime().getTime()));
	        } else {
	            stmt.setNull(9, java.sql.Types.TIMESTAMP);
	        }

	        stmt.setDouble(10, o.getTotalPrice());
	        
	        stmt.setString(11, o.getOrderStatus().name());

	        stmt.setInt(12, o.getOrderNumber());

	        return stmt.executeUpdate() > 0;
	    } finally {
	        if (stmt != null)
	            stmt.close();
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
			if (stmt != null)
				stmt.close();
		}
	}

	public Order getByConfirmationCode(int code) throws SQLException {
		String sql = "SELECT * FROM `order` WHERE confirmation_code = ? AND order_status = 'APPROVED'";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setInt(1, code);
			rs = stmt.executeQuery();

			if (rs.next()) {
				int subIdTemp = rs.getInt("subscriber_id");
				Integer subId = rs.wasNull() ? null : subIdTemp;

				return new Order(
				    rs.getInt("order_number"),
				    rs.getTimestamp("order_date"),
				    rs.getInt("number_of_guests"),
				    rs.getInt("confirmation_code"),
				    subId,
				    rs.getTimestamp("date_of_placing_order"),
				    rs.getString("client_name"),   
				    rs.getString("client_email"),  
				    rs.getString("client_Phone"),  
				    rs.getTimestamp("ArrivalTime"),
				    rs.getTimestamp("leaving_time"),
				    rs.getDouble("total_price"),
				    OrderStatus.valueOf(rs.getString("order_status")) 
				);
			}
			return null;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public boolean updateOrderStatus(int orderNumber, Order.OrderStatus status) throws SQLException {
		String sql = "UPDATE `order` SET status = ? WHERE order_number = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, status.name());
			stmt.setInt(2, orderNumber);
			return stmt.executeUpdate() > 0;
		}
	}

	public int countActiveOrdersInTimeRange(java.util.Date requestedDate, int numberOfGuests) throws SQLException {
		// SQL query to count orders within 2 hours before or after the requested time
		// Filter by status (APPROVED/SEATED) and minimum guest capacity
		String sql = "SELECT COUNT(*) FROM `order` "
				+ "WHERE order_date BETWEEN (? - INTERVAL 2 HOUR) AND (? + INTERVAL 2 HOUR) "
				+ "AND order_status IN ('APPROVED', 'SEATED') " + "AND number_of_guests >= ?";

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
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}
}