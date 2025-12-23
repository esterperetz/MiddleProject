package clientGui.user;

import Entities.Alarm;
import Entities.Response;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientLogic.UserLogic;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SubscriberLoginController extends MainNavigator implements MessageListener<Object> {

	@FXML
	private TextField SubscriberCode;
	private ActionEvent currentEvent;
	private int lastEnteredSubId;
	
	@FXML
	public void initialize() {
	    Platform.runLater(() -> {
	        if (SubscriberCode.getScene() != null && SubscriberCode.getScene().getWindow() != null) {
	            Stage stage = (Stage) SubscriberCode.getScene().getWindow();
	            stage.setOnCloseRequest(event -> {
	                clientUi.disconnectClient();
	             
	            });
	        }
	    });
	}
	
	@FXML
	void performLogin(ActionEvent event) {
		String subscriber_Code = SubscriberCode.getText().trim();
        
        if (subscriber_Code.isEmpty()) {
            super.showAlert("Input Error", "Please enter a code", Alert.AlertType.WARNING);
            return;
        }

        try {
            this.currentEvent = event;
            this.lastEnteredSubId = Integer.parseInt(subscriber_Code);
            UserLogic user = new UserLogic(clientUi);
            user.getSubscriberById(Integer.parseInt(subscriber_Code));
            
        } catch (NumberFormatException e) {
            super.showAlert("Format Error", "Code must be a number", Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	 
	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {
		        Response res = (Response) msg;
		        boolean isSuccess = res.getStatus().getString().equals("SUCCESS");

		        // חייבים להשתמש ב-Platform.runLater כי שינוי UI חייב לקרות ב-JavaFX Thread
		        Platform.runLater(() -> {
		            if (isSuccess) {
		            	System.out.println(res.getStatus().getString());
		                SubscriberOptionController controller = super.loadScreen("user/SubscriberOption", currentEvent, clientUi);
		                if (controller != null) {
		                    controller.initData(clientUi, true, lastEnteredSubId);
		                   
		                }
		            } else {
		                Alarm.showAlert("Invalid Subscriber code", "Please enter a valid code", Alert.AlertType.ERROR);
		            }
		            
		        });
	           
		    }
		} catch (Exception e) {
			System.out.println("two ");
		}
	}

	public void move() {

	}
	

	@FXML
	void goBack(ActionEvent event) {
		
		// וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
		super.loadScreen("navigation/SelectionScreen", event, clientUi);
	}

}
