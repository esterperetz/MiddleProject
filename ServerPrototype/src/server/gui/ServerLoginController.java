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
import server.controller.ServerController;   // 👈 הייבוא הנכון
import server.gui.ServerViewController;     // 👈 קונטרולר של המסך עם הטבלה

public class ServerLoginController {

    @FXML
    private TextField txtUserName;

    @FXML
    private TextField Scheme;

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

    // אלה *לא* רכיבי FXML
    private DBConnection db;
    private ServerController server;

    @FXML
    void onConnect(ActionEvent event) {
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();
        String schema = Scheme.getText().trim();
        String portText = (txtPort != null) ? txtPort.getText().trim() : "5555";

        int port = 5555;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            port = 5555;
        }

        try {
            // 1. יוצרים חיבור ל־DB ושומרים בשדה
            db = new DBConnection(user, pass, schema);

            // 2. טוענים את ה־FXML של השרת (זה עם הטבלה של ה-IP)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connections_to_server.fxml"));
            Parent root = loader.load();
            ServerViewController view = loader.getController();

            // 3. מחליפים את ה־Scene בחלון הנוכחי (במקום לפתוח חלון חדש)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Server");
            stage.setScene(new Scene(root));
            stage.show();

            // 4. יוצרים את השרת ומתחילים להאזין – ושומרים אותו בשדה
            server = new ServerController(port, db, view);
            server.listen();
            view.log("Server listening on port " + port + "!");

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Connection failed");
            alert.setContentText("Check user / password / schema\n" + e.getMessage());
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

        try {
            DBConnection dbConector = new DBConnection(user, pass, scheme);
            lblStatus.setText("WellDone! We are connecting to your DB");
            onConnect(event);
        
            // אם אתה רוצה: אפשר מפה לקרוא ל-onConnect(event) ולהמשיך לשרת
            // onConnect(event);
        } catch (Exception e) {
            lblStatus.setText("Failed");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Something went wrong");
            alert.setContentText("Unable to process your request.");
            alert.showAndWait();
        }

        System.out.println("Login send: user=" + user + ", pass=" + pass + ", scheme=" + scheme);
    }

    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
