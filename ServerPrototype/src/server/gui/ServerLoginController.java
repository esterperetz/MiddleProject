package server.gui;

import DBConnection.DBConnection; // הייבוא של הסינגלטון
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
import server.gui.ServerViewController;

public class ServerLoginController {

    @FXML private TextField txtUserName;
    @FXML private TextField Scheme;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnSend;
    @FXML private Button btnConnect;
    @FXML private Button btnExit;
    @FXML private TextField txtPort;
    @FXML private Label lblStatus;

    // db הוסר כי הוא סינגלטון
    private ServerController server; 

    @FXML
    void onConnect(ActionEvent event) {
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();
        String schema = Scheme.getText().trim();
        // פורט קבוע, כיוון שאין שדה txtPort ב-FXML ששלחת
        int port = 5555; 

        try {
            // *** 1. התיקון: קריאה ל-getConnection() שמפעילה את לוגיקת החיבור ***
            // אם החיבור נכשל, הוא יזרוק SQLException (או RuntimeException אם זה כשל קריטי)
            // ה-getConnection() ינסה ליצור את החיבור הקבוע אם הוא עדיין לא נוצר.
            DBConnection.getInstance().getConnection(); 
            
            lblStatus.setText("WellDone! We are connecting to your DB (using internal parameters)");

            // 2. טוענים את ה־FXML של השרת
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));
            Parent root = loader.load();
            ServerViewController view = loader.getController();

            // *** 3. התיקון: יוצרים את השרת ללא ארגומנט DBConnection ***
            server = new ServerController(port, view);
            server.listen();
            view.log("Server listening on port " + port + "!");

            // 4. מחליפים את ה־Scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Server");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Connection failed");
            // שיניתי את הודעת השגיאה שתשקף את השימוש בפרמטרים הפנימיים
            alert.setContentText("Check internal DB parameters in DBConnection or server port\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onSend(ActionEvent event) {
        String user = txtUserName.getText();
        String pass = txtPassword.getText();
        String scheme = Scheme.getText();

        if (user.trim().isEmpty() || pass.trim().isEmpty() || scheme.trim().isEmpty()) {
            lblStatus.setText("You must enter user name and password");
            return;
        }
        
        // קריאה ל-onConnect שמטפל בהכל
        onConnect(event);

        System.out.println("Login send: user=" + user + ", pass=" + pass + ", scheme=" + scheme);
    }

    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}