package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//CONNECTION TO DB ONLY//
public class DBConnection {

    private Connection con;

    public DBConnection(String user, String pass, String scheme) throws SQLException {
        this.con = connectToDB(user, pass, scheme);
    }

    private Connection connectToDB(String user, String pass, String scheme) throws SQLException {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + scheme +
                    "?serverTimezone=Asia/Jerusalem&useSSL=false",
                    user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Connection getConnection() throws SQLException {
    	
        if (con == null || con.isClosed()) {
            throw new SQLException("Connection is not available");
        }
        return con;
        
    	//when we close Connection we dont have active connection check if we need to close
    	
    	
    }

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }
}

	


