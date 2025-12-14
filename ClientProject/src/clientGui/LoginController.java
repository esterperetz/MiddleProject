package clientGui;

	

import java.io.IOException;

import client.ChatClient;
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

public class LoginController {

    @FXML
    private TextField txtIp;

    @FXML
    private Label lblStatus;

    private ClientUi clientUi;

    @FXML
    private void onConnect(ActionEvent event) {
        String ip = txtIp.getText().trim();

        if (ip.isEmpty()) {
            lblStatus.setText("Please enter server IP");
            return;
        }

        try {
       
            clientUi = new ClientUi(ip);

           
            if (clientUi != null) {
                lblStatus.setText("Login succeeded. Connected to: " + ip);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGui/orderUi.fxml"));
                Parent root = loader.load();

               
                OrderUi_controller controller = loader.getController();

                
                controller.initData(clientUi, ip);

               
                Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                primaryStage.setTitle("Orders Management");
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
    	        
                
            } else {
                lblStatus.setText("Login failed (clientUi is null)");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            lblStatus.setText("Login failed: " + e.getMessage());
        }
    }

  
    public ClientUi getClientUi() {
        return clientUi;
    }
}

