package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// CONNECTION TO DB ONLY //
public class DBConnection {

    private final String user;
    private final String pass;
    private final String scheme;

    public DBConnection(String user, String pass, String scheme) {
        this.user = user;
        this.pass = pass;
        this.scheme = scheme;
    }

    /**
     * Creates a NEW connection on each call.
     * Caller is responsible for closing the Connection (try-with-resources in DAO).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + scheme +
                "?serverTimezone=Asia/Jerusalem&useSSL=false",
                user,
                pass
        );
    }
}



