package clientGui;

	

import java.io.IOException;

import client.ChatClient;
import clientGui.navigation.MainNavigator;
import clientGui.navigation.SelectionController;
import clientGui.reservation.OrderUi_controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends MainNavigator {
	
    @FXML
    private TextField txtIp;

    @FXML
    private Label lblStatus;

//    private ClientUi clientUi;

    @FXML
    private void onConnect(ActionEvent event) {
    	String ip = txtIp.getText().trim();

        if (ip.isEmpty()) {
            lblStatus.setText("Please enter server IP");
            return;
        }

        try {
            // 1. יצירת החיבור
            ClientUi clientUi = new ClientUi(ip);
            
            if (clientUi != null) { 
            	setClientUi(clientUi);
            	javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
            	setStage(stage);
                // הערה: כדאי להוסיף בדיקה ב-ClientUi אם החיבור לשרת באמת הצליח
                
                lblStatus.setText("Login succeeded!");

                // 2. מעבר למסך הבא באמצעות הנביגטור והעברת ה-ClientUi
               super.loadScreen("navigation/SelectionScreen",event,clientUi);
                
            }
            else {
                lblStatus.setText("Login failed (clientUi is null)");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Login failed: " + e.getMessage());
        }
    }


//  
//    public ClientUi getClientUi() {
//        return clientUi;
//    }
}

