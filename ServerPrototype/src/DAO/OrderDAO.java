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

    public void updateOrder(Order order) throws SQLException {
        String sql = "UPDATE `order` " +
                     "SET order_date = ?, number_of_guests = ? " +
                     "WHERE order_number = ?";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, new java.sql.Date(order.getOrder_date().getTime()));
            ps.setInt(2, order.getNumber_of_guests());
            ps.setInt(3, order.getOrder_number());
            ps.executeUpdate();
        }
    }
    public void addOrder(Order order) throws SQLException {
        String sql = "INSERT INTO `order` " +
                     "(order_number, order_date, number_of_guests, confirmation_code, subscriber_id, date_of_placing_order) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, order.getOrder_number());
            ps.setDate(2, new java.sql.Date(order.getOrder_date().getTime()));
            ps.setInt(3, order.getNumber_of_guests());
            ps.setInt(4, order.getConfirmation_code());
            ps.setInt(5, order.getSubscriber_id());
            ps.setDate(6, new java.sql.Date(order.getDate_of_placing_order().getTime()));

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
