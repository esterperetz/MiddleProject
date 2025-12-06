package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private Connection connection; 
    
    private static String dbUrl; 
    private static String dbUser; 
    private static String dbPass; 

    // Private constructor to enforce Singleton pattern.
    private DBConnection() {} 

    /**
     * Initializes the Singleton instance and establishes the DB connection using provided credentials.
     * MUST be called once before any call to getInstance().
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
        
        //instance = new DBConnection();
        instance=getInstance();
        // Establish connection.
        Class.forName("com.mysql.cj.jdbc.Driver");
        instance.connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        System.out.println("Single persistent DB Connection established successfully.");
    }
    
    /**
     * Returns the single instance of DBConnection. Throws an exception if not initialized.
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            // Fails safe if called before initialization in ServerLoginController.
            //throw new IllegalStateException("DBConnection is not initialized. Call initializeConnection() first.");
        	instance=new DBConnection();	
        }
        return instance;
    }

    /**
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