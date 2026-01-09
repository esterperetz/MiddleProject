package DAO;

import java.sql.*;
import DBConnection.DBConnection;
import entities.Customer;
import entities.Employee;
import entities.Employee.Role;

public class EmployeeDAO {

	/**
	 * Authenticates employee and populates all fields including ID and Role.
	 * Updates login status in DB to prevent multiple sessions.
	 */

	public Employee login(String userName, String password) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "SELECT * FROM employees WHERE user_name = ? AND password = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, userName);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				// Update login status
				int empId = rs.getInt("employee_id");
				// updateLoginStatus(empId, true);

				// Create and populate the updated Employee entity
				Employee emp = new Employee(rs.getString("user_name"), rs.getString("password"),
						rs.getString("phone_number"), rs.getString("Email"), Role.valueOf(rs.getString("role")));
				emp.setEmployeeId(empId);
				return emp;
			}
		} finally {
			DBConnection.getInstance().releaseConnection(conn);
		}
		return null;
	}

	public Employee checkIfUsernameIsAlreadyTaken(String userName) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "SELECT * FROM employees WHERE user_name = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, userName);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {

				// Update login status
				int empId = rs.getInt("employee_id");

				// Create and populate the updated Employee entity
				Employee emp = new Employee(rs.getString("user_name"), rs.getString("password"));
				emp.setEmployeeId(empId);
				emp.setRole(Role.valueOf(rs.getString("role")));
				return emp;
			}
		} catch (Exception e) {
			System.out.println("user name is already taken ");
		} finally {
			DBConnection.getInstance().releaseConnection(conn);
		}
		return null;
	}

	// Creates a new subscriber in the DB and updates the object's ID
	public boolean createSubscriber(Customer customer) {
		String query = "INSERT INTO Customer (subscriber_code,customer_name, phone_number, email , customer_type ) VALUES (?,?, ?, ?, ?)";
		Connection con = null;
		try {
			con = DBConnection.getInstance().getConnection();
			try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				Integer subCode = customer.getSubscriberCode();
				// אם יש מספר, מכניסים אותו
				ps.setInt(1, subCode);
				ps.setString(2, customer.getName());
				ps.setString(3, customer.getPhoneNumber());
				ps.setString(4, customer.getEmail());
				ps.setString(5, customer.getType().getString());

				int rowsAffected = ps.executeUpdate();

				if (rowsAffected > 0) {
					ResultSet generatedKeys = ps.getGeneratedKeys(); // returns id number to ps
					if (generatedKeys.next()) {
						customer.setCustomerId(generatedKeys.getInt(1));
					}

					return true;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error creating subscriber: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
		return false;
	}

	/** Inserts a new employee into the DB. */
	public boolean createEmployee(Employee emp) throws SQLException {
		String query = "INSERT INTO employees (user_name, password,phone_number,email, role) VALUES (?,?,?, ?, ?)";
		Connection con = null;
		try {
			con = DBConnection.getInstance().getConnection();
			try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

				ps.setString(1, emp.getUserName());
				ps.setString(2, emp.getPassword());
				ps.setString(3, emp.getPhoneNumber());
				ps.setString(4, emp.getEmail());
				ps.setString(5, emp.getRole().getRoleValue());

				int rowsAffected = ps.executeUpdate();

				if (rowsAffected > 0) {
					ResultSet generatedKeys = ps.getGeneratedKeys(); // returns id number to ps
					if (generatedKeys.next()) {
						emp.setEmployeeId(generatedKeys.getInt(1));
					}
					return true;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error creating employee: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
		return false;
	}

	public boolean updateEmployeeDetails(Employee employee) {
		String query = "UPDATE employees SET  password = ?, phone_number = ?, email = ? WHERE user_name = ?";
		Connection con = null;
		try {
			con = DBConnection.getInstance().getConnection();
			try (PreparedStatement ps = con.prepareStatement(query)) {
				ps.setString(1, employee.getPassword());
				ps.setString(2, employee.getPhoneNumber());
				ps.setString(3, employee.getEmail());
				ps.setString(4, employee.getUserName());

				int rowsAffected = ps.executeUpdate();
				return rowsAffected > 0;

			}
		} catch (SQLException e) {
			System.out.println("Error updating employee: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBConnection.getInstance().releaseConnection(con);
		}
		return false;
	}

	/** Removes an employee record from the DB. */
	public boolean deleteEmployee(int id) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "DELETE FROM employees WHERE employee_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		} finally {
			DBConnection.getInstance().releaseConnection(conn);
		}
	}
}