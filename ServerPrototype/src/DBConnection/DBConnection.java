package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;
    private final String url;
    private final String user;
    private final String pass;
    
    // החיבור הבודד והקבוע נשמר כאן
    private Connection connection; 

    private DBConnection() {
        this.url = "jdbc:mysql://localhost:3306/mid_project_prototype?serverTimezone=Asia/Jerusalem&useSSL=false";
        this.user = "root";      
        this.pass = "159357";
        
        // יצירת החיבור בפעם הראשונה
        try {
            // טעינת הדרייבר
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Single persistent DB Connection established.");
        }
        catch (SQLException | ClassNotFoundException e) {
            System.err.println("FATAL: Could not establish a single persistent DB connection.");
            e.printStackTrace();
            // זורק שגיאת זמן ריצה כדי לעצור אם אין חיבור לבסיס נתונים
            throw new RuntimeException("Database connection failed during initialization.", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * מחזיר את החיבור הבודד והקבוע.
     * מוודא שהחיבור תקף, ואם לא - מנסה ליצור אותו מחדש.
     */
    public Connection getConnection() throws SQLException {
        // בדיקה אם החיבור נסגר מבחוץ או פג תוקפו.
        // isValid(10) בודק את תקינות החיבור עם Timeout של 10 שניות.
        if (connection == null || connection.isClosed() || !connection.isValid(10)) { 
            System.out.println("Connection is stale or closed. Re-establishing connection.");
            try {
                // יצירה מחדש של החיבור
                this.connection = DriverManager.getConnection(url, user, pass);
                System.out.println("Connection re-established successfully.");
            } catch (SQLException e) {
                 System.err.println("Could not re-establish DB connection.");
                 throw e; 
            }
        }
        return connection;
    }
    
    /**
     * סוגר את החיבור היחיד (יש לקרוא בעת כיבוי השרת).
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Single persistent DB connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the single DB connection.");
                e.printStackTrace();
            }
        }
    }
}
