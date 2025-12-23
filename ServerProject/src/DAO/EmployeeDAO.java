package DAO;

import java.sql.*;
import DBConnection.DBConnection;
import entities.Employee;
import entities.Employee.Role;

public class EmployeeDAO {

	/**
	 * Authenticates employee and populates all fields including ID and Role.
	 * Updates login status in DB to prevent multiple sessions.
	 */
	public Employee login(String userName, int password) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "SELECT * FROM bistro.employees WHERE user_name = ? AND password = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, userName);
			stmt.setInt(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
//				if (rs.getBoolean("is_logged_in"))
//					return null;

				// Update login status
				int empId = rs.getInt("employee_id");
//				updateLoginStatus(empId, true);

				// Create and populate the updated Employee entity
				Employee emp = new Employee(rs.getString("user_name"), rs.getInt("password"));
				emp.setEmployeeId(empId);
				emp.setRole(Role.valueOf(rs.getString("role")));
				return emp;
			}
		}
		return null;
	}

//	/** Updates is_logged_in status for session management. */
//	public void updateLoginStatus(int employeeId, boolean status) throws SQLException {
//		Connection conn = DBConnection.getInstance().getConnection();
//		String sql = "UPDATE bistro.employees SET is_logged_in = ? WHERE employee_id = ?";
//		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//			stmt.setBoolean(1, status);
//			stmt.setInt(2, employeeId);
//			stmt.executeUpdate();
//		}
//	}

	/** Inserts a new employee into the DB. */
	public boolean createEmployee(Employee emp) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "INSERT INTO bistro.employees (user_name, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, emp.getUserName());
			stmt.setInt(2, emp.getPassword());
			stmt.setString(3, emp.getRole().getRoleValue());
			return stmt.executeUpdate() > 0;
		}
	}

	
	
	/** Removes an employee record from the DB. */
	public boolean deleteEmployee(int id) throws SQLException {
		Connection conn = DBConnection.getInstance().getConnection();
		String sql = "DELETE FROM bistro.employees WHERE employee_id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			return stmt.executeUpdate() > 0;
		}
	}
}