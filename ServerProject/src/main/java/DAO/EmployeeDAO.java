package DAO;

import java.sql.*;
import DBConnection.DBConnection;
import entities.Customer;
import entities.Employee;
import entities.Employee.Role;

/**
 * Data Access Object (DAO) for managing Employee data.
 * 
 * Handles authentication, creation, updates, and deletion of employee records,
 * as well as subscriber creation which is an employee-privileged action.
 */
public class EmployeeDAO {

    /**
     * Authenticates an employee against the database.
     * Verifies username and password, and if successful, returns a fully populated Employee object.
     *
     * @param userName The username provided during login.
     * @param password The password provided during login.
     * @return An Employee object if credentials are valid, null otherwise.
     * @throws SQLException If a database error occurs.
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

    /**
     * Checks if a given username already exists in the database.
     * Used during the registration process to prevent duplicate usernames.
     *
     * @param userName The username to check.
     * @return An Employee object if the username is taken, null otherwise.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Creates a new subscriber (Customer) in the database.
     * Generates a new customer ID and updates the passed Customer object.
     *
     * @param customer The Customer object containing subscriber details.
     * @return true if the subscriber was successfully created.
     */
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

    /**
     * Inserts a new employee record into the database.
     * Retrieves the generated employee ID and updates the Employee object.
     *
     * @param emp The Employee object to create.
     * @return true if the employee was successfully added.
     * @throws SQLException If a database error occurs.
     */
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

    /**
     * Updates an existing employee's details (password, phone, email).
     * The update is performed based on the username.
     *
     * @param employee The Employee object with updated details.
     * @return true if the update was successful.
     */
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

    /**
     * Removes an employee record from the database by ID.
     *
     * @param id The ID of the employee to delete.
     * @return true if the deletion was successful.
     * @throws SQLException If a database error occurs.
     */
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