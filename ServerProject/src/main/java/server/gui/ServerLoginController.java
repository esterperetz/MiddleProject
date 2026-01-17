package server.gui;

import DBConnection.DBConnection;
import entities.Alarm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import server.controller.ServerController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

/**
 * Controller class for the Server Login/Configuration screen.
 * Handles initial setup, database connection authentication, and server startup.
 */
public class ServerLoginController {

    // --- FXML UI Components ---
    @FXML
    private TextField txtHost;
    
    @FXML
    private TextField txtUserName;
    
    @FXML
    private TextField Scheme; // Note: Keeps original name to match FXML fx:id
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private Button btnSend;
    
    @FXML
    private Button btnConnect;
    
    @FXML
    private Button btnExit;
    
    @FXML
    private TextField txtPort;
    
    @FXML
    private Label lblStatus;

    // --- Controller Logic Fields ---
    private ServerController server;

    /**
     * Handles the "Connect" button action.
     * Validates inputs, initializes the DB connection, starts the server listener,
     * and transitions to the main Server Control Panel.
     *
     * @param event The event triggered by clicking the button.
     */
    @FXML
    void onConnect(ActionEvent event) {
        // 1. Retrieve and normalize data from GUI
        String host = (txtHost != null) ? txtHost.getText().trim() : "localhost";
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();
        String schema = Scheme.getText().trim();
        
        // Retrieve local IP Address for display purposes
        String ipAddress = " ";
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException u) {
            lblStatus.setText("IP exception.");
        }
        
        // Validate Port Input
        int port = 5555; // Default port
        try {
            if (txtPort != null && !txtPort.getText().trim().isEmpty()) {
                port = Integer.parseInt(txtPort.getText().trim());
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Port must be a valid number.");
            return;
        }

        // Validate Required Fields
        if (user.isEmpty() || pass.isEmpty() || schema.isEmpty() || host.isEmpty()) {
            lblStatus.setText("All DB connection fields are required.");
            return;
        }

        // 2. Attempt Connection and Startup
        try {
            // A. Initialize DB Connection (Must happen before starting server logic)
            DBConnection.initializeConnection(host, schema, user, pass);

            // B. Load the main server GUI screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));
            Parent root = loader.load();
            
            // C. Get the controller for the next view
            ServerViewController view = loader.getController();
            
            // D. Create Server Controller and start listening
            server = new ServerController(port, view);
            server.listen();
            
            // E. Transition Scene: Replace login window with server panel
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            primaryStage.setTitle("Server Control Panel - Listening on Port " + port + " IP: " + ipAddress);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            String header = "DB Connection Failed";
            String context = "Could not connect to the database. Check credentials and schema: \n";
            Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);

        } catch (IOException e) {
            e.printStackTrace();
            String header = "GUI Load Error";
            String context = "Could not load 'connections_to_server.fxml'. Check file path:\n";
            Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);

        } catch (Exception e) {
            e.printStackTrace();
            String header = "Server Startup Failed";
            String context = "An unexpected error occurred during server startup:\n";
            Alarm.showAlertWithException(header, context, Alert.AlertType.ERROR, e);
        }
    }

    /**
     * Alias method for connection (e.g., if triggered by a different button).
     *
     * @param event The action event.
     */
    @FXML
    private void onSend(ActionEvent event) {
        onConnect(event);
    }

    /**
     * Handles the "Exit" button action.
     * Safely closes the server connection if active and closes the window.
     *
     * @param event The action event.
     */
    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        
        // Safely close the server if it's running
        if (server != null && server.isListening()) {
            try {
                server.close();
            } catch (Exception ignored) {
                // Ignore errors during shutdown
            }
        }
        
        // Close the window
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    /**
     * Initial startup method to load the login screen.
     * Useful if this class is used as an entry point.
     *
     * @param primaryStage The main application stage.
     * @throws Exception If FXML loading fails.
     */
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Configuration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}