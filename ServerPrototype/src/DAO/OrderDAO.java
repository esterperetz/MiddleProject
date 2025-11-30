package DAO;

import DBConnection.DBConnection;
import Entities.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//ORDER DATA LAYER, interfaces with DB for orders//
public class OrderDAO {

    private final DBConnection db;

    public OrderDAO(DBConnection db) {
        this.db = db;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order`";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToOrder(rs));
            }
        }

        return list;
    }

    public Order getOrder(int orderNumber) throws SQLException {
        String sql = "SELECT * FROM `order` WHERE order_number = ?";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToOrder(rs);
                }
                return null;
            }
        }
    }

    public void updateOrder(int orderNumber, Date newDate, int newGuests) throws SQLException {
        String sql = "UPDATE `order` " +
                     "SET order_date = ?, number_of_guests = ? " +
                     "WHERE order_number = ?";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(newDate.getTime()));
            ps.setInt(2, newGuests);
            ps.setInt(3, orderNumber);
            ps.executeUpdate();
        }
    }

    //gets row from DB and creates Order object//
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        int orderNumber       = rs.getInt("order_number");
        Date orderDate        = rs.getDate("order_date");
        int numberOfGuests    = rs.getInt("number_of_guests");
        int confirmationCode  = rs.getInt("confirmation_code");
        int subscriberId      = rs.getInt("subscriber_id");
        Date dateOfPlacing    = rs.getDate("date_of_placing_order");

        return new Order(orderNumber, orderDate, numberOfGuests,
                         confirmationCode, subscriberId, dateOfPlacing);
    }
}
