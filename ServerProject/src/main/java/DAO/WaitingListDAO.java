package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DBConnection.DBConnection;
import entities.WaitingList;

public class WaitingListDAO {

    public List<WaitingList> getAllWaitingList() throws SQLException {
        String sql = "SELECT * FROM waiting_list WHERE in_waiting_list = ? ORDER BY enter_time ASC";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, 1);
            rs = stmt.executeQuery();

            List<WaitingList> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToWaitingList(rs));
            }
            return list;
        } finally {
            closeResources(rs, stmt);
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    public List<Map<String, Object>> getAllWaitingListWithCustomers() {
        List<Map<String, Object>> resultList = new ArrayList<>();

        String sql = "SELECT " + " c.subscriber_code, c.email, c.customer_name, c.phone_number, "
                + " w.waiting_id, w.customer_id, w.enter_time, " + " w.number_of_guests, w.confirmation_code "
                + "FROM Customer c " + "JOIN waiting_list w ON c.customer_id = w.customer_id";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("customer_name", rs.getString("customer_name"));
                    row.put("email", rs.getString("email"));
                    row.put("phone_number", rs.getString("phone_number"));
                    row.put("subscriber_code", rs.getObject("subscriber_code"));

                    row.put("waiting_id", rs.getInt("waiting_id"));
                    row.put("customer_id", rs.getInt("customer_id"));
                    row.put("number_of_guests", rs.getInt("number_of_guests"));
                    row.put("confirmation_code", rs.getInt("confirmation_code"));

                    row.put("enter_time", rs.getTimestamp("enter_time"));

                    resultList.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }

        return resultList;
    }

    public WaitingList getByWaitingId(int waitingId) {
        String sql = "SELECT * FROM waiting_list WHERE waiting_id = ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, waitingId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToWaitingList(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    public WaitingList getByCode(int code) throws SQLException {
        String sql = "SELECT * FROM waiting_list WHERE confirmation_code = ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, code);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToWaitingList(rs);
                    }
                    return null;
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    public int getPosition(Timestamp enterTime) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 FROM waiting_list WHERE enter_time < ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setTimestamp(1, enterTime);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 1;
                }
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    public boolean enterWaitingList(WaitingList item) throws SQLException {
        String sql = "INSERT INTO waiting_list (customer_id, number_of_guests, enter_time, confirmation_code) VALUES (?, ?, ?, ?)";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {

                if (item.getCustomerId() == null) {
                    stmt.setNull(1, java.sql.Types.INTEGER);
                } else {
                    stmt.setInt(1, item.getCustomer().getCustomerId());
                }

                stmt.setInt(2, item.getNumberOfGuests());
                stmt.setTimestamp(3, new java.sql.Timestamp(item.getEnterTime().getTime()));
                stmt.setInt(4, item.getConfirmationCode());

                return stmt.executeUpdate() > 0;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    public boolean exitWaitingList(int waitingId) throws SQLException {
        String sql = "UPDATE waiting_list SET in_waiting_list = ? WHERE waiting_id = ?";
        Connection con = null;

        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
            	stmt.setInt(1, 0);
                stmt.setInt(2, waitingId);
                return stmt.executeUpdate() > 0;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }

    // creates waiting list from rs
    private WaitingList mapResultSetToWaitingList(ResultSet rs) throws SQLException {
        int subIdTemp = rs.getInt("customer_id");
        Integer subId = rs.wasNull() ? null : subIdTemp;

        return new WaitingList(
                rs.getInt("waiting_id"),
                subId,
                rs.getInt("number_of_guests"),
                rs.getTimestamp("enter_time"),
                rs.getInt("confirmation_code"), null);
    }

    private void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 // בתוך WaitingListDAO.java
//for manager reports
    public List<WaitingList> getWaitingListForReport(int month, int year) throws SQLException {
        // עדכנתי את השאילתה: הוספתי תנאי ש-customer_id לא יהיה NULL
        String sql = "SELECT w.*, c.customer_name, c.phone_number, c.email, c.subscriber_code, c.customer_type " +
                     "FROM waiting_list w " +
                     "LEFT JOIN Customer c ON w.customer_id = c.customer_id " +
                     "WHERE MONTH(w.enter_time) = ? AND YEAR(w.enter_time) = ? " +
                     "AND w.customer_id IS NOT NULL " + // <--- השינוי כאן: מסנן שורות ללא מזהה לקוח
                     "ORDER BY w.enter_time ASC";

        Connection con = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, month);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                List<WaitingList> list = new ArrayList<>();
                while (rs.next()) {
                    // אין צורך לבדוק כאן אם ה-ID הוא null כי הסינון נעשה ב-SQL

                    // 1. יצירת אובייקט WaitingList
                    WaitingList wl = new WaitingList(
                            rs.getInt("waiting_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("number_of_guests"),
                            rs.getTimestamp("enter_time"),
                            rs.getInt("confirmation_code"),
                            null
                    );

                    // 2. יצירת אובייקט Customer ומילוי הפרטים
                    entities.Customer cust = new entities.Customer();
                    String name = rs.getString("customer_name");
                    
                    // כעת בטוח שיהיה שם (אלא אם יש בעיה ב-DB שלקוח נמחק), אבל נשאיר את הבדיקה ליתר ביטחון
                    if (name != null) {
                        cust.setName(name);
                        cust.setPhoneNumber(rs.getString("phone_number"));
                    } else {
                        cust.setName("Unknown Registered Customer"); 
                    }
                    
                    wl.setCustomer(cust);
                    list.add(wl);
                }
                return list;
            }
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
    }
}