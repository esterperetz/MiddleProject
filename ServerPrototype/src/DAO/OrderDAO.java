package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import DBConnection.DBConnection;
import Entities.Order;

public class OrderDAO {

    // --- מתודות קיימות (GET_ALL, GET_BY_ID, CREATE) נשארות כפי שהן ---

    public List<Order> getAllOrders() throws SQLException {
        // ... (הקוד נשאר ללא שינוי)
        String sql = "SELECT * FROM orders";
        Connection con = null; 
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Order> list = new ArrayList<>();

            while (rs.next()) {
                Order o = new Order(
                    rs.getInt("order_number"),
                    rs.getDate("order_date"),
                    rs.getInt("number_of_guests"),
                    rs.getInt("confirmation_code"),
                    rs.getInt("subscriber_id"),
                    rs.getDate("date_of_placing_order")
                );
                list.add(o);
            }

            return list;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public Order getOrder(int id) throws SQLException {
        // ... (הקוד נשאר ללא שינוי)
        String sql = "SELECT * FROM orders WHERE order_number = ?"; // שינוי ל order_number אם זה המפתח
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new Order(
                        rs.getInt("order_number"),
                        rs.getDate("order_date"),
                        rs.getInt("number_of_guests"),
                        rs.getInt("confirmation_code"),
                        rs.getInt("subscriber_id"),
                        rs.getDate("date_of_placing_order")
                    );
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public boolean createOrder(Order o) throws SQLException {
        // ... (הקוד נשאר ללא שינוי)
        String sql = "INSERT INTO orders(" +
                     "order_date, number_of_guests, confirmation_code, " +
                     "subscriber_id, date_of_placing_order" +
                     ") VALUES (?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            // סט פאראמטרים (נשאר כפי שכתבת)
            stmt.setDate(1, new java.sql.Date(o.getOrder_date().getTime()));
            stmt.setInt(2, o.getNumber_of_guests());
            stmt.setInt(3, o.getConfirmation_code());
            stmt.setInt(4, o.getSubscriber_id());
            stmt.setDate(5, new java.sql.Date(o.getDate_of_placing_order().getTime()));

            // executeUpdate מחזיר את מספר השורות שהושפעו (צריך להיות > 0 להצלחה)
            return stmt.executeUpdate() > 0; 
        } finally {
            if (stmt != null) stmt.close();
        }
    }
    
    // --- מתודה חדשה: UPDATE ---

    /**
     * מעדכן הזמנה קיימת במסד הנתונים.
     * @param o האובייקט Order עם הנתונים המעודכנים.
     * @return true אם לפחות שורה אחת עודכנה, אחרת false.
     */
    public boolean updateOrder(Order o) throws SQLException {
        String sql = "UPDATE orders SET " +
                     "order_date = ?, number_of_guests = ?, confirmation_code = ?, " +
                     "subscriber_id = ?, date_of_placing_order = ? " +
                     "WHERE order_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            // הגדרת הפאראמטרים (שים לב לסדר)
            stmt.setDate(1, new java.sql.Date(o.getOrder_date().getTime()));
            stmt.setInt(2, o.getNumber_of_guests());
            stmt.setInt(3, o.getConfirmation_code());
            stmt.setInt(4, o.getSubscriber_id());
            stmt.setDate(5, new java.sql.Date(o.getDate_of_placing_order().getTime()));
            
            // המפתח לזיהוי השורה לעדכון
            stmt.setInt(6, o.getOrder_number()); 

            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null)
            	stmt.close();
        }
    }

    // --- מתודה חדשה: DELETE ---

    /**
     * מוחק הזמנה לפי מספר הזמנה.
     * @param id מספר ההזמנה (המפתח הראשי).
     * @return true אם לפחות שורה אחת נמחקה, אחרת false.
     */
    public boolean deleteOrder(int id) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_number = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            
            // הגדרת הפאראמטר: מספר ההזמנה למחיקה
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
        }
    }
}