package server.controller;

import DBConnection.DBConnection;  // מחלקת החיבור ל-DB
import Entities.Order;             // ישות Order (מה-project של ה-Entities)
import java.util.Date;
import java.util.List;

/**
 * OrderController - לוגיקה עסקית להזמנות.
 * משתמש ב-DBConnection כדי לבצע את ה-SQL בפועל.
 */
public class OrderController {

    private DBConnection dbConnector;

    public OrderController(DBConnection dbConnector) {
        this.dbConnector = dbConnector;
    }

    // ------------------------------------------------------------------
    // מתודות גישה לנתונים: מעבירות את העבודה ל-DBConnection
    // ------------------------------------------------------------------

    public List<Order> getAllOrders() {
        return dbConnector.getAllOrders();
    }

    public Order getOrder(int orderNumber) {
        return dbConnector.getOrder(orderNumber);
    }

    /**
     * מעדכן הזמנה, כולל ולידציה עסקית.
     */
    public void updateOrder(int orderNumber, Date newDate, int newGuests) throws IllegalArgumentException {

        // *** שלב 1: ולידציה עסקית ***
        Date now = new Date(System.currentTimeMillis());

        if (newDate.before(now) || newDate.equals(now)) {
            throw new IllegalArgumentException(
                "שגיאת ולידציה: תאריך ההזמנה החדש אינו תקין (בעבר או בהווה)."
            );
        }

        // *** שלב 2: ביצוע ה-SQL דרך DBConnection ***
        dbConnector.updateOrder(orderNumber, newDate, newGuests);
    }
}
