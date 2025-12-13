package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import DBConnection.DBConnection;
import Entities.Order;

public class OrderDAO {

	/**
	 * @return List of all the orders
	 * @throws SQLException
	 */
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
				Order o = new Order(rs.getInt("order_number"), rs.getDate("order_date"), rs.getInt("number_of_guests"),
						rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
						rs.getDate("date_of_placing_order"));
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

	/**
	 * @param id
	 * @return order according the id_order(promery key in DB)
	 * @throws SQLException
	 */
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
				return new Order(rs.getInt("order_number"), rs.getDate("order_date"), rs.getInt("number_of_guests"),
						rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
						rs.getDate("date_of_placing_order"));
			}
			return null;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	/**
	 * @param o
	 * @return true if we success to create new order
	 * @throws SQLException
	 */
	public boolean createOrder(Order o) throws SQLException {
		String sql = "INSERT INTO `order`(" + "order_date, number_of_guests, confirmation_code, "
				+ "subscriber_id, date_of_placing_order" + ") VALUES (?, ?, ?, ?, ?)";
		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);

			stmt.setDate(1, new java.sql.Date(o.getOrder_date().getTime()));
			stmt.setInt(2, o.getNumber_of_guests());
			stmt.setInt(3, o.getConfirmation_code());
			stmt.setInt(4, o.getSubscriber_id());
			stmt.setDate(5, new java.sql.Date(o.getDate_of_placing_order().getTime()));

			return stmt.executeUpdate() > 0;
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}


	/**
	 * @param o
	 * @return update the order,return true if we succeeded ,false if we not 
	 * @throws SQLException
	 */
	public boolean updateOrder(Order o) throws SQLException {
		String sql = "UPDATE `order` SET " + "order_date = ?, number_of_guests = ?, confirmation_code = ?, "
				+ "subscriber_id = ?, date_of_placing_order = ? " + "WHERE order_number = ?";
		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);

			stmt.setDate(1, new java.sql.Date(o.getOrder_date().getTime()));
			stmt.setInt(2, o.getNumber_of_guests());
			stmt.setInt(3, o.getConfirmation_code());
			stmt.setInt(4, o.getSubscriber_id());
			stmt.setDate(5, new java.sql.Date(o.getDate_of_placing_order().getTime()));

			stmt.setInt(6, o.getOrder_number());

			return stmt.executeUpdate() > 0;
		} finally {
			if (stmt != null)
				stmt.close();
		}
	}


	/**
	 * @param id
	 * @return true if we deleted,false if we dont
	 * @throws SQLException
	 */
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
}