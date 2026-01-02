package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import entities.WaitingList;

public class WaitingListDAO {

    public List<WaitingList> getAllWaitingList() throws SQLException {
        String sql = "SELECT * FROM waiting_list ORDER BY enter_time ASC";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<WaitingList> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToWaitingList(rs));
            }
            return list;
        } finally {
            closeResources(rs, stmt);
        }
    }
    public WaitingList getByWaitingId(int waitingId) throws SQLException {
        String sql = "SELECT * FROM waiting_list WHERE waiting_id = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, waitingId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                // Using the helper method we created earlier to keep it clean
                return mapResultSetToWaitingList(rs);
            }
            return null;
        } finally {
            // Simplified cleanup using the helper
            closeResources(rs, stmt);
        }
    }

    public WaitingList getByCode(int code) throws SQLException {
        String sql = "SELECT * FROM waiting_list WHERE confirmation_code = ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, code);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToWaitingList(rs);
            }
            return null;
        } finally {
            closeResources(rs, stmt);
        }
    }

    /**
     * Calculates subscriber position in line by counting entries with an earlier timestamp.
     */
    public int getPosition(Timestamp enterTime) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 FROM waiting_list WHERE enter_time < ?";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setTimestamp(1, enterTime);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 1;
        } finally {
            closeResources(rs, stmt);
        }
    }

    public boolean enterWaitingList(WaitingList item) throws SQLException {
    	String sql = "INSERT INTO waiting_list (customer_id, identification_details, full_name, number_of_guests, enter_time, confirmation_code) VALUES (?, ?, ?, ?, ?, ?)";        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            if (item.getCustomerId() == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, item.getCustomerId());
            }

            stmt.setString(2, item.getIdentificationDetails());
            stmt.setString(3, item.getFullName());
            stmt.setInt(4, item.getNumberOfGuests());
            stmt.setTimestamp(5, new java.sql.Timestamp(item.getEnterTime().getTime()));
            stmt.setInt(6, item.getConfirmationCode());

            return stmt.executeUpdate() > 0;
        } finally {
            closeResources(null, stmt);
        }
    }

    public boolean exitWaitingList(int waitingId) throws SQLException {
        String sql = "DELETE FROM waiting_list WHERE waiting_id = ?";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, waitingId);
            return stmt.executeUpdate() > 0;
        } finally {
            closeResources(null, stmt);
        }
    }

    //creates waiting list from rs
    private WaitingList mapResultSetToWaitingList(ResultSet rs) throws SQLException {
    	int subIdTemp = rs.getInt("customer_id");
    	Integer subId = rs.wasNull() ? null : subIdTemp;

        return new WaitingList(
                rs.getInt("waiting_id"),
                subId,
                rs.getString("identification_details"),
                rs.getString("full_name"),
                rs.getInt("number_of_guests"),
                rs.getTimestamp("enter_time"),
                rs.getInt("confirmation_code")
        );
    }
   

    private void closeResources(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}