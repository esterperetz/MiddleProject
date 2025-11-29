package server.controller;

import DBConnection.DBConnection; // מחלקת החיבור ל-DB
import Entities.Order;      // ישות Order (צריך להיות ב-common)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * OrderManager - מטפל בלוגיקה העסקית ובפעולות ה-SQL הקשורות להזמנות.
 * משמש כמחליף לשכבת ה-Model/DAO החסרה בארכיטקטורה הנוכחית.
 */
public class OrderController {

    private DBConnection dbConnector;

    public OrderController(DBConnection dbConnector) {
        this.dbConnector = dbConnector;
    }
    
    // שיטת עזר לשליפת החיבור
    private Connection getConnection() throws SQLException {
        // מניח ש-DBConnection מספקת את החיבור דרך getConnection()
        return dbConnector.getConnection();
    }
    
    // ------------------------------------------------------------------
    // מתודות הגישה לנתונים וה-SQL
    // ------------------------------------------------------------------

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        Connection con = getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `order`");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int order_number = rs.getInt("order_number");
                Date order_date = rs.getDate("order_date");
                int number_of_guests = rs.getInt("number_of_guests");
                int confirmation_code = rs.getInt("confirmation_code");
                int subscriber_id = rs.getInt("subscriber_id");
                Date date_of_placing_order = rs.getDate("date_of_placing_order");
                
                Order o = new Order(order_number, order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order);
                list.add(o);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching all orders: " + e.getMessage());
            throw e;
        }

        return list;
    }

    public Order getOrder(int orderNumber) throws SQLException {
        Connection con = getConnection();
        
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM `order` WHERE order_number=? ");
            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                 return new Order(rs.getInt("order_number"), rs.getDate("order_date"), rs.getInt("number_of_guests"), rs.getInt("confirmation_code"), rs.getInt("subscriber_id"), rs.getDate("date_of_placing_order"));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error while fetching order " + orderNumber + ": " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * מעדכן הזמנה, כולל ולידציה עסקית.
     */
    public void updateOrder(int orderNumber, Date newDate, int newGuests) throws IllegalArgumentException, SQLException {
        
        // ** שלב 1: לוגיקה עסקית / ולידציה (Business Logic) **
        // הולידציה מתבצעת כאן
        Date now = new Date(System.currentTimeMillis());
        
        if (newDate.before(now) || newDate.equals(now)) {
            throw new IllegalArgumentException("שגיאת ולידציה: תאריך ההזמנה החדש אינו תקין (בעבר או בהווה).");
        }
        
        // ** שלב 2: ביצוע ה-SQL **
        Connection con = getConnection();
        try {
            PreparedStatement ps = con
                    .prepareStatement("UPDATE `order` SET order_date = ?, number_of_guests = ? WHERE order_number = ?");
            // שימוש ב-java.sql.Date לשמירה ב-DB
            ps.setDate(1, new java.sql.Date(newDate.getTime())); 
            ps.setInt(2, newGuests);
            ps.setInt(3, orderNumber);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("SQL Error during order update: " + e.getMessage());
            throw e;
        }
    }
}