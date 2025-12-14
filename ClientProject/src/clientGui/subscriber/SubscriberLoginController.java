package clientGui.subscriber;

import clientGui.navigation.MainNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SubscriberLoginController {
	
	 @FXML
	    private TextField SubscriberCode;

	  

	    @FXML
	    void performLogin(ActionEvent event) {
	    	
	        String username = SubscriberCode.getText();
	        System.out.println("Login attempt: " + username);
	        MainNavigator.loadReservationScreen(true, "", "", ""); //add to get the subscriber details from DB
	        // כאן תוסיף את הלוגיקה
	    }

	    @FXML
	    void goBack(ActionEvent event) {
	        // וודא שגם הקובץ SelectionScreen.fxml נמצא באותה תיקייה
	        MainNavigator.loadScene("navigation/SelectionScreen");
	    }
}
