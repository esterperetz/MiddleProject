package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * * Singleton class responsible for managing a single persistent connection
 * to a MySQL database. 
 *
 * This class ensures:
 *  - Only one Connection object exists throughout the server lifetime.
 *  - Reconnection happens automatically if the connection becomes invalid.
 *  - Initialization is performed once with provided credentials.
 */
public class DBConnection {

    private static DBConnection instance;
    private Connection connection; 
    
    private static String dbUrl; 
    private static String dbUser; 
    private static String dbPass; 

    
    /**
     * Private constructor to enforce the Singleton pattern.
     * Prevents creation of multiple DBConnection instances.
     */
    private DBConnection() {} 

   
    /**
     *Initializes the Singleton instance and establishes the initial database connection.
	 * This method MUST be called once when the server starts and before any call to getInstance().
	 * @param host   The database server host/IP (e.g., "localhost").
	 * @param schema The database schema name.
	 * @param user   The database username.
	 * @param pass   The database password.
	 * @throws SQLException           If the connection to the database fails.
	 * @throws ClassNotFoundException If the MySQL JDBC driver is not found.
	 * Behavior:
	 *  - Prevents re-initialization if instance already exists.
	 *  - Builds the JDBC URL and stores credentials for possible reconnection.
	 *  - Loads JDBC driver and creates the persistent connection.
     */
    public static synchronized void initializeConnection(String host, String schema, String user, String pass) throws SQLException, ClassNotFoundException {
        if (instance != null) {
            System.out.println("DBConnection already initialized. Re-initialization ignored.");
            return; 
        }
        
        // Build the JDBC URL.
        String url = String.format("jdbc:mysql://%s:3306/%s?serverTimezone=Asia/Jerusalem&useSSL=false", host, schema);
        
        // Store credentials statically for potential reconnection.
        dbUrl = url;
        dbUser = user;
        dbPass = pass;
        
        
        instance=getInstance();
        // Establish connection.
        Class.forName("com.mysql.cj.jdbc.Driver");
        instance.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        System.out.println("Single persistent DB Connection established successfully.");
    }
    
    /**
     * Returns the Singleton DBConnection instance.
	 * If called before initializeConnection(), a new empty instance is created.
	 * This fallback prevents a crash but DOES NOT automatically establish a connection..
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
        	instance=new DBConnection();	
        }
        return instance;
    }

    /**
     * @return The active (or re-established) Connection object.
	 * @throws SQLException If reconnection fails.
     * Retrieves the persistent connection, attempting to re-establish it if closed or invalid.
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
    
    /**
     * Closes the single connection (called on server shutdown).
     */
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
}