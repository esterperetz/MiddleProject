package server.gui;

import DBConnection.DBConnection;
import entities.Alarm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import server.controller.ServerController;
import java.sql.SQLException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerLoginController {

    // Ensure this field exists in ServerLogin.fxml (for DB Host/IP)
    @FXML private TextField txtHost;
    @FXML private TextField txtUserName;
    @FXML private TextField Scheme; // DB Schema (Database Name)
    @FXML private PasswordField txtPassword;
    @FXML private Button btnSend;
    @FXML private Button btnConnect;
    @FXML private Button btnExit;
    @FXML private TextField txtPort;
    @FXML private Label lblStatus;

    private ServerController server;

    @FXML
    void onConnect(ActionEvent event)  {
        // Retrieve DB connection details (GUI fields)
        String host = txtHost != null ? txtHost.getText().trim() : "localhost";
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();
        String schema = Scheme.getText().trim();
        
        String IP=" ";
        try {
            IP=InetAddress.getLocalHost().getHostAddress();
        }
        catch(UnknownHostException u)
        {
        	lblStatus.setText("IP exception.");
        }
        
        int port = 5555;
        try {
            if (txtPort != null && !txtPort.getText().trim().isEmpty()) {
                port = Integer.parseInt(txtPort.getText().trim());
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Port must be a valid number.");
            return;
        }

        if (user.isEmpty() || pass.isEmpty() || schema.isEmpty() || host.isEmpty()) {
            lblStatus.setText("All DB connection fields are required.");
            return;
        }

        try {
            // 1. Initialize DB connection with user input (must be the first step!)
            DBConnection.initializeConnection(host, schema, user, pass);

            // 2. Load the main server GUI screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));
            Parent root = loader.load();
            
            // 3. Get the Server screen controller (ServerViewController)
            ServerViewController view = loader.getController();
            
            // 4. Create the Server controller and start listening for connections
            server = new ServerController(port, view);
            server.listen(); // Start listening for clients
            
            // 5. Transition to the main server screen: replace the Scene in the current window
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            primaryStage.setTitle("Server Control Panel - Listening on Port " + port+" IP: "+IP);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (SQLException | ClassNotFoundException e) {

            e.printStackTrace();
            String header="DB Connection Failed";
            String context="Could not connect to the database. Check credentials and schema: \n";
            Alarm.showAlertWithException(header,context,Alert.AlertType.ERROR,e);
            //Alert alert = new Alert(Alert.AlertType.ERROR);
            //alert.setHeaderText("DB Connection Failed");
            //alert.setContentText("Could not connect to the database. Check credentials and schema: \n" + e.getMessage());
            //alert.showAndWait();
        }
        catch (IOException e) {
            e.printStackTrace();
            String header="GUI Load Error";
            String context="Could not load 'connections_to_server.fxml'. Check file path:\n";
            
            Alarm.showAlertWithException(header,context,Alert.AlertType.ERROR,e);
            //Alert alert = new Alert(Alert.AlertType.ERROR);
            //alert.setHeaderText("GUI Load Error");
            //alert.setContentText("Could not load 'connections_to_server.fxml'. Check file path:\n" + e.getMessage());
            //alert.showAndWait();
        }
        catch (Exception e) {
            e.printStackTrace();
            String header="Server Startup Failed";
            String context="An unexpected error occurred during server startup:\n";
            
            Alarm.showAlertWithException(header,context,Alert.AlertType.ERROR,e);
            //Alert alert = new Alert(Alert.AlertType.ERROR);
            //alert.setHeaderText("Server Startup Failed");
            //alert.setContentText("An unexpected error occurred during server startup:\n" + e.getMessage());
            //alert.showAndWait();
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        // Calls onConnect, which executes the actual connection logic
        onConnect(event);
    }

    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        // If server is active, it should be closed before exiting
        if (server != null && server.isListening()) {
             try {
                 server.close();
            	 //server.serverClosed();
             } catch (Exception ignored) {}
        }
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    
    /**
     * @param primaryStage
     * @throws Exception
     * start method is needed to launch the initial screen
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