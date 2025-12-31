package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import DBConnection.DBConnection;
import entities.Customer;
import entities.CustomerType;
import entities.Employee.Role;

public class CustomerDAO {


	// Creates a new subscriber in the DB and updates the object's ID
	public boolean createCustomer(Customer customer) {
		String query = "INSERT INTO Customer (customer_name, phone_number, email , customer_type ) VALUES (?, ?, ?, ?)";	
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

			ps.setString(1, customer.getName());
	        ps.setString(2, customer.getPhoneNumber());
	        ps.setString(3, customer.getEmail());
	    
	        ps.setString(3, customer.getType().getString());

			int rowsAffected = ps.executeUpdate();

			if (rowsAffected > 0) {
				ResultSet generatedKeys = ps.getGeneratedKeys(); // returns id number to ps
				if (generatedKeys.next()) {
					customer.setCustomerId(generatedKeys.getInt(1));
				}
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Error creating subscriber: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	// Fetches a subscriber by their unique ID
	public Customer getCustomerBySubscriberCode(int SubscriberCode) {

		String query = "SELECT * FROM Customer WHERE subscriber_code = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, SubscriberCode);
		
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Customer s = new Customer(null,rs.getInt("subscriber_code"), rs.getString("customer_name"),
						rs.getString("phone_number"), rs.getString("email"), CustomerType.valueOf(rs.getString("customer_type")));
//				s.setSubscriberId(rs.getInt("subscriber_id"));
				return s;
				
//				return createSubscriberFromResultSet(rs);
			}
		} catch (SQLException e) {
			System.out.println("Error fetching subscriber by ID: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	// Fetches a subscriber by their username
	public Customer getSubscriberBySubscriberName(String customerName) {
		String query = "SELECT * FROM Customer WHERE customer_name = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, customerName);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return createCustomerFromResultSet(rs);
			}
		} catch (SQLException e) {
			System.out.println("Error fetching subscriber by username: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	// Updates editable details (name, phone, email)
	public boolean updateCustomerDetails(Customer customer) {
		String query = "UPDATE Customer SET  customer_id= ?, subscriber_name = ?, phone_number = ?, email = ? WHERE subscriber_id = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query)) {
			ps.setInt(1, customer.getSubscriberCode());
			ps.setString(2, customer.getName());
			ps.setString(3, customer.getPhoneNumber());
			ps.setString(4, customer.getEmail());
			ps.setString(5, customer.getType().getString());

			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			System.out.println("Error updating subscriber: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	// Returns a list of all subscribers in the system
	public ArrayList<Customer> getAllCustomers() {
		ArrayList<Customer> subscribers = new ArrayList<>();
		String query = "SELECT * FROM Customer WHERE customer_type = SUBSCRIBER";
		try (Connection con = DBConnection.getInstance().getConnection();
				Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				subscribers.add(createCustomerFromResultSet(rs));
			}
		} catch (SQLException e) {
			System.out.println("Error fetching all subscribers: " + e.getMessage());
			e.printStackTrace();
		}
		return subscribers;
	}

	// Helper method to map ResultSet to Subscriber object
	private Customer createCustomerFromResultSet(ResultSet rs) throws SQLException {
		try {
			Customer s = new Customer(null, rs.getInt("subscriber_code"), rs.getString("customer_name"),
					rs.getString("phone_number"), rs.getString("email") , CustomerType.valueOf(rs.getString("customer_type")));
//			s.setSubscriberId(rs.getInt("subscriber_id"));
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}
}