package DBConnection;

import java.sql.Connection;
//import java.sql.Date;
import java.util.Date;
import java.util.List;

import Entities.Order;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBConnection {
	private Connection con;

	public DBConnection() {
		con = connectToDB();
	}

	private Connection connectToDB() {
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/prototype?serverTimezone=Asia/Jerusalem&useSSL=false", "root", "1234");
			return conn;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * get the full orders from DB
	 */
	public List<Order> getAllOrders() {

		List<Order> list = new ArrayList<>();
		try {
			PreparedStatement ps = con.prepareStatement("SELECT * FROM order");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int order_number = rs.getInt("order_number");
				Date order_date = rs.getDate("order_date");
				int number_of_guests = rs.getInt("number_of_guests");
				int confirmation_code = rs.getInt("confirmation_code");
				int subscriber_id = rs.getInt("subscriber_id");
				Date date_of_placing_order = rs.getDate("date_of_placing_order");
				Order o = new Order(order_number, order_date, number_of_guests, confirmation_code, subscriber_id,
						date_of_placing_order);
				list.add(o);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;

	}

	/**
	 * @param order_Number, he is the key in the table order
	 * @return the order with this order number
	 */
	public Order getOrder(int order_Number) {
		Order o = new Order(order_Number, null, order_Number, order_Number, order_Number, null);
		try {
			PreparedStatement ps = con.prepareStatement("SELECT *  FROM order WHERE order_number= ");
			ps.setString(1, order_Number + "");// change the ?
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				o.setOrder_number(rs.getInt("order_number"));
				o.setOrder_date(rs.getDate("order_date"));
				o.setOrder_number(rs.getInt("number_of_guests"));
				o.setConfirmation_code(rs.getInt("confirmation_code"));
				o.setDate_of_placing_order(rs.getDate("date_of_placing_order"));
				o.setSubscriber_id(rs.getInt("subscriber_id"));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;

	}

	/**
	 * @param orderNumber , the primary key of the order that we want to update
	 * @param newDate,    the new date of the order that we update
	 * @param newGuests,  the new number of guests of the order that we update the
	 *                    method update the order in DB
	 */
	public void updateOrder(int orderNumber, Date newDate, int newGuests) {

		try {
			PreparedStatement ps = con
					.prepareStatement("UPDATE order SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
			ps.setDate(1, new java.sql.Date(newDate.getTime()));
			// ps.setDate(1, newDate);
			ps.setInt(2, newGuests);
			ps.setInt(3, orderNumber);
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
	
	


