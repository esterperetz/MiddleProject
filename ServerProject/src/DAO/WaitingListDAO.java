package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import DBConnection.DBConnection;
import Entities.WaitingList;

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
                int subIdTemp = rs.getInt("subscriber_id");
                Integer subId = rs.wasNull() ? null : subIdTemp;

                WaitingList item = new WaitingList(
                        rs.getInt("waiting_id"),
                        subId,
                        rs.getString("identification_details"),
                        rs.getString("full_name"),
                        rs.getInt("number_of_guests"),
                        rs.getTimestamp("enter_time"),
                        rs.getInt("confirmation_code")
                );
                list.add(item);
            }
            return list;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
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
                int subIdTemp = rs.getInt("subscriber_id");
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
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    public boolean enterWaitingList(WaitingList item) throws SQLException {
        String sql = "INSERT INTO waiting_list (subscriber_id, identification_details, full_name, number_of_guests, enter_time, confirmation_code) VALUES (?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement stmt = null;

        try {
            con = DBConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);

            if (item.getSubscriberId() == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, item.getSubscriberId());
            }

            stmt.setString(2, item.getIdentificationDetails());
            stmt.setString(3, item.getFullName());
            stmt.setInt(4, item.getNumberOfGuests());
            stmt.setTimestamp(5, new java.sql.Timestamp(item.getEnterTime().getTime()));
            stmt.setInt(6, item.getConfirmationCode());

            return stmt.executeUpdate() > 0;
        } finally {
            if (stmt != null) stmt.close();
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
            if (stmt != null) stmt.close();
        }
    }
}