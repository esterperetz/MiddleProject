package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

	private static DBConnection instance;
	private java.util.concurrent.BlockingQueue<Connection> connectionPool;
	private static final int POOL_SIZE = 10;

	private static String dbUrl;
	private static String dbUser;
	private static String dbPass;
	private static boolean conn_established = false;

	private DBConnection() {
		connectionPool = new java.util.concurrent.LinkedBlockingQueue<>(POOL_SIZE);
	}

	public static synchronized void initializeConnection(String host, String schema, String user, String pass)
			throws SQLException, ClassNotFoundException {
		if (instance != null && conn_established) {
			System.out.println("DBConnection already initialized.");
			return;
		}

		String url = String.format("jdbc:mysql://%s:3306/%s?serverTimezone=Asia/Jerusalem&useSSL=false", host, schema);
		dbUrl = url;
		dbUser = user;
		dbPass = pass;

		if (instance == null) {
			instance = new DBConnection();
		}

		// Initialize pool with connections
		try {
			for (int i = 0; i < POOL_SIZE; i++) {
				Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
				if (!instance.connectionPool.offer(con)) {
					con.close();
				}
			}

			// Use a temporary connection for table creation to avoid draining the pool
			// immediately
			try (Connection setupCon = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
				createTableSubscriber(setupCon);
				createTableEmployee(setupCon);
				createTableTables(setupCon);
				createTableOrder(setupCon);
				createTableWaitingList(setupCon);
				createTableOpeningHours(setupCon);
				insertIntoTableOpeningHours(setupCon);
//				createOpeningHoursTables(setupCon);
			}

			conn_established = true;
			System.out.println("DB Connection Pool initialized with " + POOL_SIZE + " connections.");
		} catch (SQLException e) {
			throw new SQLException("Failed to initialize connection pool", e);
		}
	}

	public static synchronized DBConnection getInstance() {
		if (instance == null) {
			instance = new DBConnection();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		try {
			Connection con = connectionPool.poll(5, java.util.concurrent.TimeUnit.SECONDS);
			if (con == null) {
				throw new SQLException("Database connection timeout - pool is exhausted.");
			}

			if (!con.isValid(2)) {
				System.out.println("Connection INVALID. Replacing...");
				try {
					con.close();
				} catch (Exception ignored) {
				}
				con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
			}
			return con;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SQLException("Interrupted while waiting for connection", e);
		}
	}

	public void releaseConnection(Connection con) {
		if (con != null) {
			try {
				if (!con.isClosed()) {
					boolean returned = connectionPool.offer(con);
					if (!returned) {
						// Pool is full (shouldn't happen if logic is correct), close it
						con.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void closeConnection() {
		// Shutdown pool
		Connection con;
		while ((con = connectionPool.poll()) != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("DB Connection pool closed.");
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
				+ "UNIQUE (subscriber_code), "
				+ "UNIQUE (email)"
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
				"reminder_sent BOOLEAN DEFAULT FALSE, " +
				"PRIMARY KEY (order_number), " +
				"CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE RESTRICT ON UPDATE CASCADE, "
				+
				"CONSTRAINT fk_order_table FOREIGN KEY (table_number) REFERENCES tables(table_number) ON DELETE SET NULL"
				+
				");";
		try {
			stmt = con1.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void createTableWaitingList(Connection con) {
		Statement stmt;
		String sql = "CREATE TABLE IF NOT EXISTS waiting_list (" +
		        "waiting_id INT NOT NULL AUTO_INCREMENT, " +
		        "customer_id INT , " +
		        "number_of_guests INT NOT NULL, " +
		        "enter_time DATETIME NOT NULL, " +
		        "confirmation_code INT NOT NULL, " +
		        "in_waiting_list TINYINT(1) DEFAULT 1, " +
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
	
	public static void createOpeningHoursTables(Connection con) {
	    Statement stmt;
	    try {
	        stmt = con.createStatement();

	        // --- 1. טבלת השגרה (Weekly Schedule) ---
	        String sqlWeekly = "CREATE TABLE IF NOT EXISTS opening_hours (" +
	                "day_of_week INT NOT NULL, " +
	                "open_time TIME, " +
	                "close_time TIME, " +
	                "is_closed TINYINT(1) DEFAULT 0, " +
	                "PRIMARY KEY (day_of_week)" +
	                ");";
	        stmt.executeUpdate(sqlWeekly);

	        // --- 2. טבלת החריגים (Special Events) ---
	        String sqlSpecial = "CREATE TABLE IF NOT EXISTS special_events (" +
	                "event_date DATE NOT NULL, " +
	                "open_time TIME, " +
	                "close_time TIME, " +
	                "is_closed TINYINT(1) DEFAULT 0, " +
	                "description VARCHAR(255), " +
	                "PRIMARY KEY (event_date)" +
	                ");";
	        stmt.executeUpdate(sqlSpecial);

	      
	        String initData = "INSERT IGNORE INTO weekly_schedule (day_of_week, open_time, close_time, is_closed) VALUES " +
	                "(1, '08:00:00', '23:00:00', 0), " + // Sunday
	                "(2, '08:00:00', '23:00:00', 0), " + // Monday
	                "(3, '08:00:00', '23:00:00', 0), " + // Tuesday
	                "(4, '08:00:00', '23:00:00', 0), " + // Wednesday
	                "(5, '08:00:00', '23:00:00', 0), " + // Thursday
	                "(6, '08:00:00', '14:00:00', 0) ";   // Friday

	        
	        stmt.executeUpdate(initData);
	        
	        System.out.println("Tables 'weekly_schedule' and 'special_events' created/verified successfully.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static void insertIntoTableOpeningHours(Connection con) {
		Statement stmt;
		try {
			stmt = con.createStatement();

			String checkSql = "SELECT COUNT(*) FROM opening_hours";
			var rs = stmt.executeQuery(checkSql);

			if (rs.next()) {
				int rowCount = rs.getInt(1);
				if (rowCount > 0) {
					// אם יש כבר שורות, אנחנו לא עושים כלום ויוצאים
					System.out.println("Opening hours data already exists. Skipping insert.");
					return;
				}
			}

			String sql = "INSERT INTO opening_hours (day_of_week, special_date, open_time, close_time, is_closed) VALUES "+
					"(1, '2026-01-12', '17:00:00', '23:59:59', 0),"+
					"(1, '2026-01-13', '12:00:00', '17:00:00', 0),"+
					"(1, NULL, '17:00:00', '00:00:00', 0),"+
					"(2, NULL, '17:00:00', '00:00:00', 0),"+
					"(3, NULL, '17:00:00', '00:00:00', 0),"+
					"(4, NULL, '17:00:00', '00:00:00', 0),"+
					"(5, NULL, '17:00:00', '00:00:00', 0),"+
					"(6, NULL, '17:00:00', '00:00:00', 0),"+
					"(7, NULL, NULL, NULL, 1)";

			int rowsAffected = stmt.executeUpdate(sql);
			System.out.println("Default opening hours inserted successfully.");

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error inserting into opening_hours: " + e.getMessage());
		}
	}
}