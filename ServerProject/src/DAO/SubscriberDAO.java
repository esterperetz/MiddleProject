package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import Entities.Subscriber;

public class SubscriberDAO {
    private Connection dbConnection;

    public SubscriberDAO(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Creates a new subscriber in the DB and updates the object's ID
    public boolean createSubscriber(Subscriber subscriber) {
        String query = "INSERT INTO subscribers (first_name, last_name, username, phone_number, email) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = dbConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { 
            
            ps.setString(1, subscriber.getFirstName());
            ps.setString(2, subscriber.getLastName());
            ps.setString(3, subscriber.getUsername());
            ps.setString(4, subscriber.getPhoneNumber());
            ps.setString(5, subscriber.getEmail());

            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys(); //returns id number to ps
                if (generatedKeys.next()) {
                    subscriber.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error creating subscriber: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Fetches a subscriber by their unique ID
    public Subscriber getSubscriberById(int id) {
        String query = "SELECT * FROM subscribers WHERE subscriber_id = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return createSubscriberFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Fetches a subscriber by their username 
    public Subscriber getSubscriberByUsername(String username) {
        String query = "SELECT * FROM subscribers WHERE username = ?";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return createSubscriberFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subscriber by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Updates editable details (name, phone, email) 
    public boolean updateSubscriberDetails(Subscriber subscriber) {
        String query = "UPDATE subscribers SET first_name = ?, last_name = ?, phone_number = ?, email = ? WHERE subscriber_id = ?";
        
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setString(1, subscriber.getFirstName());
            ps.setString(2, subscriber.getLastName());
            ps.setString(3, subscriber.getPhoneNumber());
            ps.setString(4, subscriber.getEmail());
            ps.setInt(5, subscriber.getId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error updating subscriber: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Returns a list of all subscribers in the system
    public ArrayList<Subscriber> getAllSubscribers() {
        ArrayList<Subscriber> subscribers = new ArrayList<>();
        String query = "SELECT * FROM subscribers";
        
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                subscribers.add(createSubscriberFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all subscribers: " + e.getMessage());
            e.printStackTrace();
        }
        return subscribers;
    }

    // Helper method to map ResultSet to Subscriber object
    private Subscriber createSubscriberFromResultSet(ResultSet rs) throws SQLException {
        Subscriber s = new Subscriber(
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("username"),
            rs.getString("phone_number"),
            rs.getString("email")
        );
        s.setId(rs.getInt("subscriber_id"));
        return s;
    }
}