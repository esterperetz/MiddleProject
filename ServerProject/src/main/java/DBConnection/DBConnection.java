package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

	private static DBConnection instance;
	private Connection connection;

	private static String dbUrl;
	private static String dbUser;
	private static String dbPass;
	private static boolean conn_established = false;

	private DBConnection() {
	}

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
        
//		Class.forName("com.mysql.cj.jdbc.Driver");
		instance.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
		createTableSubscriber(instance.connection);
		createTableEmployee(instance.connection);
		createTableTables(instance.connection); 
		createTableOrder(instance.connection);
		createTableWaitingList(instance.connection);
		createTableOpeningHours(instance.connection); // תיקון: נוסף לאתחול
		conn_established = true;
		System.out.println("Single persistent DB Connection established successfully.");
	}

	public static synchronized DBConnection getInstance() {
		if (instance == null) {
			instance = new DBConnection();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed() || !connection.isValid(10)) {
			System.out.println("Connection is stale or closed. Re-establishing connection.");
			try {
				this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
				System.out.println("Connection re-established successfully.");
			} catch (SQLException e) {
				System.err.println("Could not re-establish DB connection.");
				throw e;
			}
		}
		return connection;
	}

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

	public static void createTableSubscriber(Connection con1) {
		Statement stmt;
		String sql = "CREATE TABLE IF NOT EXISTS Customer (" 
		           + "customer_id INT NOT NULL AUTO_INCREMENT, "
		           + "subscriber_code INT DEFAULT NULL, "
		           + "customer_name VARCHAR(100) NOT NULL, "
		           + "phone_number VARCHAR(255) NOT NULL, "
		           + "email VARCHAR(40) , "
		           + "customer_type ENUM('REGULAR', 'SUBSCRIBER') NOT NULL, " 
		           + "PRIMARY KEY (customer_id), "
		           + "UNIQUE (subscriber_code)"
		           + ");";
		try {
			stmt = con1.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void createTableOrder(Connection con1) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS `order`(" +
	            "order_number INT NOT NULL AUTO_INCREMENT, " +
	            "order_date DATETIME NOT NULL, " +
	            "number_of_guests INT NOT NULL, " +
	            "confirmation_code INT NOT NULL, " +
	            "customer_id INT, " + 
	            "table_number INT DEFAULT NULL, " +
	            "date_of_placing_order DATETIME NOT NULL, " +
	            "arrival_time DATETIME, " +             
	            "leaving_time DATETIME, " + 
	            "total_price DECIMAL(10, 2), " +
	            "order_status ENUM('APPROVED', 'SEATED', 'PAID', 'CANCELLED') NOT NULL, " +
	            "PRIMARY KEY (order_number), " +
	            "CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE RESTRICT ON UPDATE CASCADE, " +
	            "CONSTRAINT fk_order_table FOREIGN KEY (table_number) REFERENCES tables(table_number) ON DELETE SET NULL" +
	            ");";
	    try {
	        stmt = con1.createStatement();
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	public static void createTableEmployee(Connection con) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS employees (" +
	                 "employee_id INT NOT NULL AUTO_INCREMENT, " +
	                 "user_name VARCHAR(100) NOT NULL UNIQUE, " +
	                 "password VARCHAR(255) NOT NULL, " + 
	                 "phone_number VARCHAR(20), " +
	                 "email VARCHAR(100), " +
	                 "role ENUM('MANAGER', 'REPRESENTATIVE') NOT NULL, " +
	                 "PRIMARY KEY (employee_id)" +
	                 ");";
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static void createTableTables(Connection con) {
	    String sql = "CREATE TABLE IF NOT EXISTS tables (" +
	                 "table_number INT PRIMARY KEY, " +
	                 "number_of_seats INT NOT NULL, " +
	                 "is_occupied TINYINT(1) DEFAULT 0);";
	    try (Statement stmt = con.createStatement()) {
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	public static void createTableWaitingList(Connection con) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS waiting_list (" +
	                 "waiting_id INT NOT NULL AUTO_INCREMENT, " +
	                 "customer_id INT , " +
	                 "identification_details VARCHAR(255) NOT NULL, " +
	                 "full_name VARCHAR(255) NOT NULL, " +
	                 "number_of_guests INT NOT NULL, " +
	                 "enter_time DATETIME NOT NULL, " +
	                 "confirmation_code INT NOT NULL, " +
	                 "PRIMARY KEY (waiting_id), " +
	                 "CONSTRAINT fk_waiting_customer FOREIGN KEY (customer_id) " +
	                 "REFERENCES Customer(customer_id) ON DELETE RESTRICT ON UPDATE CASCADE" +
	                 ");";
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static void createTableOpeningHours(Connection con) {
	    Statement stmt;
	    String sql = "CREATE TABLE IF NOT EXISTS opening_hours (" +
	                 "id INT NOT NULL AUTO_INCREMENT, " +
	                 "day_of_week INT DEFAULT NULL, " +
	                 "special_date DATE DEFAULT NULL, " +
	                 "open_time TIME DEFAULT NULL, " +
	                 "close_time TIME DEFAULT NULL, " +
	                 "is_closed TINYINT(1) DEFAULT 0, " +
	                 "PRIMARY KEY (id)" +
	                 ");";
	    try {
	        stmt = con.createStatement();
	        stmt.executeUpdate(sql);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}