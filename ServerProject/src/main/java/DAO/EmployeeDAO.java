package DAO;

import java.sql.*;
import DBConnection.DBConnection;
import entities.Employee;
import entities.Subscriber;
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
//				updateLoginStatus(empId, true);

				// Create and populate the updated Employee entity
				Employee emp = new Employee(rs.getString("user_name"), rs.getString("password"),rs.getString("phone_number"),rs.getString("Email"),Role.valueOf(rs.getString("role")));
				emp.setEmployeeId(empId);
				return emp;
			}
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
		}catch(Exception e) {
			System.out.println("user name is already taken ");
		}
		return null;
	}

	/** Inserts a new employee into the DB. */
	public boolean createEmployee(Employee emp) throws SQLException {
		String query = "INSERT INTO employees (user_name, password,phone_number,email, role) VALUES (?,?,?, ?, ?)";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)) {

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
		} catch (SQLException e) {
			System.out.println("Error creating employee: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean updateEmployeeDetails(Employee employee) {
		String query = "UPDATE employees SET  password = ?, phone_number = ?, email = ? WHERE user_name = ?";
		try (Connection con = DBConnection.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, employee.getPassword());
			ps.setString(2, employee.getPhoneNumber());
			ps.setString(3, employee.getEmail());
			ps.setString(4, employee.getUserName());

			int rowsAffected = ps.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			System.out.println("Error updating employee: " + e.getMessage());
			e.printStackTrace();
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
		}
	}
}