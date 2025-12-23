package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * * Singleton class responsible for managing a single persistent connection to
 * a MySQL database.
 *
 * This class ensures: - Only one Connection object exists throughout the server
 * lifetime. - Reconnection happens automatically if the connection becomes
 * invalid. - Initialization is performed once with provided credentials.
 */
public class DBConnection {

	private static DBConnection instance;
	private Connection connection;

	private static String dbUrl;
	private static String dbUser;
	private static String dbPass;
	private static boolean conn_established = false;

	private DBConnection() {
	}

	// Initializes the Singleton instance and establishes the initial database
	// connection.
	public static synchronized void initializeConnection(String host, String schema, String user, String pass)
			throws SQLException, ClassNotFoundException {
		if (instance != null && conn_established == true) {
			System.out.println("DBConnection already initialized. Re-initialization ignored.");
			return;
		}

		String url = String.format("jdbc:mysql://%s:3306/%s?serverTimezone=Asia/Jerusalem&useSSL=false", host, schema);
		dbUrl = url;
		dbUser = user;
		dbPass = pass;
		instance = getInstance();
        
		Class.forName("com.mysql.cj.jdbc.Driver");
		instance.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
		createTableSubscriber(instance.connection);
		createTableOrder(instance.connection);
		conn_established = true;
		System.out.println("Single persistent DB Connection established successfully.");
	}

	// Returns the Singleton DBConnection instance.
	public static synchronized DBConnection getInstance() {
		if (instance == null) {
			instance = new DBConnection();
		}
		return instance;
	}

	/**
	 * @return The active (or re-established) Connection object. Retrieves the
	 *         persistent connection, attempting to re-establish it if closed or
	 *         invalid.
	 */
	public Connection getConnection() throws SQLException {
		// Check if connection is closed or invalid.
		if (connection == null || connection.isClosed() || !connection.isValid(10)) {
			System.out.println("Connection is stale or closed. Re-establishing connection.");
			try {
				// Re-establish connection using stored credentials.
				this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
				
				System.out.println("Connection re-established successfully.");
			} catch (SQLException e) {
				System.err.println("Could not re-establish DB connection.");
				throw e;
			}
		}
		return connection;
	}

	// Closes the single connection (called on server shutdown).
	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
				System.out.println("Single persistent DB connection closed.");
			} catch (SQLException e) {
				System.err.println("Error closing DB connection: " + e.getMessage());
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// VERIFY that the calls to these functions are UNCOMMENTED in initializeConnection() //
	//                                                                                   //
	////////////////////////////////////////////////////////////////////////////////////////
	public static void createTableSubscriber(Connection con1) {
		Statement stmt;
		String sql = "CREATE TABLE IF NOT EXISTS bistro.subscriber (" + "subscriber_id INT NOT NULL AUTO_INCREMENT, "
				+ "subscriber_name VARCHAR(100) NOT NULL, "
				+ "phone_number VARCHAR(255) UNIQUE NOT NULL, " + "email VARCHAR(20), " + "PRIMARY KEY (subscriber_id)"
				+ ");";
		try {
			stmt = con1.createStatement();
			stmt.executeUpdate(sql);
//		stmt.executeUpdate("load data local infile \"courses.txt\" into table courses");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static void createTableOrder(Connection con1) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS bistro.order(" +
	            "order_number INT NOT NULL AUTO_INCREMENT, " +
	            "order_date DATETIME NOT NULL, " +
	            "number_of_guests INT NOT NULL, " +
	            "confirmation_code INT NOT NULL, " +
	            "subscriber_id INT DEFAULT NULL, " + 
	            "date_of_placing_order DATETIME NOT NULL, " +
	            "client_name VARCHAR(255) NOT NULL, " +  
	            "client_email VARCHAR(255) NOT NULL, " + 
	            "client_phone VARCHAR(20) NOT NULL, " +  
	            "arrival_time DATETIME, " +             
	            "leaving_time DATETIME, " + // New field
	            "total_price DECIMAL(10, 2), " +
	            "order_status ENUM('APPROVED', 'SEATED', 'PAID', 'CANCELLED') NOT NULL, " +
	            "PRIMARY KEY (order_number), " +
	            "CONSTRAINT fk_order_subscriber FOREIGN KEY (subscriber_id) REFERENCES bistro.subscriber(subscriber_id) ON DELETE SET NULL ON UPDATE CASCADE" +
	            ");";
	    try {
	        stmt = con1.createStatement();
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) { e.printStackTrace(); }
	}
	public static void createTableEmployee(Connection con) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS bistro.employees (" +
	                 "employee_id INT NOT NULL AUTO_INCREMENT, " +
	                 "user_name VARCHAR(100) NOT NULL UNIQUE, " +
	                 "password VARCHAR(255) NOT NULL, " + 
	                 "phone_number VARCHAR(20), " +
	                 "email VARCHAR(100), " +
	                 "role ENUM('MANAGER', 'REPRESENTATIVE') NOT NULL, " +
	                 "is_logged_in BOOLEAN DEFAULT FALSE, " +
	                 "PRIMARY KEY (employee_id)" +
	                 ");";
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(sql);
	        System.out.println("Table 'employees' is ready.");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	

}