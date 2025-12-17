package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import DBConnection.DBConnection;
import Entities.Subscriber;

public class SubscriberDAO {

	public boolean createSubscriber(Subscriber subscriber) throws SQLException {
		String sql = "INSERT INTO subscribers (first_name, last_name, username, phone_number, email) VALUES (?, ?, ?, ?, ?)";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			stmt.setString(1, subscriber.getFirstName());
			stmt.setString(2, subscriber.getLastName());
			stmt.setString(3, subscriber.getUsername());
			stmt.setString(4, subscriber.getPhoneNumber());
			stmt.setString(5, subscriber.getEmail());

			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected > 0) {
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					subscriber.setId(rs.getInt(1));
				}
				return true;
			}
			return false;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public Subscriber getSubscriberById(int id) throws SQLException {
		String sql = "SELECT * FROM subscribers WHERE subscriber_id = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();

			if (rs.next()) {
				Subscriber s = new Subscriber(rs.getString("first_name"), rs.getString("last_name"),
						rs.getString("username"), rs.getString("phone_number"), rs.getString("email"));
				s.setId(rs.getInt("subscriber_id"));
				return s;
			}
			return null;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public Subscriber getSubscriberByUsername(String username) throws SQLException {
		String sql = "SELECT * FROM subscribers WHERE username = ?";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);
			stmt.setString(1, username);
			rs = stmt.executeQuery();

			if (rs.next()) {
				Subscriber s = new Subscriber(rs.getString("first_name"), rs.getString("last_name"),
						rs.getString("username"), rs.getString("phone_number"), rs.getString("email"));
				s.setId(rs.getInt("subscriber_id"));
				return s;
			}
			return null;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}

	public boolean updateSubscriberDetails(Subscriber subscriber) throws SQLException {
		String sql = "UPDATE subscribers SET first_name = ?, last_name = ?, phone_number = ?, email = ? WHERE subscriber_id = ?";
		Connection con = null;
		PreparedStatement stmt = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);

			stmt.setString(1, subscriber.getFirstName());
			stmt.setString(2, subscriber.getLastName());
			stmt.setString(3, subscriber.getPhoneNumber());
			stmt.setString(4, subscriber.getEmail());
			stmt.setInt(5, subscriber.getId());

			return stmt.executeUpdate() > 0;

		} finally {
			if (stmt != null)
				stmt.close();
		}
	}

	public List<Subscriber> getAllSubscribers() throws SQLException {
		String sql = "SELECT * FROM subscribers";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			con = DBConnection.getInstance().getConnection();
			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();

			List<Subscriber> subscribers = new ArrayList<>();

			while (rs.next()) {
				Subscriber s = new Subscriber(rs.getString("first_name"), rs.getString("last_name"),
						rs.getString("username"), rs.getString("phone_number"), rs.getString("email"));
				s.setId(rs.getInt("subscriber_id"));
				subscribers.add(s);
			}
			return subscribers;
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
		}
	}
}