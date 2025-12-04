package server.gui;

import DBConnection.DBConnection; 
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

public class ServerLoginController {

    // ודא ששדה זה קיים ב-ServerLogin.fxml (עבור DB Host/IP)
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
    void onConnect(ActionEvent event) {
        // קליטת פרטי חיבור לבסיס הנתונים (שדות ה-GUI)
        String host = txtHost != null ? txtHost.getText().trim() : "localhost"; 
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();
        String schema = Scheme.getText().trim();
        
        // קליטת פורט השרת
        int port = 5555; 
        try {
            if (txtPort != null && !txtPort.getText().trim().isEmpty()) {
                port = Integer.parseInt(txtPort.getText().trim());
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Port must be a valid number.");
            return;
        }

        // ודא שכל שדות ההתחברות ל-DB מולאו
        if (user.isEmpty() || pass.isEmpty() || schema.isEmpty() || host.isEmpty()) {
            lblStatus.setText("All DB connection fields are required.");
            return;
        }

        try {
            // 1. אתחול חיבור ל-DB באמצעות הקלט מהמשתמש (חייב להיות הצעד הראשון!)
            DBConnection.initializeConnection(host, schema, user, pass); 

            // 2. טעינת מסך ה-GUI הראשי של השרת
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));
            Parent root = loader.load();
            
            // 3. קבלת הבקר של מסך השרת (ServerViewController)
            ServerViewController view = loader.getController(); 
            
            // 4. יצירת בקר השרת והאזנה לחיבורים
            server = new ServerController(port, view);
            server.listen(); // התחלת האזנה ללקוחות
            
            // 5. ✅ מעבר למסך הראשי של השרת: החלפת ה-Scene בחלון הקיים
            Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            primaryStage.setTitle("Server Control Panel - Listening on Port " + port);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("DB Connection Failed");
            alert.setContentText("Could not connect to the database. Check credentials and schema: \n" + e.getMessage());
            alert.showAndWait();
        }
        catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("GUI Load Error");
            alert.setContentText("Could not load 'connections_to_server.fxml'. Check file path:\n" + e.getMessage());
            alert.showAndWait();
        }
        catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Server Startup Failed");
            alert.setContentText("An unexpected error occurred during server startup:\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        // קריאה ל-onConnect שמבצע את לוגיקת החיבור בפועל
        onConnect(event); 
    }

    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        // אם השרת פעיל, כדאי לסגור אותו לפני היציאה
        if (server != null && server.isListening()) {
             try {
                server.close();
            } catch (Exception ignored) {}
        }
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    // מתודת start נדרשת כדי להפעיל את המסך ההתחלתי
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Configuration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}