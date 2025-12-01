package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//CONNECTION TO DB ONLY//
public class DBConnection {

    private Connection con;
    private String user , pass , scheme;
    private String url = "jdbc:mysql://localhost:3306/"; // כאן "orders" הוא schema/database


    public DBConnection(String user, String pass, String scheme) throws SQLException {
        this.con = connectToDB(user, pass, scheme);
    }

    private Connection connectToDB(String user, String pass, String scheme) throws SQLException {
        try {
        	this.user = user;
        	this.pass = pass;
        	this.scheme = scheme;
            return DriverManager.getConnection(url + scheme + 
            		"?serverTimezone=Asia/Jerusalem&useSSL=false",user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            throw new SQLException("Connection is not available");
        }
        return DriverManager.getConnection(url + scheme +
                "?serverTimezone=Asia/Jerusalem&useSSL=false",
                user, pass);
    	
    }
    /*
     when we close Connection we dont have active connection check if we need to close
    public Connection getConnection() throws SQLException {
        if (con == null || con.isClosed()) {
            con = DriverManager.getConnection(
                    url + scheme + "?serverTimezone=Asia/Jerusalem&useSSL=false",
                    user,
                    pass
            );
        }
        return con;
    }
    */

    public void close() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }
}

	


