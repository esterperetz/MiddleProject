package clientGui.user;

import client.MessageListener;
import clientGui.BaseController;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SubscriberLoginController implements  MessageListener<Object>, BaseController{
	
	 @FXML
	    private TextField SubscriberCode;

	  

	    @FXML
	    void performLogin(ActionEvent event) {
	    	//
	        String username = SubscriberCode.getText();
	        System.out.println("Login attempt: " + username);
	        //MainNavigator.loadReservationScreen(true, "", "", ""); //add to get the subscriber details from DB
	        MainNavigator.loadScene("user/SubscriberOption");
	        //System.out.println("asdasdas");
	        // כאן תוסיף את הלוגיקה
	    }

	    @FXML
	    void goBack(ActionEvent event) {
	        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
	        MainNavigator.loadScene("navigation/SelectionScreen");
	    }

		@Override
		public void setClientUi(ClientUi clientUi) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMessageReceive(Object msg) {
			// TODO Auto-generated method stub
			
		}
}
