package clientGui;

	

import java.io.IOException;

import client.ChatClient;
import clientGui.navigation.MainNavigator;
import clientGui.navigation.SelectionController;
import clientGui.reservation.OrderUi_controller;
import entities.Alarm;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
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
            Alarm.showAlert("Input Error", "IP is empty , please enter server IP", AlertType.ERROR);
            return;
        }

        try {
            ClientUi clientUi = new ClientUi(ip);
            
            if (clientUi != null) { 
            	setClientUi(clientUi);
            	javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                Stage stage = (Stage) source.getScene().getWindow();
            	setStage(stage);
                
                lblStatus.setText("Login succeeded!");

               super.loadScreen("navigation/SelectionScreen",event,clientUi);
                
            }
            else {
            	lblStatus.setText("Login failed (clientUi is null)");
                Alarm.showAlert("Connection Error", "Client Creation Failed \n clientUi object is null.", AlertType.ERROR);            }
            
        } catch (Exception e) {
            // כאן השינוי העיקרי: הצגת ה-Alert
            Alarm.showAlert("Connection Error Login Failed", "Could not connect to server:" + e.getMessage() + "\nPlease check the IP address.",AlertType.ERROR);
        }
    }


//  
//    public ClientUi getClientUi() {
//        return clientUi;
//    }
}

