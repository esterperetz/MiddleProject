package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import DBConnection.DBConnection;
import entities.Customer;
import entities.CustomerType;

/**
 * Data Access Object (DAO) for managing Customer entities.
 * 

[Image of customer database table schema]

 * Handles creating, retrieving, and updating customer records in the database.
 * Distinguishes between REGULAR customers and SUBSCRIBERS.
 */
public class CustomerDAO {

    /**
     * Creates a new customer/subscriber in the DB and updates the object's ID.
     * Uses RETURN_GENERATED_KEYS to retrieve the auto-incremented ID.
     *
     * @param customer The Customer object to persist.
     * @return true if the customer was successfully created.
     */
    public boolean createCustomer(Customer customer) {
        String query = "INSERT INTO Customer (subscriber_code,customer_name, phone_number, email , customer_type ) VALUES (?,?, ?, ?, ?)";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                Integer subCode = customer.getSubscriberCode();
                if (subCode == null || subCode == 0) {
                    ps.setNull(1, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(1, subCode);
                }
                ps.setString(2, customer.getName());
                ps.setString(3, customer.getPhoneNumber());
                ps.setString(4, customer.getEmail());
                ps.setString(5, customer.getType().getString());

                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet generatedKeys = ps.getGeneratedKeys(); // returns id number to ps
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getInt(1));
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating subscriber: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }

    /**
     * Fetches a subscriber by their unique Subscriber Code.
     *
     * @param SubscriberCode The unique subscriber code.
     * @return The Customer object if found, otherwise null.
     */
    public Customer getCustomerBySubscriberCode(int SubscriberCode) {

        String query = "SELECT * FROM Customer WHERE subscriber_code = ?";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, SubscriberCode);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Customer s = new Customer(rs.getInt("customer_id"), rs.getInt("subscriber_code"),
                            rs.getString("customer_name"),
                            rs.getString("phone_number"), rs.getString("email"),
                            CustomerType.valueOf(rs.getString("customer_type")));
                    // s.setSubscriberId(rs.getInt("subscriber_id"));
                    return s;

                    // return createSubscriberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    /**
     * Fetches a customer by their unique internal Database ID (customer_id).
     *
     * @param SubscriberId The primary key ID of the customer.
     * @return The Customer object if found, otherwise null.
     */
    public Customer getCustomerByCustomerId(int SubscriberId) {

        String query = "SELECT * FROM Customer WHERE customer_id = ?";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, SubscriberId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Customer s = new Customer(rs.getInt("customer_id"), rs.getInt("subscriber_code"),
                            rs.getString("customer_name"),
                            rs.getString("phone_number"), rs.getString("email"),
                            CustomerType.valueOf(rs.getString("customer_type")));
                    // s.setSubscriberId(rs.getInt("subscriber_id"));
                    return s;

                    // return createSubscriberFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by ID: " + e.getMessage());
            // e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    /**
     * Fetches a customer by email, strictly filtering for SUBSCRIBER type.
     *
     * @param customerMail The customer's email.
     * @return The Customer object if found and is a subscriber.
     */
    public Customer getCustomerByEmailSUBSCRIBER(String customerMail) {
        String query = "SELECT * FROM Customer WHERE email = ?  AND customer_type = 'SUBSCRIBER'";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, customerMail);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return createCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    /**
     * Fetches a customer by their email regardless of type.
     *
     * @param customerMail The customer's email.
     * @return The Customer object if found.
     */
    public Customer getCustomerByEmail(String customerMail) {
        String query = "SELECT * FROM Customer WHERE email = ?";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, customerMail);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    return createCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return null;
    }

    /**
     * Updates an existing customer's contact details based on their subscriber code.
     *
     * @param customer The Customer object containing updated details.
     * @return true if the update was successful.
     */
    public boolean updateCustomerDetails(Customer customer) {
        String query = "UPDATE customer SET customer_name = ?, phone_number = ?, email = ? WHERE subscriber_code = ?";

        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (PreparedStatement ps = con.prepareStatement(query)) {

                ps.setString(1, customer.getName());

                ps.setString(2, customer.getPhoneNumber());

                ps.setString(3, customer.getEmail());

                ps.setInt(4, customer.getSubscriberCode());

                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;

            }
        } catch (SQLException e) {
            System.out.println("Error updating subscriber: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return false;
    }

    /**
     * Returns a list of all customers who are of type SUBSCRIBER.
     *
     * @return ArrayList of Customer objects.
     */
    public ArrayList<Customer> getAllCustomers() {
        ArrayList<Customer> subscribers = new ArrayList<>();
        String query = "SELECT * FROM Customer WHERE customer_type = 'SUBSCRIBER'";
        Connection con = null;
        try {
            con = DBConnection.getInstance().getConnection();
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    subscribers.add(createCustomerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all subscribers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBConnection.getInstance().releaseConnection(con);
        }
        return subscribers;
    }

    /**
     * Helper method to map a ResultSet row to a Customer object.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return The mapped Customer object.
     * @throws SQLException If a database error occurs.
     */
    private Customer createCustomerFromResultSet(ResultSet rs) throws SQLException {
        try {
            Customer s = new Customer(rs.getInt("customer_id"), rs.getInt("subscriber_code"),
                    rs.getString("customer_name"),
                    rs.getString("phone_number"), rs.getString("email"),
                    CustomerType.valueOf(rs.getString("customer_type")));
            // s.setSubscriberId(rs.getInt("subscriber_id"));
            return s;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}