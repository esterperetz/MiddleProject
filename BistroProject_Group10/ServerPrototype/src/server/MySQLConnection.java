package server;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import logic.Order;
import logic.ParkingSpot;
import logic.Role;
import logic.subscriber;

/**
 * The class handles any SQL query needed
 */
public class MySQLConnection {
	private Connection con;

	/**
	 * Class Constructor
	 */
	protected MySQLConnection() {
		createDatabaseAndTable();
	}

	/**
	 * @return con
	 */
	public Connection getCon() {
		// Getter of con
		return con;
	}

	/**
	 * @return con
	 */
	private Connection connectToDB() {
		// Start a connection to DB bpark returns con
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
		}
		try {
			Connection conn = DriverManager
					.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=UTC&useSSL=false", "root", "Aa123456");
			System.out.println("DB connection succeed");
			return conn;
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("connection failed");
			return null;
		}

	}

	/**
	 * Connect to the MySQL server (without specifying a database)
	 * Happens at Server Startup
	 * @return Connection object
	 */
	private Connection connectToMySQL() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeeded");
		} catch (Exception ex) {
			System.out.println("Driver definition failed");
			return null;
		}
		try {
			// Connecting to MySQL without specifying a database
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost?serverTimezone=UTC&useSSL=false",
					"root", "Aa123456");
			System.out.println("DB connection succeeded");
			return conn;
		} catch (Exception ex) {
			System.out.println("Connection failed");
			return null;
		}
	}

	/**
	 * @param con
	 */
	private void disconnectFromDB(Connection con) {
		// Closes the connection
		try {
			if (con != null && !con.isClosed()) {
				con.close(); // Close the connection after the operation is complete
				System.out.println("Connection closed after retrieving orders.");
			}
		} catch (SQLException e) {
			System.out.println("Failed to close the connection.");
			e.printStackTrace();
		}
	}

	/**
	 * Create the database if it doesn't exist, and import the SQL file if needed
	 */
	private void createDatabaseAndTable() {
		try {
			// Step 1: Connect to MySQL (without specifying a database)
			con = connectToMySQL();
			if (con == null) {
				throw new SQLException();
			}

			// Step 2: Create the database if it doesn't exist
			String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS bpark";
			Statement stmt = con.createStatement();
			stmt.executeUpdate(createDatabaseSQL);
			System.out.println("Database created or already exists.");

			// Step 3: Reconnect to the bpark database
			stmt.close();
			disconnectFromDB(con);
			con = connectToDB();
			if (con == null) {
				System.out.println("Failed to reconnect to bpark database.");
				return;
			}

			// Step 4: Select the bpark database explicitly
			Statement useStmt = con.createStatement();
			String chooseBpark = "USE bpark;";
			useStmt.execute(chooseBpark);
			System.out.println("Using bpark database.");

			// Step 5: Check if the table exists
			String showOrder = "SHOW TABLES LIKE 'order'";
			ResultSet rs = useStmt.executeQuery(showOrder);
			if (!rs.next()) {
				System.out.println("Table 'order' does not exist. Proceeding with import.");
				// If table does not exist, import the SQL file
				importSQLFile();
			} else {
				System.out.println("Table 'order' already exists. Skipping import.");
			}

			rs.close();
			useStmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to create database or table.");
		} finally {
			disconnectFromDB(con);
		}
	}

	/**
	 * Import SQL file to create table and insert data
	 */
	private void importSQLFile() {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("bpark_order.sql");
			if (is == null) {
				throw new Exception("SQL file not found in classpath.");
			}
			Scanner scanner = new Scanner(is, "UTF-8").useDelimiter(";");
			Statement stmt = con.createStatement();
			while (scanner.hasNext()) {
				String line = scanner.next().trim();
				if (!line.isEmpty()) {
					stmt.execute(line);
				}
			}
			scanner.close();
			stmt.close();
			System.out.println("SQL file imported successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to import SQL file.");
		}
	}

	/**
	 * @return ordersList
	 * The method returns an ArrayList<Order> of all orders in DB
	 */
	public ArrayList<Order> getallordersfromDB() {
		ArrayList<Order> orderslist = new ArrayList<>();
		try {
			con = connectToDB();
			if (con == null)
				throw new SQLException();
			String table = "SELECT* FROM `order`; ";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(table);
			while (rs.next()) {
				int order_number = rs.getInt("order_number");
				int parking_space = rs.getInt("parking_space");
				Date order_date = rs.getDate("order_date");
				int confirmation_code = rs.getInt("confirmation_code");
				int subscriber_id = rs.getInt("subscriber_id");
				Date placing_date = rs.getDate("date_of_placing_an_order");
				subscriber sub = new subscriber(subscriber_id, "", "", "", Role.SUBSCRIBER, null, 0);
				ParkingSpot spot = new ParkingSpot(parking_space, null, null);

				// Create Order
				Order temp = new Order(confirmation_code, // code
						sub, // subscriber
						order_date, // order_date
						placing_date, // date_of_placing_an_order
						spot, // parking_space
						order_number // order_id
				);
				orderslist.add(temp);
			}
			rs.close();
			stmt.close();
			System.out.println("got all orders");
		} catch (SQLException e) {
			System.out.println("Failed to get all orders");
		} finally {
			disconnectFromDB(con);
		}
		return orderslist;

	}

	/**
	 * @return ordersList
	 * The method returns an order of a received order number in DB
	 */
	public Order getOrderFromDB(String id) {
		Order temp = null;
		try {
			con = connectToDB();
			if (con == null)
				throw new SQLException();
			String order = "SELECT* FROM `order` WHERE order_number = ?;";
			PreparedStatement stmt = con.prepareStatement(order);
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			
			if (rs.next()) {
				int order_number = rs.getInt("order_number");
				int parking_space = rs.getInt("parking_space");
				Date order_date = rs.getDate("order_date");
				int confirmation_code = rs.getInt("confirmation_code");
				int subscriber_id = rs.getInt("subscriber_id");
				Date placing_date = rs.getDate("date_of_placing_an_order");
				subscriber sub = new subscriber(subscriber_id, "", "", "", Role.SUBSCRIBER, null, 0);
				ParkingSpot spot = new ParkingSpot(parking_space, null, null);

				// Create Order
				temp = new Order(confirmation_code, // code
						sub, // subscriber
						order_date, // order_date
						placing_date, // date_of_placing_an_order
						spot, // parking_space
						order_number // order_id
				);
			}
			rs.close();
			stmt.close();
			System.out.println("got order: " + id);
		} catch (SQLException e) {
			System.out.println("Failed to get order");
		} finally {
			disconnectFromDB(con);
		}
		return temp;

	}

	/**
	 * @param order
	 * Gets an order and updates it in the DB
	 */
	public void updateDB(Order order) {
		try {
			con = connectToDB();
			if (con == null)
				throw new SQLException();
			String update_order = "UPDATE `order` SET parking_space = ?, order_date = ? WHERE order_number = ?";
			PreparedStatement stmt = con.prepareStatement(update_order);
			stmt.setInt(1, order.get_ParkingSpot().getSpotId());
			Date sqlDate = order.getorder_date();
			// Convert Date back to java.sql.Date
			stmt.setDate(2, realDate(sqlDate));
			stmt.setInt(3, order.get_order_id());
			stmt.executeUpdate();
			stmt.close();
			System.out.println("Updated");
		} catch (SQLException e) {
			System.out.println("Failed to Update");
		} finally {
			disconnectFromDB(con);
		}
	}
	
	/**
	 * @param date
	 * @return java.sql.Date
	 * Converts java.util.Date to java.sql.Date
	 */
	private java.sql.Date realDate(Date date){	
		java.util.Date utilDate = new java.util.Date(date.getTime()); 

		// Convert java.util.Date to LocalDate
		LocalDate localDate = utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

		// Add one day using LocalDate
		LocalDate newLocalDate = localDate.plusDays(1);
		return java.sql.Date.valueOf(newLocalDate);
	}
}
