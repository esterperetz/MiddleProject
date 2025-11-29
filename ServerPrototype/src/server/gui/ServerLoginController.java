package server.gui;

import DBConnection.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private Button btnExit;

    @FXML
    private Label lblStatus;

    @FXML
    private void onSend(ActionEvent event) {
    	
        String user = txtUserName.getText();
        String pass = txtPassword.getText();
        String scheme = Scheme.getText();

        if (user.trim().isEmpty() || pass.trim().isEmpty()||scheme.trim().isEmpty()) {
            lblStatus.setText("You must enter user name and password");
            return;
        }
        try {
            DBConnection dbConector= new DBConnection(user,pass,scheme);
            lblStatus.setText("WellDone! We are connecting to your DB");
            //we need to called to methods that open the second screen
        }
        catch(Exception e)
        {
        	lblStatus.setText("Failed");
        	Alert alert = new Alert(Alert.AlertType.ERROR);
        	alert.setTitle("Error");
        	alert.setHeaderText("Something went wrong");
        	alert.setContentText("Unable to process your request.");
        	alert.showAndWait();
        	
        }

      

        System.out.println("Login send: user=" + user + ", pass=" + pass+ ", scheme="+scheme );
        //lblStatus.setText("Login sent to server");
    }

    @FXML
    private void onExit(ActionEvent event) {
        System.out.println("Exit from Server Login");
        ((Node) event.getSource()).getScene().getWindow().hide();
        
    }

    public void start(Stage primaryStage) throws Exception {
        javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("ServerLogin.fxml"));
        javafx.scene.Parent root = loader.load();
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        primaryStage.setTitle("Server Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
}